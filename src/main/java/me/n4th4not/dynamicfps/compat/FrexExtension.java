package me.n4th4not.dynamicfps.compat;

import net.minecraftforge.fml.IExtensionPoint;

import java.util.function.Supplier;

public class FrexExtension {
	public record Factory(Supplier<Boolean> api) implements IExtensionPoint<Factory> {}
}
