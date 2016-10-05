package com.fortitudetec.testing.junit4.spark;

/**
 * This exception is only thrown if the security cannot be reset on {@link spark.Spark}.
 */
public class SparkUnsecureResetException extends RuntimeException {

    public SparkUnsecureResetException(String message) {
        super(message);
    }

    public SparkUnsecureResetException(String message, Throwable cause) {
        super(message, cause);
    }
}
