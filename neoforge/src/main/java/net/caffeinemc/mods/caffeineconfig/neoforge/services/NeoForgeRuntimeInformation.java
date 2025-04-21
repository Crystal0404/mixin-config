package net.caffeinemc.mods.caffeineconfig.neoforge.services;

import net.caffeinemc.mods.caffeineconfig.services.PlatformRuntimeInformation;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModInfo;

import java.util.NoSuchElementException;
import java.util.Objects;

public class NeoForgeRuntimeInformation implements PlatformRuntimeInformation {
    @Override
    public PlatformModInfo getModInfoById(String modId) {
        for (ModInfo modInfo : FMLLoader.getLoadingModList().getMods()) {
            if (Objects.equals(modInfo.getModId(), modId)) {
                return new PlatformModInfo(modInfo.getDisplayName(), modId, modInfo.getVersion().toString());
            }
        }
        throw new NoSuchElementException();
    }
}
