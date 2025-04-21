package net.caffeinemc.mods.caffeineconfig.services;

public interface PlatformRuntimeInformation {
    PlatformRuntimeInformation INSTANCE =  Services.load(PlatformRuntimeInformation.class);

    static PlatformRuntimeInformation getInstance() {
        return INSTANCE;
    }

    PlatformModInfo getModInfoById(String modId);

    record PlatformModInfo(String modName, String modId, String modVersion) {

    }
}
