package com.example.neoforge;

import net.caffeinemc.mods.caffeineconfig.api.CaffeineMixinPlugin;
import net.caffeinemc.mods.caffeineconfig.config.CaffeineConfig;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
    // If, for some reason, you can't extend from "AbstractCaffeineConfigMixinPlugin"
    // you can use this method to apply CaffeineConfig
    private final CaffeineConfig config =
            CaffeineConfig.builder("Test", "test", MixinPlugin.class)
                    .addMixinOption("mixin.test1", true)
                    .addOptionDependency("mixin.test1", "mixin.test2", true)
                    .withDefaultPropertiesPath("/assets/test/default.properties")
                    .withDependenciesPath("/assets/test/dependencies.properties")
                    .withInfoUrl("www.test.com")
                    .withMixinPackageRoot("com.example.neoforge.mixin.")
                    .buildOrThrow("./config/test.properties");

    private final CaffeineMixinPlugin plugin = CaffeineMixinPlugin.init(config);

    @Override
    public void onLoad(String s) {
        this.plugin.setEnableDebug(true); // if you don't need debugging, you can ignore it :)
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String s, String s1) {
        return this.plugin.shouldApplyMixin(s, s1); // don t forget about it
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }

    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }
}
