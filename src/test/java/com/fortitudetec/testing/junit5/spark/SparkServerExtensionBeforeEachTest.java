package com.fortitudetec.testing.junit5.spark;

import static org.assertj.core.api.Assertions.assertThat;

import com.fortitudetec.testing.junit5.spark.JavaSparkRunnerExtension.SparkStarter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Optional;

@ExtendWith(JavaSparkRunnerExtension.class)
class SparkServerExtensionBeforeEachTest {

    private Client client;

    @BeforeEach
    void setUp(SparkStarter s) {
        s.runSpark(http -> {
            http.get("/ping", (request, response) -> "pong");
            http.get("/health", (request, response) -> "healthy");
        });
    }

    @BeforeEach
    void setUp() {
        client = ClientBuilder.newClient();
    }

    @AfterEach
    void tearDown() {
        Optional.ofNullable(client).ifPresent(Client::close);
    }

    @Test
    void testSparkServerExtension_PingRequest() {
        Response response = client.target(URI.create("http://localhost:4567/ping"))
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("pong");
    }

    @Test
    void testSparkServerExtension_HealthRequest() {
        Response response = client.target(URI.create("http://localhost:4567/health"))
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("healthy");
    }

}