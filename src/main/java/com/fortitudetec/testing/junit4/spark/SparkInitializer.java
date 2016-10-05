package com.fortitudetec.testing.junit4.spark;

/**
 * Can be used to customize the Spark test server instance, e.g. to use {@link spark.Spark#port} to
 * set the port.
 */
@FunctionalInterface
public interface SparkInitializer {

    void init();

}
