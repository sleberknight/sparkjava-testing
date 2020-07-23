package com.fortitudetec.testing.junit5.spark;


import static org.assertj.core.api.Assertions.assertThat;

import com.fortitudetec.testing.junit5.spark.JavaSparkRunnerExtension.SparkStarter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Optional;

@ExtendWith(JavaSparkRunnerExtension.class)
class SparkServerRuleWithPortTest {

    private Client client = ClientBuilder.newBuilder().build();
    ;

    @BeforeAll
    static void setUp(SparkStarter s) {
        s.runSpark(http -> {
            http.port(6543);
            http.get("/ping", (request, response) -> "pong");
            http.get("/health", (request, response) -> "healthy");
        });
    }

    @AfterEach
    void tearDown() {
        Optional.ofNullable(client).ifPresent(Client::close);
    }

    @Test
    void testSparkServerRule_PingRequest() {
        Response response = client.target(URI.create("http://localhost:6543/ping"))
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("pong");
    }

    @Test
    void testSparkServerRule_HealthRequest() {
        Response response = client.target(URI.create("http://localhost:6543/health"))
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("healthy");
    }

}
