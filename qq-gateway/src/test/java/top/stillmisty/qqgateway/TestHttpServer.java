package top.stillmisty.qqgateway;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

/** 测试用 HTTP 服务端，记录请求并按注入的 handler 返回响应。 */
final class TestHttpServer implements AutoCloseable {

  record Request(String path, String method, Map<String, List<String>> headers, String body) {

    String header(String name) {
      for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
        if (entry.getKey().equalsIgnoreCase(name) && !entry.getValue().isEmpty()) {
          return entry.getValue().getFirst();
        }
      }
      return "";
    }
  }

  record Response(int status, String body, Map<String, String> headers) {

    static Response json(String body) {
      return new Response(200, body, Map.of("Content-Type", "application/json"));
    }

    static Response status(int status) {
      return new Response(status, "", Map.of());
    }

    static Response status(int status, String body) {
      return new Response(status, body, Map.of("Content-Type", "application/json"));
    }
  }

  @FunctionalInterface
  interface Handler {
    Response handle(Request request);
  }

  private final HttpServer server;
  private final List<Request> requests = new CopyOnWriteArrayList<>();
  private volatile Handler handler = request -> Response.json("{}");

  TestHttpServer() throws IOException {
    server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    server.createContext(
        "/",
        exchange -> {
          byte[] rawBody = exchange.getRequestBody().readAllBytes();
          Request request =
              new Request(
                  exchange.getRequestURI().getPath(),
                  exchange.getRequestMethod(),
                  Map.copyOf(exchange.getRequestHeaders()),
                  new String(rawBody, StandardCharsets.UTF_8));
          requests.add(request);
          Response response;
          try {
            response = handler.handle(request);
          } catch (RuntimeException e) {
            response = Response.status(500, "{\"error\":\"" + e + "\"}");
          }
          response.headers().forEach((key, value) -> exchange.getResponseHeaders().set(key, value));
          byte[] out = response.body().getBytes(StandardCharsets.UTF_8);
          if (out.length == 0) {
            exchange.sendResponseHeaders(response.status(), -1);
          } else {
            exchange.sendResponseHeaders(response.status(), out.length);
            exchange.getResponseBody().write(out);
          }
          exchange.close();
        });
    server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    server.start();
  }

  void setHandler(Handler handler) {
    this.handler = handler;
  }

  URI baseUri() {
    return URI.create("http://127.0.0.1:" + server.getAddress().getPort());
  }

  List<Request> requests() {
    return List.copyOf(requests);
  }

  long countRequests(String path) {
    return requests.stream().filter(request -> request.path().equals(path)).count();
  }

  @Override
  public void close() {
    server.stop(0);
  }
}
