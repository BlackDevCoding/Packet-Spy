package dev.blackdev.web;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
public class WebServer {
    private static HttpServer server;
    private static final List<OutputStream> clients = Collections.synchronizedList(new ArrayList<>());
    private static volatile int port = -1;
    private static volatile boolean started = false;
    public static int start() {
        if (started) return port;
        int desired = Integer.getInteger("packetspy.port", 8753);
        for (int p = desired; p < desired + 10; p++) {
            try {
                server = HttpServer.create(new InetSocketAddress("127.0.0.1", p), 0);
                port = p;
                break;
            } catch (IOException ignored) {}
        }
        if (server == null) throw new RuntimeException("PacketSpy web server failed to bind any port");
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/", new StaticHandler("web/index.html", "text/html; charset=utf-8"));
        server.createContext("/style.css", new StaticHandler("web/style.css", "text/css; charset=utf-8"));
        server.createContext("/app.js", new StaticHandler("web/app.js", "text/javascript; charset=utf-8"));
        server.createContext("/health", exchange -> respond(exchange, 200, "ok\n", "text/plain"));
        server.createContext("/events", new SseHandler());
        server.start();
        started = true;
        new Thread(WebServer::pingLoop, "PacketSpy-SSE-Ping").start();
        System.out.println("[PacketSpy] Web UI: http://127.0.0.1:" + port + "/");
        return port;
    }
    private static void respond(HttpExchange exchange, int code, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        Headers h = exchange.getResponseHeaders();
        h.add("Content-Type", contentType);
        h.add("Cache-Control", "no-cache, no-store, must-revalidate");
        h.add("Access-Control-Allow-Origin", "http://127.0.0.1:" + port);
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }
    private static class StaticHandler implements HttpHandler {
        private final String resourcePath;
        private final String contentType;
        StaticHandler(String resourcePath, String contentType) { this.resourcePath = resourcePath; this.contentType = contentType; }
        @Override public void handle(HttpExchange exchange) throws IOException {
            Headers h = exchange.getResponseHeaders();
            h.add("Content-Type", contentType);
            h.add("Cache-Control", "no-cache");
            InputStream in = WebServer.class.getClassLoader().getResourceAsStream(resourcePath);
            if (in == null) { respond(exchange, 404, "not found\n", "text/plain"); return; }
            byte[] buf = in.readAllBytes();
            exchange.sendResponseHeaders(200, buf.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(buf); }
        }
    }
    public static void broadcast(String jsonLine) {
        String line = "data: " + jsonLine + "\n\n";
        byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
        synchronized (clients) {
            clients.removeIf(os -> {
                try { os.write(bytes); os.flush(); return false; }
                catch (IOException e) { try { os.close(); } catch (IOException ignored) {} return true; }
            });
        }
    }
    private static class SseHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            Headers h = exchange.getResponseHeaders();
            h.add("Content-Type", "text/event-stream; charset=utf-8");
            h.add("Cache-Control", "no-cache");
            h.add("Connection", "keep-alive");
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            synchronized (clients) { clients.add(os); }
            String hello = ": connected\n\n";
            os.write(hello.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
    }
    private static void pingLoop() {
        try {
            while (true) {
                String ping = ": ping\n\n";
                byte[] bytes = ping.getBytes(StandardCharsets.UTF_8);
                synchronized (clients) {
                    clients.removeIf(os -> {
                        try { os.write(bytes); os.flush(); return false; }
                        catch (IOException e) { try { os.close(); } catch (IOException ignored) {} return true; }
                    });
                }
                Thread.sleep(15000);
            }
        } catch (InterruptedException ignored) {}
    }
}
