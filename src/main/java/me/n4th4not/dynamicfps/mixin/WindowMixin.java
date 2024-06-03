package me.n4th4not.dynamicfps.mixin;

import com.mojang.blaze3d.platform.Window;
import me.n4th4not.dynamicfps.DynamicFPSMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Window.class)
public class WindowMixin {
    /**
     * Sets a frame rate limit while we're cancelling some or all rendering.
     */
    @Inject(method = "getFramerateLimit", at = @At("RETURN"), cancellable = true)
    private void onGetFramerateLimit(CallbackInfoReturnable<Integer> ci) {
        int target = Math.min(DynamicFPSMod.targetFrameRate(), ci.getReturnValue());

        if (target != -1) {
            // We're currently reducing the frame rate
            // Instruct Minecraft to render max 15 FPS
            // Going lower here makes resuming feel sluggish
            ci.setReturnValue(Math.max(target, 15));
        }
    }
}