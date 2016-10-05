package com.fortitudetec.testing.junit4.spark;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;
import spark.Spark;

import java.lang.reflect.Field;

/**
 * A JUnit rule that starts a Spark server ( <a href="http://sparkjava.com/">http://sparkjava.com/</a> ) before
 * tests run, and shuts the server down after tests run. It can be annotated with either @{@link org.junit.ClassRule}
 * or {@link org.junit.Rule}. If annotated with ClassRule, then the same server will serve all tests, started before
 * the first test is run and stopped after all tests have finished. If annotated with Rule, then a new server is started
 * before and stopped after each individual test.
 * <p>
 * Example usage:
 * <pre><code>
 * {@literal @}ClassRule
 *  public static final SparkServerRule SPARK_SERVER = new SparkServerRule(() -> {
 *      get("/ping", (request, response) -> "pong");
 *      get("/health", (request, response) -> "healthy");
 *  }
 * </code>
 * </pre>
 * <p>
 * <em>This rule resets the Spark server to its default values upon cleanup, so that changes (e.g. to a different
 * port) will not affect other tests.</em>
 *
 * @implNote This uses a nasty hack to unset security on Spark. See {@link #resetSparkToNonSecureModeHack()} or the
 * explanation in the error message, {@link #HACK_EXCEPTION_MESSAGE}
 */
public class SparkServerRule extends ExternalResource {

    private static final Logger LOG = LoggerFactory.getLogger(SparkServerRule.class);

    private static final String DEFAULT_SPARK_IP_ADDRESS = "0.0.0.0";
    private static final int DEFAULT_SPARK_PORT = 4567;
    private static final String HACK_EXCEPTION_MESSAGE =
            "Error resetting Spark to non-secure via private field access hack."
                    + " Most likely a newer version of Spark has changed the"
                    + " internals of the Spark and/or Service classes. This hack"
                    + " relies on the Spark class having a private static class named"
                    + " SingletonHolder as the first inner class, which must contain a"
                    + " private static final Service field named INSTANCE. Service"
                    + " is expected to have a protected SslStores instance field named"
                    + " sslStores that we set to null to remove any previous secure"
                    + " settings.";

    private final Runnable routes;

    /**
     * Create Spark server rule with specified routes. Spark server will start on the default port, 4567.
     */
    public SparkServerRule(Runnable routes) {
        this.routes = routes;
    }

    /**
     * Create Spark server rule with specified port and routes. Spark server will start on specified port.
     */
    public SparkServerRule(int port, Runnable routes) {
        Spark.port(port);
        this.routes = routes;
    }

    /**
     * Create Spark server rule with specified initializer and routes. The {@link SparkInitializer} is a
     * {@link FunctionalInterface} so can be a target of lambdas. The initializer can be used to customize
     * the test Spark server. For example, you can change the port via {@link Spark#port(int)}, set the IP
     * address via {@link Spark#ipAddress(String)}, or even set the server to run securely over https
     * using {@link Spark#secure(String, String, String, String)}.
     */
    public SparkServerRule(SparkInitializer initializer, Runnable routes) {
        initializer.init();
        this.routes = routes;
    }

    @Override
    protected void before() throws Throwable {
        LOG.trace("Start spark server");
        new Thread(routes).start();

        LOG.trace("Await initialization of Spark...");
        Spark.awaitInitialization();

        LOG.trace("Spark is ignited!");
    }

    @Override
    protected void after() {
        LOG.trace("Stopping Spark...");
        Spark.stop();

        // reset to defaults, otherwise Spark settings persist across test classes...
        Spark.ipAddress(DEFAULT_SPARK_IP_ADDRESS);
        Spark.port(DEFAULT_SPARK_PORT);
        resetSparkToNonSecureModeHack();

        LOG.trace("Spark stopped");
    }

    private void resetSparkToNonSecureModeHack() {
        try {
            Class<?> singletonHolderClass = Spark.class.getDeclaredClasses()[0];
            Field instanceStaticField = singletonHolderClass.getDeclaredField("INSTANCE");
            instanceStaticField.setAccessible(true);
            Service service = (Service) instanceStaticField.get(null);
            Field sslStoresField = service.getClass().getDeclaredField("sslStores");
            sslStoresField.setAccessible(true);
            sslStoresField.set(service, null);
        } catch (Exception e) {
            throw new SparkUnsecureResetException(HACK_EXCEPTION_MESSAGE, e);
        }
    }

}
