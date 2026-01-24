package dev.foxgirl.loadingbackgrounds.mixins;

import dev.foxgirl.loadingbackgrounds.util.TextureInfo;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DynamicTexture.class)
public abstract class MixinDynamicTexture extends AbstractTexture implements TextureInfo {
    @Override
    public void loadingbackgrounds$init() {
    }

    @Override
    public int loadingbackgrounds$getWidth() {
        return ((DynamicTexture) (Object) this).getPixels().getWidth();
    }

    @Override
    public int loadingbackgrounds$getHeight() {
        return ((DynamicTexture) (Object) this).getPixels().getHeight();
    }
}
