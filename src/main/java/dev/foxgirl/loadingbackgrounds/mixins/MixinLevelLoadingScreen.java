package dev.foxgirl.loadingbackgrounds.mixins;

import dev.foxgirl.loadingbackgrounds.LoadingBackgroundsScreen;
import dev.foxgirl.loadingbackgrounds.util.DrawStatus;
import dev.foxgirl.loadingbackgrounds.util.Position;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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
        final DrawStatus status = LoadingBackgroundsScreen.getInstance().draw(graphics, this, false);
        if (status == DrawStatus.FALLBACK) {
            super.renderBackground(graphics, mouseX, mouseY, delta);
        }
    }

    //? >=1.21.9 {
    @Shadow(aliases = "loadTracker")
    private net.minecraft.client.multiplayer.LevelLoadTracker levelLoadingProgress;
    //?} else {
    /*@Shadow(aliases = "progressListener")
    @org.spongepowered.asm.mixin.Final
    private net.minecraft.server.level.progress.StoringChunkProgressListener levelLoadingProgress;
    *///?}

    @ModifyVariable(method = "render", at = @At("STORE"), name = "i")
    private int loadingbackgrounds$render$0(final int x) {
        final Position position = LoadingBackgroundsScreen.getInstance().getPosition();
        if (position != Position.CENTER) {
            final int width = this.width;
            final int size =
                //? >=1.21.9 {
                this.levelLoadingProgress.statusView().radius() * 5;
                //?} else {
                /*this.levelLoadingProgress.getDiameter();
                *///?}
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
        final Position position = LoadingBackgroundsScreen.getInstance().getPosition();
        if (position != Position.CENTER) {
            final int height = this.height;
            final int size =
                //? >=1.21.9 {
                this.levelLoadingProgress.statusView().radius() * 2;
                //?} else {
                /*this.levelLoadingProgress.getDiameter();
                 *///?}
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
