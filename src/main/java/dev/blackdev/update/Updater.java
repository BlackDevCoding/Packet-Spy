package dev.blackdev.update;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Updater {
    private static final String REPO_LATEST_API = "https://api.github.com/repos/BlackDevCoding/Packet-Spy/releases/latest";
    private static final Pattern ASSET_JAR = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+\\.jar)\"");
    private static final Pattern ASSET_URL = Pattern.compile("\"browser_download_url\"\s*:\s*\"([^\"]+)\"");
    private static final Pattern VERSION_IN_NAME = Pattern.compile("(\\d+\\.\\d+\\.\\d+)");
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private static volatile boolean started = false;

    public static void startAsync() {
        if (started) return;
        started = true;
        boolean enabled = Boolean.parseBoolean(System.getProperty("packetspy.autoUpdate", "true"));
        if (!enabled) return;
        Thread t = new Thread(Updater::run, "PacketSpy-Updater");
        t.setDaemon(true);
        t.start();
    }

    private static void run() {
        try {
            Path currentJar = findCurrentJar();
            if (currentJar == null || !currentJar.toString().endsWith(".jar")) {
                log("Updater: not running from a jar, skipping.");
                return;
            }
            String currentVersion = readCurrentVersion();
            if (currentVersion == null) {
                log("Updater: could not read current version; skipping.");
                return;
            }

            String json = httpGet(REPO_LATEST_API);
            if (json == null || json.isEmpty()) {
                log("Updater: failed to query GitHub releases.");
                return;
            }

            String tag = extract(json, "\"tag_name\"\s*:\s*\"([^\"]+)\"");
            String assetName = firstMatch(ASSET_JAR, json);
            String downloadUrl = firstMatch(ASSET_URL, json);

            if (assetName == null || downloadUrl == null) {
                log("Updater: no jar asset found on latest release.");
                return;
            }

            String latestVersion = versionFrom(assetName);
            if (latestVersion == null) latestVersion = versionFrom(tag);
            if (latestVersion == null) {
                log("Updater: unable to parse version, skipping.");
                return;
            }

            if (!isNewer(latestVersion, currentVersion)) {
                log("Updater: up-to-date (" + currentVersion + ").");
                return;
            }

            Path modsDir = currentJar.getParent();
            Path tmp = modsDir.resolve(assetName + ".download");
            Path target = modsDir.resolve(assetName);

            log("Updater: downloading " + assetName + " ...");
            if (!downloadTo(downloadUrl, tmp)) {
                log("Updater: download failed.");
                Files.deleteIfExists(tmp);
                return;
            }
            log("Updater: downloaded to " + tmp.getFileName());

            // Schedule swap on exit to avoid file locking issues
            Path oldBackup = modsDir.resolve(currentJar.getFileName().toString() + ".old");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    tryMove(tmp, target);
                    Files.move(currentJar, oldBackup, StandardCopyOption.REPLACE_EXISTING);
                    log("Updater: replaced jar. Old -> " + oldBackup.getFileName());
                    try { Files.deleteIfExists(oldBackup); } catch (IOException ignore) {}
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, "PacketSpy-Updater-Swap"));

            log("Updater: update ready. It will be applied on exit/restart.");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static boolean isNewer(String latest, String current) {
        try {
            String[] a = latest.split("\\.");
            String[] b = current.split("\\.");
            for (int i = 0; i < Math.max(a.length, b.length); i++) {
                int ai = i < a.length ? Integer.parseInt(a[i]) : 0;
                int bi = i < b.length ? Integer.parseInt(b[i]) : 0;
                if (ai != bi) return ai > bi;
            }
            return false;
        } catch (Exception e) {
            return !latest.equals(current);
        }
    }

    private static String versionFrom(String s) {
        if (s == null) return null;
        Matcher m = VERSION_IN_NAME.matcher(s);
        return m.find() ? m.group(1) : null;
    }

    private static String httpGet(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "PacketSpy-Updater")
                .timeout(Duration.ofSeconds(15))
                .GET().build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) return null;
        return res.body();
    }

    private static boolean downloadTo(String url, Path out) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent", "PacketSpy-Updater")
                    .timeout(Duration.ofMinutes(2))
                    .GET().build();
            HttpResponse<InputStream> res = HTTP.send(req, HttpResponse.BodyHandlers.ofInputStream());
            if (res.statusCode() / 100 != 2) return false;
            try (InputStream in = res.body(); OutputStream os = Files.newOutputStream(out)) {
                in.transferTo(os);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String extract(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        return m.find() ? m.group(1) : null;
    }

    private static String firstMatch(Pattern p, String text) {
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1) : null;
    }

    private static Path findCurrentJar() {
        try {
            CodeSource src = Updater.class.getProtectionDomain().getCodeSource();
            if (src == null) return null;
            URI uri = src.getLocation().toURI();
            Path path = Path.of(uri);
            if (Files.isDirectory(path)) return null;
            return path;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private static String readCurrentVersion() {
        try {
            Optional<ModContainer> c = FabricLoader.getInstance().getModContainer("packetspy");
            if (c.isEmpty()) return null;
            return c.get().getMetadata().getVersion().getFriendlyString();
        } catch (Throwable t) {
            return null;
        }
    }

    private static void tryMove(Path src, Path dst) throws IOException {
        try { Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
        catch (IOException e) { Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING); }
    }

    private static void log(String s) { System.out.println("[PacketSpy] " + s); }
}
