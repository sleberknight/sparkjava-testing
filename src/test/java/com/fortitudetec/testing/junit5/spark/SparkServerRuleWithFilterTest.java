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
class SparkServerRuleWithFilterTest {

    private Client client = ClientBuilder.newClient();

    private static boolean authenticated;

    @BeforeAll
    static void setUp(SparkStarter s) {
        s.runSpark(http -> {
            http.port(56789);
            http.before((request, response) -> {
                if (!authenticated) {
                    http.halt(401, "Go away!");
                }
            });
            http.get("/secret", (request, response) -> "Don't forget to drink your Ovaltine!");
        });
    }

    @AfterEach
    void tearDown() {
        Optional.ofNullable(client).ifPresent(Client::close);
    }

    @Test
    void testSparkServerRule_PingRequest_WhenAuthenticated() {
        authenticated = true;
        Response response = client.target(URI.create("http://localhost:56789/secret"))
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("Don't forget to drink your Ovaltine!");
    }

    @Test
    void testSparkServerRule_PingRequest_WhenNotAuthenticated() {
        authenticated = false;
        Response response = client.target(URI.create("http://localhost:56789/secret"))
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.readEntity(String.class)).isEqualTo("Go away!");
    }
}
