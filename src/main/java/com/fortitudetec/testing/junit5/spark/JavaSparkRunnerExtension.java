package com.fortitudetec.testing.junit5.spark;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.create;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import spark.Service;

import java.util.Optional;
import java.util.function.Consumer;

public class JavaSparkRunnerExtension implements ParameterResolver {

    public static class SparkStarter implements CloseableResource {

        private Service service;

        public SparkStarter runSpark(Consumer<Service> consumer) {
            service = Service.ignite();
            consumer.accept(service);
            service.awaitInitialization();
            return this;
        }

        @Override
        public void close() {
            Optional.ofNullable(service).ifPresent(Service::stop);
        }

    }

    private static final Namespace NAMESPACE = create(JavaSparkRunnerExtension.class);

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return appliesTo(parameterContext.getParameter().getType());
    }

    private boolean appliesTo(Class<?> type) {
        return type == SparkStarter.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE) //
                .getOrComputeIfAbsent(parameterContext, key -> new SparkStarter(), SparkStarter.class);
    }

}
