package com.fortitudetec.testing.junit4.spark;

import spark.Service;

@FunctionalInterface
public interface ServiceInitializer {

    void init(Service service);

}
