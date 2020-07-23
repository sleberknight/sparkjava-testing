# sparkjava-testing

A small testing library for spinning up a [Spark](http://sparkjava.com/) server for functional testing of HTTP clients.

## Example usage

See the actual tests for example usage. But if you don't want to do that, here's a short example that uses
the JUnit Jupiter extension.

First, annotate your test with `@ExtendWith(JavaSparkRunnerExtension.class)`. Then, create a `@BeforeAll` method
that accepts a `SparkStarter`, which allows you to define the various HTTP routes. Then your tests run, making HTTP 
requests to the test server, and finally the server is shut down after tests have run. The injection can be done in 
several places e.g. in methods annotated with @BeforeEach @BeforeAll or even @Test.

```java
@BeforeAll
static void beforeAll(SparkStarter s) {
    s.runSpark(http -> {
        http.get("/ping", (request, response) -> "pong");
        http.get("/health", (request, response) -> "healthy");
    });
}
```

In the above example, there are two _routes_, `/ping` and `/health`, specified in the lambda which simply return
200 responses containing strings. Here's an example test using a Jersey client (I'm using [AssertJ](http://joel-costigliola.github.io/assertj/) 
assertions in this test):

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

Since Spark runs on port 4567 by default that's the port our client test uses. Also, the `client` is closed in a test
tear down method.

If you want to run the test on a different port you could do it as follows:

```java
@BeforeAll
static void setUp(SparkStarter s) {
    s.runSpark(http -> {
        http.port(9876);
        http.get("/ping", (request, response) -> "pong");
        http.get("/health", (request, response) -> "healthy");
    });
}
```

And if you want to change not only the port, but also the IP address and make the server secure, you can do it like this:

```java
@BeforeAll
static void setUp(SparkStarter s) {
    s.runSpark(https -> {
        https.ipAddress("127.0.0.1");
        https.port(9876);
        URL resource = Resources.getResource("sample-keystore.jks");
        https.get("/ping", (request, response) -> "pong");
        https.get("/health", (request, response) -> "healthy");
    });
}
```

See the actual unit tests for concrete examples.
