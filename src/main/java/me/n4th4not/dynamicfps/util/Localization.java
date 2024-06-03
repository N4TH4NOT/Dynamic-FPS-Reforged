package me.n4th4not.dynamicfps.util;

import me.n4th4not.dynamicfps.Constants;
import me.n4th4not.dynamicfps.DynamicFPSMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class Localization {
	private static String translationKey(String domain, String path) {
		return domain + ".dynamic_fps." + path;
	}
	
	private static MutableComponent localized(String domain, String path, Object... args) {
		return Component.translatable(translationKey(domain, path), args);
	}

	public static MutableComponent config(String name, Object... args) {
		return localized("config",name, args);
	}

	public static MutableComponent gui(String name) {
		return localized("gui",name);
	}

	public static String keyBinding(String name) {
		return translationKey("key",name);
	}
}