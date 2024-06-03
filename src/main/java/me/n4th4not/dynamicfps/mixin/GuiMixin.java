package me.n4th4not.dynamicfps.mixin;

import me.n4th4not.dynamicfps.DynamicFPSMod;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    /**
     * Cancels rendering the GUI if it is determined to currently not be visible.
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void shouldRender(CallbackInfo callbackInfo) {
        if (!DynamicFPSMod.shouldShowLevels()) callbackInfo.cancel();
    }
}