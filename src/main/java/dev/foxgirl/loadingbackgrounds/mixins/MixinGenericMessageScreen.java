package dev.foxgirl.loadingbackgrounds.mixins;

import dev.foxgirl.loadingbackgrounds.LoadingBackgrounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GenericMessageScreen.class)
public abstract class MixinGenericMessageScreen extends Screen {
    private MixinGenericMessageScreen(final Component title) {
        super(title);
    }

    @Override
    public void renderBackground(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        if (!LoadingBackgrounds.isLoadingMessage(getTitle()) || !LoadingBackgrounds.getInstance().draw(graphics, this, true)) {
            super.renderBackground(graphics, mouseX, mouseY, delta);
        }
    }
}
