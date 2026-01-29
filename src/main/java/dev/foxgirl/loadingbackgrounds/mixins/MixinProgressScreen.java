package dev.foxgirl.loadingbackgrounds.mixins;

import dev.foxgirl.loadingbackgrounds.LoadingBackgroundsScreen;
import dev.foxgirl.loadingbackgrounds.util.DrawStatus;
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
    public void renderBackground(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        final DrawStatus status = LoadingBackgroundsScreen.getInstance().draw(graphics, this, false);
        if (status == DrawStatus.FALLBACK) {
            super.renderBackground(graphics, mouseX, mouseY, delta);
        }
    }
}
