package dev.foxgirl.loadingbackgrounds.mixins;

import dev.foxgirl.loadingbackgrounds.LoadingBackgrounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ProgressScreen.class)
public abstract class MixinProgressScreen extends Screen {
    private MixinProgressScreen(final Component title) {
        super(title);
    }

    @Override
    public void renderBackground(final GuiGraphics context, final int mouseX, final int mouseY, final float delta) {
        if (!LoadingBackgrounds.getInstance().draw(context, this, false)) {
            super.renderBackground(context, mouseX, mouseY, delta);
        }
    }
}
