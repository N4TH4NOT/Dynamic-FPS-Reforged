package me.n4th4not.dynamicfps.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class FallbackConfigScreen
    extends Screen {

    private final Screen parent;

    private static final Component WARNING_0 = Localization.config("warn_cloth_config.0");
    private static final Component WARNING_1 = Localization.config("warn_cloth_config.1");

    public FallbackConfigScreen(Screen parent) {
        super(Localization.config("title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int width = 152;
        int height = 20;
        int x = (this.width - width) / 2;
        int y = this.height - height - 5;

        this.addRenderableWidget(
                new Button(x, y, width, height,CommonComponents.GUI_BACK, button -> this.onClose())
        );
    }

    @Override
    public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        super.render(pose, mouseX, mouseY, partialTicks);

        int width = super.width / 2;
        int height = super.height / 3;

        GuiComponent.drawCenteredString(pose, this.font, WARNING_0.getVisualOrderText(), width, height, 0xFFFFFF);
        GuiComponent.drawCenteredString(pose, this.font, WARNING_1.getVisualOrderText(), width, height + 10, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }
}