package com.fortitudetec.testing.junit4.spark;

import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SparkServerRuleWithFilterTest {

    private Client client;

    private static boolean authenticated;

    @SuppressWarnings("ThrowableNotThrown") // b/c halt throws *and returns* a HaltException
    @ClassRule
    public static final SparkServerRule SPARK_SERVER = new SparkServerRule(http -> {
        http.port(56789);
        http.before((request, response) -> {
            if (!authenticated) {
                http.halt(401, "Go away!");
            }
        });
        http.get("/secret", (request, response) -> "Don't forget to drink your Ovaltine!");
    });

    @After
    public void tearDown() {
        Optional.ofNullable(client).ifPresent(Client::close);
    }

    @Test
    public void testSparkServerRule_PingRequest_WhenAuthenticated() {
        authenticated = true;

        client = ClientBuilder.newClient();
        Response response = client.target(URI.create("http://localhost:56789/secret"))
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("Don't forget to drink your Ovaltine!");
    }

    @Test
    public void testSparkServerRule_PingRequest_WhenNotAuthenticated() {
        authenticated = false;

        client = ClientBuilder.newClient();
        Response response = client.target(URI.create("http://localhost:56789/secret"))
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.readEntity(String.class)).isEqualTo("Go away!");
    }
}
