package dev.foxgirl.loadingbackgrounds.mixins;

import dev.foxgirl.loadingbackgrounds.LoadingBackgroundsScreen;
import dev.foxgirl.loadingbackgrounds.util.DrawStatus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ConnectScreen.class)
public abstract class MixinConnectScreen extends Screen {
    private MixinConnectScreen(final Component title) {
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
