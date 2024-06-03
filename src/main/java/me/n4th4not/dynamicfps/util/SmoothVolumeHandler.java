package me.n4th4not.dynamicfps.util;

import me.n4th4not.dynamicfps.DynamicFPSMod;
import me.n4th4not.dynamicfps.util.duck.DuckSoundEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

import java.util.HashMap;
import java.util.Map;

public class SmoothVolumeHandler {
    private static boolean ACTIVE = false;
    private static final Minecraft MINECRAFT = Minecraft.getInstance();
    private static final Map<SoundSource, Float> CURRENT_OVERRIDES = new HashMap<>();

    public static void init() {
        if (ACTIVE || !DynamicFPSMod.volumeTransitionSpeed().isActive()) return;

        ACTIVE = true;
        MinecraftForge.EVENT_BUS.addListener(SmoothVolumeHandler::tickVolumes);
    }

    public static float volumeMultiplier(SoundSource source) {
        if (!ACTIVE) return DynamicFPSMod.volumeMultiplier(source);
        else return CURRENT_OVERRIDES.getOrDefault(source, 1.0f);
    }

    private static void tickVolumes(TickEvent.ClientTickEvent event) {
        for (SoundSource source : SoundSource.values()) {
            float desired = DynamicFPSMod.volumeMultiplier(source);
            float current = CURRENT_OVERRIDES.getOrDefault(source, 1.0f);

            if (current != desired) {
                if (current < desired) {
                    float up = DynamicFPSMod.volumeTransitionSpeed().getUp();
                    CURRENT_OVERRIDES.put(source, Math.min(desired, current + up / 20.0f));
                }
                else {
                    float down = DynamicFPSMod.volumeTransitionSpeed().getDown();
                    CURRENT_OVERRIDES.put(source, Math.max(desired, current - down / 20.0f));
                }

                // Update volume of currently playing sounds
                ((DuckSoundEngine) MINECRAFT.getSoundManager().soundEngine).dynamic_fps$updateVolume(source);
            }
        }
    }
}