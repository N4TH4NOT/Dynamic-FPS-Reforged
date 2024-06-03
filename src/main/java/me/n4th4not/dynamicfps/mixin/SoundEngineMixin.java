package me.n4th4not.dynamicfps.mixin;

import com.mojang.blaze3d.audio.Listener;
import me.n4th4not.dynamicfps.util.SmoothVolumeHandler;
import me.n4th4not.dynamicfps.util.duck.DuckSoundEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin
    implements DuckSoundEngine {

    @Shadow
    @Final
    private Options options;

    @Shadow
    private boolean loaded;

    @Shadow
    @Final
    private Listener listener;

    @Shadow
    @Final
    private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;

    @Shadow
    private float calculateVolume(SoundInstance instance) {
        throw new RuntimeException("Failed to find SoundEngine.calculateVolume.");
    }

    @Shadow public abstract void play(SoundInstance p_120313_);

    @Unique
    private static final Minecraft dynamic_fps$minecraft = Minecraft.getInstance();

    @Override
    public void dynamic_fps$updateVolume(SoundSource source) {
        if (!this.loaded) return;

        if (source.equals(SoundSource.MASTER)) {
            float volume = this.options.getSoundSourceVolume(source);
            this.listener.setGain(this.dynamic_FPS_Reforged$adjustVolume(volume, source));
            return;
        }

        // When setting the volume to zero we pause music but cancel other types of sounds
        // This results in a less jarring experience when quickly tabbing out and back in.
        // Also fixes this compat bug: https://github.com/juliand665/Dynamic-FPS/issues/55
        boolean isMusic = source.equals(SoundSource.MUSIC) || source.equals(SoundSource.RECORDS);

        this.instanceToChannel.forEach((instance, handle) -> {
            if (!instance.getSource().equals(source)) return;

            float volume = this.calculateVolume(instance);

            handle.execute(channel -> {
                if (volume <= 0.0f) {
                    // Pause music unconditionally when volume is zero
                    // Otherwise if vanilla doesn't pause the sound set the volume to zero
                    // This allows long sounds (e.g. sonic boom) to be heard when tabbing back in
                    if (isMusic) channel.pause();
                    else if (!dynamic_fps$minecraft.isPaused()) channel.setVolume(volume);
                }
                else {
                    // Only resume music if the game is not paused
                    // Because vanilla pauses music on pause screens
                    if (isMusic && !dynamic_fps$minecraft.isPaused()) channel.unpause();

                    channel.setVolume(volume);
                }
            });
        });
    }

    /**
     * Cancels playing sounds while we are overwriting the volume to be off.
     * <p>
     * This is done in favor of actually setting the volume to zero because it
     * Allows pausing and resuming the sound engine without cancelling all active sounds.
     */
    @Inject(method ="play", at = @At("HEAD"), cancellable = true)
    private void play(SoundInstance instance, CallbackInfo ci) {
        if (SmoothVolumeHandler.volumeMultiplier(instance.getSource()) == 0.0f) ci.cancel();
    }

    @Inject(method ="playDelayed", at = @At("HEAD"), cancellable = true)
    private void play(SoundInstance instance, int delay, CallbackInfo ci) {
        this.play(instance,ci);
    }

    /**
     * Applies the user's requested volume multiplier to any newly played sounds.
     */
    @Inject(method = "getVolume", at = @At("RETURN"), cancellable = true)
    private void getVolume(SoundSource source, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(this.dynamic_FPS_Reforged$adjustVolume(cir.getReturnValue(), source));
    }

    /**
     * Adjust the given volume with the multiplier set in the active Dynamic FPS config.
     */
    @Unique
    private float dynamic_FPS_Reforged$adjustVolume(float value, @Nullable SoundSource source) {
        if (source == null) source = SoundSource.MASTER;
        return value * SmoothVolumeHandler.volumeMultiplier(source);
    }
}