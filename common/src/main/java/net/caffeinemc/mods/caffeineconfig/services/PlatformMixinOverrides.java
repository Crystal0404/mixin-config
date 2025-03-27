package net.caffeinemc.mods.caffeineconfig.services;

import java.util.List;

public interface PlatformMixinOverrides {
    PlatformMixinOverrides INSTANCE =  Services.load(PlatformMixinOverrides.class);

    static PlatformMixinOverrides getInstance() {
        return INSTANCE;
    }

    List<MixinOverride> applyModOverrides(String name, String id);

    record MixinOverride(String modId, String option, boolean enabled) {

    }
}
