package dev.blackdev;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Path;
import dev.blackdev.log.PacketLogger;
import dev.blackdev.web.WebServer;
public class PacketSpyMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Path gameDir = FabricLoader.getInstance().getGameDir();
        PacketLogger.init(gameDir);
        WebServer.start();
    }
}
