package dev.foxgirl.loadingbackgrounds;

import com.google.common.collect.Iterators;
import dev.deftu.omnicore.api.client.OmniClient;
import dev.deftu.omnicore.api.client.render.ImmediateScreenRenderer;
import dev.deftu.omnicore.api.client.render.OmniRenderingContext;
import dev.deftu.omnicore.api.client.render.pipeline.OmniRenderPipelines;
import dev.deftu.omnicore.api.color.ColorFormat;
import dev.deftu.omnicore.api.color.OmniColor;
import dev.foxgirl.loadingbackgrounds.util.DrawStatus;
import dev.foxgirl.loadingbackgrounds.util.LoadingBackgroundsConfig;
import dev.foxgirl.loadingbackgrounds.util.Position;
import dev.foxgirl.loadingbackgrounds.util.TextureInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;

public final class LoadingBackgroundsScreen extends Screen {
    private static final Logger LOGGER = LogManager.getLogger(LoadingBackgroundsScreen.class);
    private static LoadingBackgroundsScreen INSTANCE;
    private static final long secondsStart = System.nanoTime();
    private static final Set<String> LOADING_MESSAGE_TRANSLATION_KEYS = Set.of(
        "menu.generatingLevel",
        "menu.generatingTerrain",
        "menu.loadingForcedChunks",
        "menu.loadingLevel",
        "menu.preparingSpawn",
        "menu.savingChunks",
        "menu.savingLevel",
        "menu.working",
        "multiplayer.downloadingStats",
        "multiplayer.downloadingTerrain",
        "selectWorld.data_read",
        "selectWorld.loading_list",
        "selectWorld.resource_load",
        "resourcepack.downloading",
        "resourcepack.progress",
        "download.pack.title"
    );

    private LoadingBackgroundsConfig config = LoadingBackgroundsConfig.DEFAULT;
    private Iterator<ResourceLocation> textures;
    private ResourceLocation texturePrevious;
    private ResourceLocation textureCurrent;
    private double stateSecondsStarted = secondsSinceStart();
    private boolean stateIsFading = false;

    public LoadingBackgroundsScreen() {
        super(Component.empty());
        INSTANCE = this;
    }

    private void initFromScreen(final Screen screen) {
        this.width = screen.width;
        this.height = screen.height;
    }

    public void init(@NotNull final Path configDirectory) {
        LOGGER.info("Setting up Loading Backgrounds...");
        config = LoadingBackgroundsConfig.read(configDirectory);
    }

    public static LoadingBackgroundsScreen getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("""
                    Tried to access LoadingBackgroundsScreen instance before it was initialized

                    This usually happens when one of the possible loading screens
                    attempts to render before the mod initialization step finishes.

                    This is probably a conflict with another mod!
                """);
        }

        return INSTANCE;
    }

    public static boolean isLoadingMessage(@Nullable final Component message) {
        if (message != null && message.getContents() instanceof TranslatableContents contents) {
            return LOADING_MESSAGE_TRANSLATION_KEYS.contains(contents.getKey());
        } else {
            return false;
        }
    }

    public DrawStatus draw(final GuiGraphics graphics, final Screen screen, final boolean shouldDrawDefaultBackground) {
        final double secondsNow = secondsSinceStart();
        double secondsDiff = secondsNow - this.stateSecondsStarted;
        if (secondsDiff > Math.max(this.config.secondsStay(), this.config.secondsFade()) + 5.0D || this.textures == null) {
            secondsDiff = 0.0D;
            this.stateSecondsStarted = secondsNow;
            this.stateIsFading = false;
            this.textures = this.getBackgroundTextures();
            if (textures == null) {
                return shouldDrawDefaultBackground ? DrawStatus.FALLBACK : DrawStatus.FAILED;
            }

            this.texturePrevious = this.textures.next();
            this.textureCurrent = this.textures.next();
        }

        boolean success;
        if (this.stateIsFading) {
            success = drawCustomBackground(graphics, screen, this.texturePrevious, this.config.brightness(), 1.0F);
            drawCustomBackground(graphics, screen, this.textureCurrent, this.config.brightness(), (float) Math.min(secondsDiff / this.config.secondsFade(), 1.0D));
            if (secondsDiff > this.config.secondsFade()) {
                this.stateSecondsStarted = secondsNow;
                this.stateIsFading = false;
            }
        } else {
            success = drawCustomBackground(graphics, screen, this.textureCurrent, this.config.brightness(), 1.0F);
            if (secondsDiff > this.config.secondsStay()) {
                this.stateSecondsStarted = secondsNow;
                this.stateIsFading = true;
                this.texturePrevious = this.textureCurrent;
                this.textureCurrent = this.textures.next();
            }
        }

        if (success) {
            return DrawStatus.SUCCESS;
        } else if (shouldDrawDefaultBackground) {
            return DrawStatus.FALLBACK;
        } else {
            return DrawStatus.FAILED;
        }
    }

    public boolean drawCustomBackground(final GuiGraphics graphics, final Screen screen, final ResourceLocation texture, final float brightness, final float opacity) {
        this.initFromScreen(screen);
        if (texture == null || texture.equals(MissingTextureAtlasSprite.getLocation())) {
            System.out.println("Failed to draw texture");
            return false;
        }

        final TextureInfo textureInfo = (TextureInfo) OmniClient.getTextureManager().getTexture(texture);
        textureInfo.loadingbackgrounds$init();

        float textureWidth = textureInfo.loadingbackgrounds$getWidth();
        float textureHeight = textureInfo.loadingbackgrounds$getHeight();
        if (textureWidth <= 0 || textureHeight <= 0) {
            System.out.println("Failed to draw texture: Texture size is <=0");
            return false;
        } else {
            final OmniRenderingContext context = OmniRenderingContext.from(graphics);
            ImmediateScreenRenderer.render(context, () -> {
                // Calculate scale factors
                float scaleX = screen.width / textureWidth;
                float scaleY = screen.height / textureHeight;

                // Check if the texture aspect ratio matches the screen aspect ratio
                if (scaleX < scaleY) {
                    // The texture is wider than the screen, so we need to adjust the scale and offset
                    scaleX = scaleY;
                } else {
                    // The texture is taller than the screen or has the same aspect ratio, so we adjust the scale and offset accordingly
                    scaleY = scaleX;
                }

                final OmniColor color = OmniColor.rgba((int) (brightness * 255), (int) (brightness * 255), (int) (brightness * 255), (int) (opacity * 255));
                context.renderTexture(
                    OmniRenderPipelines.TEXTURED,
                    texture,
                    0, 0,
                    screen.width, screen.height,
                    0, 0,
                    (int) (textureWidth * scaleX),
                    (int) (textureHeight * scaleY),
                    color
                );
            });

            return true;
        }
    }

    private static double secondsSinceStart() {
        return (double) (System.nanoTime() - secondsStart) * 1.0E-9D;
    }

    private Map<ResourceLocation, Resource> getBackgroundTextureResources() {
        return OmniClient.getResourceManager().listResources("textures/gui/backgrounds", (filename) -> filename.getPath().endsWith(".png"));
    }

    private Iterator<ResourceLocation> getBackgroundTextures() {
        var resources = this.getBackgroundTextureResources();
        if (resources.isEmpty()) {
            resources = this.getBackgroundTextureResources();
            if (resources.isEmpty()) {
                return null;
            }
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        final List<ResourceLocation> textures = (List) Arrays.asList(resources.keySet().toArray());
        Collections.shuffle(textures);
        return Iterators.cycle(textures);
    }

    public @NotNull Position getPosition() {
        return this.config.position();
    }
}
