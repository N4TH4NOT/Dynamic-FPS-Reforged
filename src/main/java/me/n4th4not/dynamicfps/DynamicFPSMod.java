package me.n4th4not.dynamicfps;

import me.n4th4not.dynamicfps.compat.ClothConfig;
import me.n4th4not.dynamicfps.compat.FREX;
import me.n4th4not.dynamicfps.compat.GLFW;
import me.n4th4not.dynamicfps.config.Config;
import me.n4th4not.dynamicfps.config.DynamicFPSConfig;
import me.n4th4not.dynamicfps.util.HudInfoRenderer;
import me.n4th4not.dynamicfps.util.IdleHandler;
import me.n4th4not.dynamicfps.util.OptionsHolder;
import me.n4th4not.dynamicfps.util.SmoothVolumeHandler;
import me.n4th4not.dynamicfps.util.duck.DuckLoadingOverlay;
import me.n4th4not.dynamicfps.util.duck.DuckSoundEngine;
import me.n4th4not.dynamicfps.util.event.WindowObserver;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static me.n4th4not.dynamicfps.util.Localization.config;

@Mod(Constants.MOD_ID)
public class DynamicFPSMod {
	private static Config CONFIG = Config.ACTIVE;
	private static PowerState STATE = PowerState.FOCUSED;

	public static DynamicFPSConfig MOD_CONFIG = DynamicFPSConfig.load();

	private static boolean FORCE_LOW_FPS = false;
	private static boolean KEYBIND_DISABLED = false;

	private static final Minecraft MINECRAFT = Minecraft.getInstance();

	private static @Nullable WindowObserver WINDOW;

	private static long LAST_RENDER;

