package com.example.fabric;

import net.caffeinemc.mods.caffeineconfig.api.AbstractCaffeineConfigMixinPlugin;
import net.caffeinemc.mods.caffeineconfig.config.CaffeineConfig;

public class MixinPlugin extends AbstractCaffeineConfigMixinPlugin {
    // Use the extended abstract class to apply CaffeineConfig
    @Override
    protected CaffeineConfig createConfig() {
        return CaffeineConfig.builder("Test", "test", MixinPlugin.class)
                .addMixinOption("mixin.test1", true)
                .addOptionDependency("mixin.test1", "mixin.test2", true)
                .withDefaultPropertiesPath("/assets/test/default.properties")
                .withDependenciesPath("/assets/test/dependencies.properties")
                .withInfoUrl("www.test.com")
                .withMixinPackageRoot("com.example.fabric.mixin.")
                .buildOrThrow("./config/test.properties");
    }

    @Override
    protected boolean enableDebug() {
        return true;
    }
}
