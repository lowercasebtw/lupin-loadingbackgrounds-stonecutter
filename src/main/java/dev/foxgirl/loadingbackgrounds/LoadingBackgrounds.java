package dev.foxgirl.loadingbackgrounds;

import com.google.common.collect.Iterators;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.foxgirl.loadingbackgrounds.util.LoadingBackgroundsConfig;
import dev.foxgirl.loadingbackgrounds.util.Position;
import dev.foxgirl.loadingbackgrounds.util.TextureInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
//? >=1.21.2
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public final class LoadingBackgrounds extends Screen {
    private static final Logger LOGGER = LogManager.getLogger(LoadingBackgrounds.class);
    private static LoadingBackgrounds INSTANCE;

    public static LoadingBackgrounds getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("""
                    Tried to access LoadingBackgroundsImpl instance before it was initialized

                    This usually happens when one of the possible loading screens
                    attempts to render before the mod initialization step finishes.

                    This is probably a conflict with another mod!
                """);
        }

        return INSTANCE;
    }

    private LoadingBackgroundsConfig config = LoadingBackgroundsConfig.DEFAULT;

    public LoadingBackgrounds() {
        super(Component.empty());
        INSTANCE = this;
    }

    public void init(@NotNull Path configDirectory) {
        LOGGER.info("Setting up Loading Backgrounds...");
        config = LoadingBackgroundsConfig.read(configDirectory);
    }

    private static final Set<String> loadingMessageTranslationKeys = new HashSet<>(Arrays.asList(new String[]{
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
        "download.pack.title",
    }));

    public static boolean isLoadingMessage(@Nullable Component message) {
        if (message != null && message.getContents() instanceof TranslatableContents contents) {
            return loadingMessageTranslationKeys.contains(contents.getKey());
        } else {
            return false;
        }
    }

    private Iterator<ResourceLocation> textures;
    private ResourceLocation texturePrevious;
    private ResourceLocation textureCurrent;
    private double stateSecondsStarted = seconds();
    private boolean stateIsFading = false;

    public @NotNull Position getPosition() {
        return config.position();
    }

    private void initFromScreen(Screen screen) {
        minecraft = getClient();
        width = screen.width;
        height = screen.height;
    }

    public boolean draw(final GuiGraphics graphics, final Screen screen, final boolean shouldDrawDefaultBackground) {
        double secondsNow = seconds();
        double secondsDiff = secondsNow - stateSecondsStarted;
        if (secondsDiff > Math.max(config.secondsStay(), config.secondsFade()) + 5.0D || textures == null) {
            secondsDiff = 0.0D;
            stateSecondsStarted = secondsNow;
            stateIsFading = false;
            textures = getBackgroundTextures();
            if (textures == null) {
                if (shouldDrawDefaultBackground) {
                    drawDefaultBackground(graphics, screen);
                }
                return false;
            }

            texturePrevious = textures.next();
            textureCurrent = textures.next();
        }

        boolean success;
        if (stateIsFading) {
            success = drawCustomBackground(graphics, screen, texturePrevious, config.brightness(), 1.0F);
            drawCustomBackground(graphics, screen, textureCurrent, config.brightness(), (float) Math.min(secondsDiff / config.secondsFade(), 1.0D));
            if (secondsDiff > config.secondsFade()) {
                stateSecondsStarted = secondsNow;
                stateIsFading = false;
            }
        } else {
            success = drawCustomBackground(graphics, screen, textureCurrent, config.brightness(), 1.0F);
            if (secondsDiff > config.secondsStay()) {
                stateSecondsStarted = secondsNow;
                stateIsFading = true;
                texturePrevious = textureCurrent;
                textureCurrent = textures.next();
            }
        }

        if (!success && shouldDrawDefaultBackground) {
            drawDefaultBackground(graphics, screen);
        }

        return success;
    }

    public boolean drawCustomBackground(final GuiGraphics graphics, final Screen screen, final ResourceLocation texture, final float brightness, final float opacity) {
        initFromScreen(screen);
        if (texture == null || texture.equals(MissingTextureAtlasSprite.getLocation())) {
            return false;
        }

        final AbstractTexture abstractTexture = getTextureManager().getTexture(texture);
        if (abstractTexture == MissingTextureAtlasSprite.getTexture()) {
            return false;
        }

        final TextureInfo textureInfo = (TextureInfo) abstractTexture;
        textureInfo.loadingbackgrounds$init();

        float textureWidth = textureInfo.loadingbackgrounds$getWidth();
        float textureHeight = textureInfo.loadingbackgrounds$getHeight();
        if (textureWidth <= 0 || textureHeight <= 0) {
            return false;
        }

        float screenWidth = screen.width;
        float screenHeight = screen.height;

        float offsetX = 0.0F;
        float offsetY = 0.0F;

        // Calculate scale factors
        float scaleX = screenWidth / textureWidth;
        float scaleY = screenHeight / textureHeight;

        // Check if the texture aspect ratio matches the screen aspect ratio
        if (scaleX < scaleY) {
            // The texture is wider than the screen, so we need to adjust the scale and offset
            scaleX = scaleY;
            offsetX = 0.0F - ((screenWidth - (textureWidth * scaleX)) * 0.5F);
        } else {
            // The texture is taller than the screen or has the same aspect ratio, so we adjust the scale and offset accordingly
            scaleY = scaleX;
            offsetY = 0.0F - ((screenHeight - (textureHeight * scaleY)) * 0.5F);
        }

        RenderSystem.setShaderColor(brightness, brightness, brightness, opacity);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        graphics.blit(
            //? >=1.21.2
            RenderType::guiTextured,
            texture,
            0, 0,
            0, 0,
            (int) offsetX, (int) offsetY,
            (int) screenWidth, (int) screenHeight,
            (int) (textureWidth * scaleX), (int) (textureHeight * scaleY)
        );
        RenderSystem.disableBlend();
        return true;
    }

    public void drawDefaultBackground(final GuiGraphics graphics, final Screen screen) {
        this.initFromScreen(screen);
        this.drawDefaultBackgroundActual(graphics, screen);
    }

    /* Implementation for ~~1.20.5~~ 1.21.0 and higher */
    private void drawDefaultBackgroundActual(final GuiGraphics graphics, final Screen screen) {
        /*float delta = client.getRenderTickCounter().getLastDuration();
        if (client.world == null) {
            renderPanoramaBackground(graphics, delta);
        }
        applyBlur(delta);
        renderDarkening(graphics);*/
    }

    /* Implementation for 1.20.4 and lower
    private void drawDefaultBackgroundActual(DrawContext graphics, Screen screen) {
        graphics.setShaderColor(0.25F, 0.25F, 0.25F, 1.0F);
        graphics.drawTexture(OPTIONS_BACKGROUND_TEXTURE, 0, 0, 0, 0.0F, 0.0F, width, height, 32, 32);
        graphics.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    } */

    private static final long secondsStart = System.nanoTime();

    private static double seconds() {
        return (double) (System.nanoTime() - secondsStart) * 1.0E-9D;
    }

    private static Minecraft getClient() {
        return Minecraft.getInstance();
    }

    private static ResourceManager getResourceManager() {
        return getClient().getResourceManager();
    }

    private static PackRepository getResourcePackManager() {
        return getClient().getResourcePackRepository();
    }

    private static TextureManager getTextureManager() {
        return getClient().getTextureManager();
    }

    private static final Pattern PROFILE_NAME_PATTERN =
        Pattern.compile("load(ing)?[\\W_-]{0,3}(background|image|pic)", Pattern.CASE_INSENSITIVE);

    private static String getProfileID(Pack profile) {
        //? >=1.20.5 {
        return profile.getId();
        //?} else {
        /*return profile.getName();
         *///?}
    }

    private static boolean matchesProfileNamePattern(String name) {
        return PROFILE_NAME_PATTERN.matcher(name).find();
    }

    private static boolean matchesProfileNamePattern(Pack profile) {
        return matchesProfileNamePattern(getProfileID(profile)) || matchesProfileNamePattern(profile.getTitle().getString());
    }

    private Map<ResourceLocation, Resource> getBackgroundTextureResources() {
        return getResourceManager().listResources("textures/gui/backgrounds", (filename) -> filename.getPath().endsWith(".png"));
    }

    private Iterator<ResourceLocation> getBackgroundTextures() {
        var resources = getBackgroundTextureResources();
        if (resources.isEmpty()) {
            resources = getBackgroundTextureResources();
            if (resources.isEmpty()) {
                return null;
            }
        }

        @SuppressWarnings({"unchecked", "rawtypes"}) final List<ResourceLocation> textures = (List) Arrays.asList(resources.keySet().toArray());
        Collections.shuffle(textures);
        return Iterators.cycle(textures);
    }
}
