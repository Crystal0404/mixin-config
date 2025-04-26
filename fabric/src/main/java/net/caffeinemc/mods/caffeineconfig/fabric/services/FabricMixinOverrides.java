package net.caffeinemc.mods.caffeineconfig.fabric.services;

import net.caffeinemc.mods.caffeineconfig.config.CaffeineConfig;
import net.caffeinemc.mods.caffeineconfig.services.PlatformMixinOverrides;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FabricMixinOverrides implements PlatformMixinOverrides {
    @Override
    public List<MixinOverride> applyModOverrides(CaffeineConfig config) {
        String JSON_KEY_OPTIONS = config.getId() + ":options";
        List<MixinOverride> list = new ArrayList<>();

        for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
            ModMetadata meta = container.getMetadata();

            if (meta.containsCustomValue(JSON_KEY_OPTIONS)) {
                CustomValue overrides = meta.getCustomValue(JSON_KEY_OPTIONS);

                if (overrides.getType() != CustomValue.CvType.OBJECT) {
                    config.getLogger().info(
                            "[{}] Mod '{}' contains invalid {} option overrides, ignoring",
                            config.getModName(),
                            config.getModName(),
                            meta.getId()
                    );
                    continue;
                }

                for (Map.Entry<String, CustomValue> entry : overrides.getAsObject()) {
                    if (entry.getValue().getType() != CustomValue.CvType.BOOLEAN) {
                        config.getLogger().info(
                                "[{}] Mod '{}' attempted to override option '{}' with an invalid value, ignoring",
                                config.getModName(),
                                meta.getId(),
                                entry.getKey()
                        );
                        continue;
                    }

                    list.add(new MixinOverride(meta.getId(), entry.getKey(), entry.getValue().getAsBoolean()));
                }
            }
        }
        return list;
    }
}
