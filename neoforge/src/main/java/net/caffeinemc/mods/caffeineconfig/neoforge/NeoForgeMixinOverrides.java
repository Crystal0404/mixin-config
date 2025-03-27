package net.caffeinemc.mods.caffeineconfig.neoforge;

import net.caffeinemc.mods.caffeineconfig.services.PlatformMixinOverrides;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NeoForgeMixinOverrides implements PlatformMixinOverrides {
    @Override
    public List<MixinOverride> applyModOverrides(String name, String id) {
        Logger logger = LoggerFactory.getLogger(name);
        String JSON_KEY_LITHIUM_OPTIONS = id + ":options";
        List<MixinOverride> list = new ArrayList<>();

        for (ModInfo meta : FMLLoader.getLoadingModList().getMods()) {
            meta.getOwningFile().getConfigElement(JSON_KEY_LITHIUM_OPTIONS).ifPresent(override -> {
                if (override instanceof Map<?, ?> overrides && overrides.keySet().stream().allMatch(key -> key instanceof String)) {
                    overrides.forEach((key, value) -> {
                        if (!(value instanceof Boolean) || !(key instanceof String)) {
                            logger.info("[{}] Mod '{}' attempted to override option '{}' with an invalid value, ignoring", name, meta.getModId(), key);
                            return;
                        }

                        list.add(new MixinOverride(meta.getModId(), (String) key, (Boolean) value));
                    });
                } else {
                    logger.info("[{}] '{}' contains invalid Lithium option overrides, ignoring", name, meta.getModId());
                }
            });
        }

        return list;
    }
}
