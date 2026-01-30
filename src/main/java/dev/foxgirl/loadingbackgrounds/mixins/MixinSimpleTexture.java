package dev.foxgirl.loadingbackgrounds.mixins;

import dev.foxgirl.loadingbackgrounds.util.TextureInfo;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SimpleTexture.class)
public abstract class MixinSimpleTexture extends AbstractTexture implements TextureInfo {
    @Unique
    private int loadingbackgrounds$dataWidth = -1;

    @Unique
    private int loadingbackgrounds$dataHeight = -1;

    @Override
    public int loadingbackgrounds$getWidth() {
        return loadingbackgrounds$dataWidth;
    }

    @Override
    public int loadingbackgrounds$getHeight() {
        return loadingbackgrounds$dataHeight;
    }

    @Override
    public void loadingbackgrounds$init() {
        if (loadingbackgrounds$dataWidth <= 0 || loadingbackgrounds$dataHeight <= 0) {
            //? >=1.21.5 {
            com.mojang.blaze3d.opengl.GlStateManager._bindTexture(((com.mojang.blaze3d.opengl.GlTexture) this.texture).glId());
            //?} else {
            /*this.bind();
             *///?}

            final int[] buffer = new int[1];

            GL11.glGetTexLevelParameteriv(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH, buffer);
            loadingbackgrounds$dataWidth = buffer[0];

            GL11.glGetTexLevelParameteriv(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT, buffer);
            loadingbackgrounds$dataHeight = buffer[0];
        }
    }
}
