package me.n4th4not.dynamicfps.compat;

import me.n4th4not.dynamicfps.Constants;
import me.n4th4not.dynamicfps.DynamicFPSMod;
import me.n4th4not.dynamicfps.GraphicsState;
import me.n4th4not.dynamicfps.PowerState;
import me.n4th4not.dynamicfps.config.Config;
import me.n4th4not.dynamicfps.util.VariableStepTransformer;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

import java.util.Locale;
import java.util.Optional;

import static me.n4th4not.dynamicfps.util.Localization.config;

public final class ClothConfig {
    public static Screen genConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(config( "title"))
                .setSavingRunnable(DynamicFPSMod::onConfigChanged);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(
                config("category.general")
        );

        general.addEntry(
                entryBuilder.startBooleanToggle(
                                config("enabled"),
                                DynamicFPSMod.MOD_CONFIG.enabled()
                        )
                        .setDefaultValue(true)
                        .setSaveConsumer(DynamicFPSMod.MOD_CONFIG::setEnabled)
                        .build()
        );

        general.addEntry(
                entryBuilder.startTextDescription(CommonComponents.EMPTY).build()
        );

        general.addEntry(
                entryBuilder.startIntSlider(
                                config("idle_time"),
                                DynamicFPSMod.MOD_CONFIG.idleTime() / 60,
                                0, 30
                        )
                        .setDefaultValue(0)
                        .setSaveConsumer(value -> DynamicFPSMod.MOD_CONFIG.setIdleTime(value * 60))
                        .setTextGetter(ClothConfig::idleTimeMessage)
                        .setTooltip(config("idle_time_tooltip"))
                        .build()
        );

        general.addEntry(
                entryBuilder.startBooleanToggle(
                                config("uncap_menu_frame_rate"),
                                DynamicFPSMod.MOD_CONFIG.uncapMenuFrameRate()
                        )
                        .setDefaultValue(false)
                        .setSaveConsumer(DynamicFPSMod.MOD_CONFIG::setUncapMenuFrameRate)
                        .setTooltip(config("uncap_menu_frame_rate_tooltip"))
                        .build()
        );

        VariableStepTransformer volumeTransformer = getVolumeStepTransformer();

        general.addEntry(
                entryBuilder.startIntSlider(
                                config("volume_transition_speed_up"),
                                volumeTransformer.toStep((int) (DynamicFPSMod.volumeTransitionSpeed().getUp() * 10)),
                                1, 31
                        )
                        .setDefaultValue(volumeTransformer.toStep((int) (1.0f * 10)))
                        .setSaveConsumer(step -> DynamicFPSMod.volumeTransitionSpeed().setUp((float) volumeTransformer.toValue(step) / 10))
                        .setTextGetter(ClothConfig::volumeTransitionMessage)
                        .setTooltip(config("volume_transition_speed_tooltip"))
                        .build()
        );

        general.addEntry(
                entryBuilder.startIntSlider(
                                config("volume_transition_speed_down"),
                                volumeTransformer.toStep((int) (DynamicFPSMod.volumeTransitionSpeed().getDown() * 10)),
                                1, 31
                        )
                        .setDefaultValue(volumeTransformer.toStep((int) (0.5f * 10)))
                        .setSaveConsumer(step -> DynamicFPSMod.volumeTransitionSpeed().setDown((float) volumeTransformer.toValue(step) / 10))
                        .setTextGetter(ClothConfig::volumeTransitionMessage)
                        .setTooltip(config("volume_transition_speed_tooltip"))
                        .build()
        );

        // Used for each state's frame rate target slider below
        VariableStepTransformer fpsTransformer = getFpsTransformer();

