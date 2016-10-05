# sparkjava-testing

A small testing library with a JUnit Rule for spinning up a Spark server for functional testing of HTTP clients.

## Example usage

See the actual tests for example usage. But if you don't want to do that, here's a short example.

Generally you will want to use a JUnit `@ClassRule` so the server is spun up _once_ before any test has run. Then your tests run, making HTTP requests to the test server, and finally the server is shut down _once_ after all tests have run. In your JUnit 4 (haven't gotten around to doing this for JUnit 5 yet....) test class, declare a rule:

```java
@ClassRule
public static final SparkServerRule SPARK_SERVER = new SparkServerRule(() -> {
    get("/ping", (request, response) -> "pong");
    get("/health", (request, response) -> "healthy");
});
```

In the above rule, there are two _routes_, `/ping` and `/health`, specified in the lambda which simply return 200 responses containing strings. Here's an example test using a Jersey client (I'm using [AssertJ](http://joel-costigliola.github.io/assertj/) assertions in this test):

```java
@Test
public void testSparkServerRule_PingRequest() {
    client = ClientBuilder.newBuilder().build();
    Response response = client.target(URI.create("http://localhost:4567/ping"))
            .request()
            .get();
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.readEntity(String.class)).isEqualTo("pong");
}
```

Since Spark runs on port 4567 by default that's the port our client test uses. Also, the `client` is closed in a test tear down method.

The `SparkServerRule` class has two other constructors, one that accepts a port, and another that accepts a `SparkInitializer`. To start your test server on a different port, you can do this:

```java
@ClassRule
public static final SparkServerRule SPARK_SERVER = new SparkServerRule(9876, () -> {
    get("/ping", (request, response) -> "pong");
    get("/health", (request, response) -> "healthy");
});
```

And if you want to change not only the port, but also the IP address and make the server secure, you use the `SparkInitializer` (which is a `@FunctionalInterface` so you can use a lambda):

```java
@ClassRule
public static final SparkServerRule SPARK_SERVER = new SparkServerRule(
        () -> {
            Spark.ipAddress("127.0.0.1");
            Spark.port(9876);
            URL resource = Resources.getResource("sample-keystore.jks");
            String file = resource.getFile();
            Spark.secure(file, "password", null, null);
        },
        () -> {
            get("/ping", (request, response) -> "pong");
            get("/health", (request, response) -> "healthy");
        });
```

See the actual unit tests for concrete examples.
