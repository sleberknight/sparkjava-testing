package com.fortitudetec.testing.junit4.spark;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

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
 *  public static final SparkServerRule SPARK_SERVER = new SparkServerRule(service -> {
 *      service.port(56789);
 *      service.get("/ping", (request, response) -> "pong");
 *      service.get("/health", (request, response) -> "healthy");
 *  });
 * </code>
 * </pre>
 */
public class SparkServerRule extends ExternalResource {

    private static final Logger LOG = LoggerFactory.getLogger(SparkServerRule.class);

    private ServiceInitializer serviceInitializer;
    private Service service;

    /**
     * Create Spark server rule with specified {@link ServiceInitializer}. You use the {@link ServiceInitializer}
     * to configure the Spark server port, IP address, security, routes, etc. Things like port and IP address must
     * be configured before routes.
     *
     * @see Service
     */
    public SparkServerRule(ServiceInitializer svcInit) {
        this.serviceInitializer = svcInit;
    }

    @Override
    protected void before() throws Throwable {
        LOG.trace("Start spark server");
        service = Service.ignite();
        serviceInitializer.init(service);

        LOG.trace("Await initialization of Spark...");
        service.awaitInitialization();

        LOG.trace("Spark is ignited!");
    }

    @Override
    protected void after() {
        LOG.trace("Stopping Spark...");
        service.stop();

        LOG.trace("Spark stopped");
    }

}
