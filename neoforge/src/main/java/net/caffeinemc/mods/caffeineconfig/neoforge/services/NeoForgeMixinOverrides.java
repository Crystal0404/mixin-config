package net.caffeinemc.mods.caffeineconfig.neoforge.services;

import net.caffeinemc.mods.caffeineconfig.config.CaffeineConfig;
import net.caffeinemc.mods.caffeineconfig.services.PlatformMixinOverrides;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NeoForgeMixinOverrides implements PlatformMixinOverrides {
    @Override
    public List<MixinOverride> applyModOverrides(CaffeineConfig config) {
        String JSON_KEY_OPTIONS = config.getId() + ":options";
        List<MixinOverride> list = new ArrayList<>();

        for (ModInfo meta : FMLLoader.getLoadingModList().getMods()) {
            meta.getOwningFile().getConfigElement(JSON_KEY_OPTIONS).ifPresent(override -> {
                if (override instanceof Map<?, ?> overrides && overrides.keySet().stream().allMatch(key -> key instanceof String)) {
                    overrides.forEach((key, value) -> {
                        if (!(value instanceof Boolean) || !(key instanceof String)) {
                            config.getLogger().info(
                                    "[{}] Mod '{}' attempted to override option '{}' with an invalid value, ignoring",
                                    config.getModName(),
                                    meta.getModId(),
                                    key
                            );
                            return;
                        }

                        list.add(new MixinOverride(meta.getModId(), (String) key, (Boolean) value));
                    });
                } else {
                    config.getLogger().info(
                            "[{}] '{}' contains invalid {} option overrides, ignoring",
                            config.getModName(),
                            config.getModName(),
                            meta.getModId()
                    );
                }
            });
        }

        return list;
    }
}
