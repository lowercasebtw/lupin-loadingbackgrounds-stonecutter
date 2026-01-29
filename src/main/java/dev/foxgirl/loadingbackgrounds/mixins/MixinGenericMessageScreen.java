package dev.foxgirl.loadingbackgrounds.mixins;

import dev.foxgirl.loadingbackgrounds.LoadingBackgroundsScreen;
import dev.foxgirl.loadingbackgrounds.util.DrawStatus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GenericMessageScreen.class)
public abstract class MixinGenericMessageScreen extends Screen {
    private MixinGenericMessageScreen(final Component title) {
        super(title);
    }

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void loadingbackgrounds$replaceBackgroundRendering(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta, final CallbackInfo ci) {
        final DrawStatus status = LoadingBackgroundsScreen.getInstance().draw(graphics, this, true);
        if (status == DrawStatus.FALLBACK || status == DrawStatus.FAILED) {
            ci.cancel();
        }

        if (!LoadingBackgroundsScreen.isLoadingMessage(getTitle()) || status == DrawStatus.FALLBACK) {
            super.renderBackground(graphics, mouseX, mouseY, delta);
        }
    }
}
