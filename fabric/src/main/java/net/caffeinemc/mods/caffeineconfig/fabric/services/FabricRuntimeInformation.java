package net.caffeinemc.mods.caffeineconfig.fabric.services;

import net.caffeinemc.mods.caffeineconfig.services.PlatformRuntimeInformation;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;

public class FabricRuntimeInformation implements PlatformRuntimeInformation {
    @Override
    public PlatformModInfo getModInfoById(String modId) {
        ModMetadata modMetadata = FabricLoader.getInstance().getModContainer(modId).orElseThrow().getMetadata();
        return new PlatformModInfo(modMetadata.getName(), modId, modMetadata.getVersion().getFriendlyString());
    }
}
