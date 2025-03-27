package net.caffeinemc.mods.caffeineconfig.fabric;

import net.caffeinemc.mods.caffeineconfig.services.PlatformMixinOverrides;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FabricMixinOverrides implements PlatformMixinOverrides {
    @Override
    public List<MixinOverride> applyModOverrides(String name, String id) {
        Logger logger = LoggerFactory.getLogger(name);
        String JSON_KEY_LITHIUM_OPTIONS = id + ":options";
        List<MixinOverride> list = new ArrayList<>();

        for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
            ModMetadata meta = container.getMetadata();

            if (meta.containsCustomValue(JSON_KEY_LITHIUM_OPTIONS)) {
                CustomValue overrides = meta.getCustomValue(JSON_KEY_LITHIUM_OPTIONS);

                if (overrides.getType() != CustomValue.CvType.OBJECT) {
                    logger.info("[{}] Mod '{}' contains invalid Lithium option overrides, ignoring", name, meta.getId());
                    continue;
                }

                for (Map.Entry<String, CustomValue> entry : overrides.getAsObject()) {
                    if (entry.getValue().getType() != CustomValue.CvType.BOOLEAN) {
                        logger.info("[{}] Mod '{}' attempted to override option '{}' with an invalid value, ignoring", name, meta.getId(), entry.getKey());
                        continue;
                    }

                    list.add(new MixinOverride(meta.getId(), entry.getKey(), entry.getValue().getAsBoolean()));
                }
            }
        }
        return list;
    }
}
