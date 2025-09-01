package dev.blackdev;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import javax.swing.SwingUtilities;
import java.nio.file.Path;
import dev.blackdev.log.PacketLogger;
import dev.blackdev.ui.PacketSpyWindow;
public class PacketSpyMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.setProperty("java.awt.headless", "false");
        Path gameDir = FabricLoader.getInstance().getGameDir();
        PacketLogger.init(gameDir);
        SwingUtilities.invokeLater(() -> PacketSpyWindow.getInstance().showWindow());
    }
}
