package me.n4th4not.dynamicfps.compat;

import me.n4th4not.dynamicfps.Constants;
import me.n4th4not.dynamicfps.DynamicFPSMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Reimplementation of FREX
 * @see net.minecraftforge.client.ConfigScreenHandler
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class FREX {
	private static final AtomicBoolean ACTIVE = new AtomicBoolean();

	public static boolean isActive() {
		return ACTIVE.get();
	}

	@SubscribeEvent
	public static void searchExtension(FMLClientSetupEvent event) {
		ModList.get().forEachModContainer(
			(id, container) -> {
				if (!ACTIVE.get()) {
					container.getCustomExtension(FrexExtension.Factory.class)
						.filter(factory -> factory.api().get())
						.ifPresent(factory -> {
							ACTIVE.set(true);
							if (Constants.DEBUG) DynamicFPSMod.LOGGER.info("{} request to show all frames, so this mod is disabled", container.getModId());
						});
				}
				else if (Constants.DEBUG) {
					container.getCustomExtension(FrexExtension.Factory.class)
							.ifPresentOrElse(factory -> {
								if (factory.api().get()) DynamicFPSMod.LOGGER.info("{} request to show all frames", container.getModId());
								else DynamicFPSMod.LOGGER.info("{} lets us know to act as normal :)", container.getModId());
							},
								() -> DynamicFPSMod.LOGGER.info("{} not responding", container.getModId())
							);
				}
			}
		);
	}
}
