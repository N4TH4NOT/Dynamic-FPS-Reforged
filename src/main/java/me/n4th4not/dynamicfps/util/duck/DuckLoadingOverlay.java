package me.n4th4not.dynamicfps.util.duck;

public interface DuckLoadingOverlay {
    default boolean dynamic_fps$isReloadComplete() {
        throw new RuntimeException("No implementation for dynamic_fps$isReloadComplete was found.");
    }
}