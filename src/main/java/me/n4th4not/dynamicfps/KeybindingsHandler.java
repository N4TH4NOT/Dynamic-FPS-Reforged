package me.n4th4not.dynamicfps;

import com.mojang.blaze3d.platform.InputConstants;
import me.n4th4not.dynamicfps.util.Localization;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
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

    static void interaction(InputEvent event) {
        if (ACTIVATION_KEYBINDING.isDown()) DynamicFPSMod.toggleDisabled();
        else if (FORCE_KEYBINDING.isDown()) DynamicFPSMod.toggleForceLowFPS();
    }

    @SubscribeEvent
    static void register(RegisterKeyMappingsEvent event) {
        event.register(FORCE_KEYBINDING);
        event.register(ACTIVATION_KEYBINDING);
    }
}