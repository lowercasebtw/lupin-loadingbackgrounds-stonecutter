package dev.foxgirl.loadingbackgrounds.mixins;

import dev.foxgirl.loadingbackgrounds.LoadingBackgrounds;
import dev.foxgirl.loadingbackgrounds.util.Position;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LevelLoadingScreen.class)
public abstract class MixinLevelLoadingScreen extends Screen {

    private MixinLevelLoadingScreen(final Component title) {
        super(title);
    }

    @Override
    public void renderBackground(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        if (!LoadingBackgrounds.getInstance().draw(graphics, this, false)) {
            super.renderBackground(graphics, mouseX, mouseY, delta);
        }
    }

    @Shadow
    @Final
    private StoringChunkProgressListener progressListener;

    @ModifyVariable(method = "render", at = @At("STORE"), name = "i")
    private int loadingbackgrounds$render$0(final int x) {
        final Position position = LoadingBackgrounds.getInstance().getPosition();
        if (position != Position.CENTER) {
            final int width = this.width;
            final int size = this.progressListener.getDiameter();
            switch (position.ordinal()) {
                case 1:
                case 3:
                    return size + (size / 4);
                case 2:
                case 4:
                    return width - size - (size / 4);
            }
        }

        return x;
    }

    @ModifyVariable(method = "render", at = @At("STORE"), name = "j")
    private int loadingbackgrounds$render$1(final int y) {
        final Position position = LoadingBackgrounds.getInstance().getPosition();
        if (position != Position.CENTER) {
            final int height = this.height;
            final int size = this.progressListener.getDiameter();
            switch (position.ordinal()) {
                case 1:
                case 2:
                    return size + (size / 4) + 15;
                case 3:
                case 4:
                    return height - size - (size / 4);
            }
        }

        return y;
    }
}
