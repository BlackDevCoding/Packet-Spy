package dev.blackdev.log;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.packet.Packet;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
public class PacketLogger {
    private static PacketLogger INSTANCE;
    private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final BufferedWriter writer;
    private final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;
    private PacketLogger(Path logFile) throws IOException {
        this.writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(logFile), StandardCharsets.UTF_8));
        Thread t = new Thread(this::writerLoop, "PacketSpy-Writer");
        t.setDaemon(true);
        t.start();
    }
    public static void init(Path gameDir) {
        if (INSTANCE != null) return;
        try {
            Path dir = gameDir.resolve("packet-logs");
            Files.createDirectories(dir);
            String ts = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC).format(Instant.now());
            Path file = dir.resolve("packets-" + ts + ".ndjson");
            INSTANCE = new PacketLogger(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize PacketLogger", e);
        }
    }
    public static PacketLogger get() {
        if (INSTANCE == null) init(FabricLoader.getInstance().getGameDir());
        return INSTANCE;
    }
    public void shutdown() {
        running.set(false);
        try { writer.flush(); writer.close(); } catch (IOException ignored) {}
    }
    private void writerLoop() {
        while (running.get()) {
            try {
                String line = queue.take();
                writer.write(line);
                writer.write('\n');
                writer.flush();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (IOException io) {
                running.set(false);
            }
        }
    }
    private String nowIso() { return ISO.format(Instant.now()); }
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
    private void emit(String json) {
        queue.offer(json);
        try { dev.blackdev.web.WebServer.broadcast(json); } catch (Throwable ignored) {}
    }
    private void logCommon(String direction, Packet<?> packet) {
        String ts = nowIso();
        String klass = packet.getClass().getName();
        String details;
        try { details = esc(String.valueOf(packet)); } catch (Throwable t) { details = "toString() failed: " + t.getClass().getSimpleName(); }
        String json = "{\"ts\":\"" + ts + "\",\"direction\":\"" + direction + "\",\"class\":\"" + esc(klass) + "\",\"details\":\"" + details + "\"}";
        emit(json);
    }
    public void logOutbound(Packet<?> packet) { logCommon("OUT", packet); }
    public void logInbound(Packet<?> packet) { logCommon("IN", packet); }
}
