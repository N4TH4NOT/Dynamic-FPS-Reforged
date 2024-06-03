package me.n4th4not.dynamicfps;

import com.mojang.blaze3d.platform.InputConstants;
import me.n4th4not.dynamicfps.util.Localization;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class KeybindingsHandler {
    private static final KeyMapping FORCE_KEYBINDING = new KeyMapping(
            Localization.keyBinding( "toggle_forced"),
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            KeyMapping.CATEGORY_MISC
    );

    public static final KeyMapping ACTIVATION_KEYBINDING = new KeyMapping(
            Localization.keyBinding( "toggle_disabled"),
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            KeyMapping.CATEGORY_MISC
    );

    @SubscribeEvent
    public static void interaction(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.getKeyMapping().same(ACTIVATION_KEYBINDING)) DynamicFPSMod.toggleDisabled();
        else if (event.getKeyMapping().same(FORCE_KEYBINDING)) DynamicFPSMod.toggleForceLowFPS();
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void keybindingRegistration(RegisterKeyMappingsEvent event) {
            event.register(FORCE_KEYBINDING);
            event.register(ACTIVATION_KEYBINDING);
        }
    }
}