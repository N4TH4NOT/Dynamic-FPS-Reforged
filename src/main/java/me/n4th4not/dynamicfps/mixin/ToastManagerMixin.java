package me.n4th4not.dynamicfps.mixin;

import me.n4th4not.dynamicfps.DynamicFPSMod;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ToastComponent.class)
public class ToastManagerMixin {
    @Inject(method = "freeSlots", at = @At("HEAD"), cancellable = true)
    private void hasFreeSlots(CallbackInfoReturnable<Integer> cir) {
        if (!DynamicFPSMod.shouldShowToasts()) cir.setReturnValue(0);
    }
}