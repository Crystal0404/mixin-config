package net.caffeinemc.mods.caffeineconfig.services;

import net.caffeinemc.mods.caffeineconfig.config.CaffeineConfig;

import java.util.List;

public interface PlatformMixinOverrides {
    PlatformMixinOverrides INSTANCE =  Services.load(PlatformMixinOverrides.class);

    static PlatformMixinOverrides getInstance() {
        return INSTANCE;
    }

    List<MixinOverride> applyModOverrides(CaffeineConfig config);

    record MixinOverride(String modId, String option, boolean enabled) {

    }
}
