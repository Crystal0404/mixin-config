package com.example;

import net.caffeinemc.mods.caffeineconfig.api.AbstractCaffeineConfigMixinPlugin;
import net.caffeinemc.mods.caffeineconfig.config.CaffeineConfig;

public class MixinPlugin extends AbstractCaffeineConfigMixinPlugin {
    @Override
    protected CaffeineConfig createConfig() {
        return CaffeineConfig.builder(ExampleMod.MOD_ID, MixinPlugin.class)
                .addMixinOption("mixin.test1", true)
                .addOptionDependency("mixin.test1", "mixin.test2", true)
                .withDefaultPropertiesPath("/assets/example/examplemod-mixin-config-default.properties")
                .withDependenciesPath("/assets/example/examplemod-mixin-config-dependencies.properties")
                .withInfoUrl("www.example.com")
                .withMixinPackageRoot("com.example.mixin")
                .buildOrThrow("./config/example.properties");
    }

    // If you don't need to print the debug information, you can ignore it
    @Override
    protected boolean enableDebug() {
        return true;
    }
}