        for (PowerState state : PowerState.values()) {
            if (!state.configurable) continue;

            Config config = DynamicFPSMod.MOD_CONFIG.get(state);
            Config standard = Config.getDefault(state);

            ConfigCategory category = builder.getOrCreateCategory(
                    config("category." + state.toString().toLowerCase())
            );

            // Having too many possible values on our slider is hard to use, so the conversion is not linear:
            // Instead the steps between possible values keep getting larger (from 1 to 2, 5, and finally 10)
            // Selecting the value all the way at the end sets no FPS limit, imitating the regular FPS slider
            category.addEntry(
                    entryBuilder.startIntSlider(
                                    config("frame_rate_target"),
                                    fpsTransformer.toStep(config.frameRateTarget()),
                                    0, 68
                            )
                            .setDefaultValue(fpsTransformer.toStep(standard.frameRateTarget()))
                            .setSaveConsumer(step -> config.setFrameRateTarget(fpsTransformer.toValue(step)))
                            .setTextGetter(ClothConfig::fpsTargetMessage)
                            .build()
            );

            SubCategoryBuilder volumes = entryBuilder.startSubCategory(config("volume_multiplier"));

            for (SoundSource source : SoundSource.values()) {
                String name = source.getName();

                volumes.add(
                        entryBuilder.startIntSlider(
                                        Component.translatable("soundCategory." + name),
                                        (int) (config.rawVolumeMultiplier(source) * 100),
                                        0, 100
                                )
                                .setDefaultValue((int) (standard.rawVolumeMultiplier(source) * 100))
                                .setSaveConsumer(value -> config.setVolumeMultiplier(source, value / 100f))
                                .setTextGetter(ClothConfig::volumeMultiplierMessage)
                                .build()
                );
            }

            category.addEntry(volumes.build());

            category.addEntry(
                    entryBuilder.startEnumSelector(
                                    config("graphics_state"),
                                    GraphicsState.class,
                                    config.graphicsState()
                            )
                            .setDefaultValue(standard.graphicsState())
                            .setSaveConsumer(config::setGraphicsState)
                            .setEnumNameProvider(ClothConfig::graphicsStateMessage)
                            .setTooltipSupplier(ClothConfig::graphicsStateTooltip)
                            .build()
            );

            category.addEntry(
                    entryBuilder.startBooleanToggle(
                                    config("show_toasts"),
                                    config.showToasts()
                            )
                            .setDefaultValue(standard.showToasts())
                            .setSaveConsumer(config::setShowToasts)
                            .setTooltip(config("show_toasts_tooltip"))
                            .build()
            );

            category.addEntry(
                    entryBuilder.startBooleanToggle(
                                    config("run_garbage_collector"),
                                    config.runGarbageCollector()
                            )
                            .setDefaultValue(standard.runGarbageCollector())
                            .setSaveConsumer(config::setRunGarbageCollector)
                            .setTooltip(config("run_garbage_collector_tooltip"))
                            .build()
            );
        }

        return builder.build();
    }

    private static Component idleTimeMessage(int value) {
        if (value == 0) return config("disabled");
        else return config("minutes", value);
    }

    private static VariableStepTransformer getVolumeStepTransformer() {
        VariableStepTransformer transformer = new VariableStepTransformer();

        transformer.addStep(1, 30);
        transformer.addStep(970, 1000);

        return transformer;
    }

    private static Component volumeTransitionMessage(int step) {
        float value = (float) getVolumeStepTransformer().toValue(step) / 10;

        if (value < 100.0f) return Component.literal(value + "%");
        else return config("volume_transition_speed_instant");
    }

    private static VariableStepTransformer getFpsTransformer() {
        VariableStepTransformer transformer = new VariableStepTransformer();

        transformer.addStep(1, 20);
        transformer.addStep(2, 72);
        transformer.addStep(3, 75);
        transformer.addStep(5, 100);
        transformer.addStep(10, 260);

        return transformer;
    }

    private static Component fpsTargetMessage(int step) {
        int fps = getFpsTransformer().toValue(step);

        if (fps != Constants.NO_FRAME_RATE_LIMIT) return Component.translatable("options.framerate", fps);
        else return Component.translatable("options.framerateLimit.max");
    }

    private static Component volumeMultiplierMessage(int value) {
        return Component.literal(Integer.toString(value) + "%");
    }

    private static Component graphicsStateMessage(Enum<GraphicsState> graphicsState) {
        return config("graphics_state_" + graphicsState.toString().toLowerCase(Locale.ROOT));
    }

    private static Optional<Component[]> graphicsStateTooltip(GraphicsState graphicsState) {
        if (!graphicsState.equals(GraphicsState.MINIMAL)) return Optional.empty();
        return Optional.of(new Component[]{ config("graphics_state_minimal_tooltip").withStyle(ChatFormatting.RED) });
    }
}