package me.n4th4not.dynamicfps;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class Constants {
    // Meta
    public static final String MOD_ID = "dynamicfps_forge";
    public static final boolean DEBUG = isDevelopmentEnvironment();

    // Miscellaneous
    // Minecraft considers limits >=260 as infinite
    public static final int NO_FRAME_RATE_LIMIT = 260;

    public static Path getCacheDir() {
        Path base = FMLPaths.GAMEDIR.get();
        return ensureDir(base.resolve(".cache").resolve(Constants.MOD_ID));
    }

    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }


    public static boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static Optional<String> getModVersion() {
        Optional<? extends ModContainer> optional = ModList.get().getModContainerById(MOD_ID);
        return optional.map(modContainer -> modContainer.getModInfo().getVersion().toString());
    }

    private static Path ensureDir(Path path) {
        try { Files.createDirectories(path); }
        catch (IOException e) { throw new RuntimeException("Failed to create Dynamic FPS directory.", e); }

        return path;
    }
}