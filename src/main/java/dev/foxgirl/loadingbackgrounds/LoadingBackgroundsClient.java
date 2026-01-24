package dev.foxgirl.loadingbackgrounds;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class LoadingBackgroundsClient implements ClientModInitializer {
    /*public LoadingBackgroundsModNeoForge(IEventBus eventBus) {
        eventBus.addListener(this::onClientSetup);
    }*/

    /*public LoadingBackgroundsModForge() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::onClientSetup);
    }*/

    /*
    public void onClientSetup(FMLClientSetupEvent event) {
        LoadingBackgrounds.createInstance().init(FMLPaths.CONFIGDIR.get());
    }*/

    @Override
    public void onInitializeClient() {
        new LoadingBackgrounds().init(
            //? fabric {
            FabricLoader.getInstance().getConfigDir()
            //?} else {
            /*FMLPaths.CONFIGDIR.get()
             *///?}
        );
    }
}
