package me.n4th4not.dynamicfps.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import me.n4th4not.dynamicfps.DynamicFPSMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;

import static me.n4th4not.dynamicfps.util.Localization.gui;

public final class HudInfoRenderer {
	private static final Minecraft minecraft = Minecraft.getInstance();

	public static void renderInfo(PoseStack pose) {
		if (DynamicFPSMod.disabledByUser()) drawCenteredText(pose, gui("hud.disabled"), 32);
		else if (DynamicFPSMod.isForcingLowFPS()) drawCenteredText(pose, gui("hud.reducing"), 32);
	}

	private static void drawCenteredText(PoseStack pose, Component component, float y) {
		Font fontRenderer = minecraft.gui.getFont();

		int stringWidth = fontRenderer.width(component);
		int windowWidth = minecraft.getWindow().getGuiScaledWidth();

		MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

		fontRenderer.drawInBatch(
				component.getVisualOrderText(),
				(windowWidth - stringWidth) / 2f, //x
				y, //y
				0xFFFFFFFF, //color
				true, //shadow
				pose.last().pose(), //matrix
				buffer, //buffer
				false, //transparent
				0, //enable(!=0) -> BakedGlyph.Effect
				255 //packedLightCoords
		);

		buffer.endBatch();
	}
}