	public static final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_ID);

	// we always render one last frame before actually reducing FPS, so the hud text
	// shows up instantly when forcing low fps.
	// additionally, this would enable mods which render differently while mc is
	// inactive.
	private static boolean HAS_RENDERED_LAST_FRAME = false;

	private static final boolean OVERLAY_OPTIMIZATION_ACTIVE = !ModList.get().isLoaded("rrls");

	// Internal "API" for Dynamic FPS itself

	public DynamicFPSMod() {
		if (FMLLoader.getDist().isDedicatedServer()) return;

		IdleHandler.init();
		SmoothVolumeHandler.init();

		LOGGER.info("Dynamic FPS {} active on Forge!", Constants.getModVersion());


		ModLoadingContext.get().registerExtensionPoint(
				ConfigScreenHandler.ConfigScreenFactory.class,
				() -> new ConfigScreenHandler.ConfigScreenFactory(
						(minecraft, parent) -> {
							if (!ModList.get().isLoaded("cloth-config"))
								return new AlertScreen(
										() -> Minecraft.getInstance().setScreen(parent),
										config("title"),
										config("warn_cloth_config").withStyle(ChatFormatting.RED)
								);
							else return ClothConfig.genConfigScreen(parent);
						}
				)
		);

		MinecraftForge.EVENT_BUS.addListener(this::renderGuiOverlay);
		MinecraftForge.EVENT_BUS.addListener(KeybindingsHandler::interaction);
	}

	public void renderGuiOverlay(RenderGuiOverlayEvent.Post event) {
		if (!MINECRAFT.options.renderDebug)
			HudInfoRenderer.renderInfo(event.getPoseStack());
	}

	public static boolean disabledByUser() {
		return KEYBIND_DISABLED;
	}

	public static @Nullable WindowObserver getWindow() {
		return WINDOW;
	}

	public static boolean isDisabled() {
		return FREX.isActive() || KEYBIND_DISABLED || !MOD_CONFIG.enabled();
	}

	public static String whyIsTheModNotWorking() {
		List<String> results = new ArrayList<>();

		if (KEYBIND_DISABLED) results.add("keybinding");
		if (!MOD_CONFIG.enabled()) results.add("mod config");
		if (FREX.isActive()) results.add("another mod");

		return String.join(", ", results);
	}

	public static void toggleDisabled() {
		KEYBIND_DISABLED = !KEYBIND_DISABLED;
		onStatusChanged(true);
	}

	public static void onConfigChanged() {
		MOD_CONFIG.save();
		IdleHandler.init();
		SmoothVolumeHandler.init();
	}

	public static void onStatusChanged(boolean userInitiated) {
		// Ensure game runs at full speed when
		// Returning without giving any other input
		if (userInitiated) IdleHandler.onActivity();

		checkForStateChanges();
	}

	public static PowerState powerState() {
		return STATE;
	}

	public static boolean isForcingLowFPS() {
		return FORCE_LOW_FPS;
	}

	public static void toggleForceLowFPS() {
		FORCE_LOW_FPS = !FORCE_LOW_FPS;
		onStatusChanged(true);
	}

	public static void setWindow(long address) {
		IdleHandler.setWindow(address);
		WINDOW = new WindowObserver(address);
	}

	public static boolean checkForRender() {
		long currentTime = Util.getEpochMillis();
		long timeSinceLastRender = currentTime - LAST_RENDER;

		if (!checkForRender(timeSinceLastRender)) return false;

		LAST_RENDER = currentTime;
		return true;
	}

	public static int targetFrameRate() {
		return CONFIG.frameRateTarget();
	}

	public static boolean uncapMenuFrameRate() {
		return MOD_CONFIG.uncapMenuFrameRate();
	}

	public static float volumeMultiplier(SoundSource source) {
		return CONFIG.volumeMultiplier(source);
	}

	public static DynamicFPSConfig.VolumeTransitionSpeed volumeTransitionSpeed() {
		return MOD_CONFIG.volumeTransitionSpeed();
	}

	public static boolean shouldShowToasts() {
		return CONFIG.showToasts();
	}

	public static boolean shouldShowLevels() {
		return isDisabled() || !isLevelCoveredByOverlay();
	}

	// Internal logic

	private static boolean isLevelCoveredByOverlay() {
		return OVERLAY_OPTIMIZATION_ACTIVE && MINECRAFT.getOverlay() instanceof LoadingOverlay && ((DuckLoadingOverlay) MINECRAFT.getOverlay()).dynamic_fps$isReloadComplete();
	}

	@SuppressWarnings("squid:S1215") // Garbage collector call
	public static void handleStateChange(PowerState previous, PowerState current) {
		if (Constants.DEBUG) LOGGER.info("State changed from {} to {}.", previous, current);

		Config before = CONFIG;
		CONFIG = MOD_CONFIG.get(current);
		GLFW.applyWorkaround(); // Apply mouse hover fix if required
		HAS_RENDERED_LAST_FRAME = false; // Render next frame w/o delay

		if (CONFIG.runGarbageCollector()) System.gc();
		// Update volume of current sounds for users not using smooth volume transition
		if (!volumeTransitionSpeed().isActive()) {
			for (SoundSource source : SoundSource.values()) {
				((DuckSoundEngine) MINECRAFT.getSoundManager().soundEngine).dynamic_fps$updateVolume(source);
			}
		}

		if (before.graphicsState() != CONFIG.graphicsState()) {
			if (before.graphicsState() == GraphicsState.DEFAULT)
				OptionsHolder.copyOptions(MINECRAFT.options);

			OptionsHolder.applyOptions(MINECRAFT.options, CONFIG.graphicsState());
		}
	}

	private static void checkForStateChanges() {
		if (WINDOW == null) return;

		if (MINECRAFT.isSameThread()) checkForStateChanges0();
        else {
			// Schedule check for the beginning of the next frame
			MINECRAFT.tell(DynamicFPSMod::checkForStateChanges0);
		}
	}

	private static void checkForStateChanges0() {
		PowerState current;

		if (isDisabled()) current = PowerState.FOCUSED;
        else if (FORCE_LOW_FPS) current = PowerState.UNFOCUSED;
        else if (WINDOW.isFocused()) {
			if (!IdleHandler.isIdle()) current = PowerState.FOCUSED;
            else current = PowerState.ABANDONED;
		}
		else if (WINDOW.isHovered()) current = PowerState.HOVERED;
        else if (!WINDOW.isIconified()) current = PowerState.UNFOCUSED;
        else current = PowerState.INVISIBLE;

		if (STATE != current) {
			PowerState previous = STATE;
			STATE = current;
			handleStateChange(previous, current);
		}
	}

	private static boolean checkForRender(long timeSinceLastRender) {
		int frameRateTarget = targetFrameRate();

		// Special frame rate target
		//  0 -> disable rendering
		// -1 -> uncapped frame rate
		if (frameRateTarget <= 0) return frameRateTarget == -1;

		// Render one more frame before
		// Applying the custom frame rate
		// So changes show up immediately
		if (!HAS_RENDERED_LAST_FRAME) {
			HAS_RENDERED_LAST_FRAME = true;
			return true;
		}

		long frameTime = 1000 / frameRateTarget;
		return timeSinceLastRender >= frameTime;
	}
}