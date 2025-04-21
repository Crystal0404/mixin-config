package net.caffeinemc.mods.caffeineconfig.api;

import net.caffeinemc.mods.caffeineconfig.config.CaffeineConfig;
import net.caffeinemc.mods.caffeineconfig.config.Option;

/**
 * The {@code CaffeineMixinPlugin} class is the core class for managing Mixin application logic.
 * It determines whether a specific Mixin should be applied based on the {@link CaffeineConfig} configuration
 * and supports debug mode for detailed logging.
 *
 * <p>This class is typically used by {@link AbstractCaffeineConfigMixinPlugin}, and developers do not need to instantiate it directly.</p>
 *
 * <p>If you cannot extend from {@link AbstractCaffeineConfigMixinPlugin} for some reason,
 * refer to the following example to apply {@link CaffeineConfig} directly</p>
 *
 *  <pre>
 *  {@code
 * public class MixinPlugin implements IMixinConfigPlugin {
 *     private final CaffeineConfig config =
 *             CaffeineConfig.builder("test", MixinPlugin.class)
 *                     .addMixinOption("mixin.test1", true)
 *                     .addOptionDependency("mixin.test1", "mixin.test2", true)
 *                     .withDefaultPropertiesPath("/assets/test/default.properties")
 *                     .withDependenciesPath("/assets/test/dependencies.properties")
 *                     .withInfoUrl("www.test.com")
 *                     .withMixinPackageRoot("com.example.neoforge.mixin.")
 *                     .buildOrThrow("./config/test.properties");
 *
 *     private final CaffeineMixinPlugin plugin = CaffeineMixinPlugin.init(config);
 *
 *     @Override
 *     public void onLoad(String s) {
 *         this.plugin.setEnableDebug(true); // if you don't need debugging, you can ignore it :)
 *     }
 *
 *     @Override
 *     public boolean shouldApplyMixin(String s, String s1) {
 *         return this.plugin.shouldApplyMixin(s, s1); // VERY IMPORTANT!!!
 *     }
 *     // .....
 *  }
 *  <pre/>
 */
public class CaffeineMixinPlugin {
    private boolean enableDebug = false;
    private final CaffeineConfig config;

    private CaffeineMixinPlugin(CaffeineConfig config) {
        this.config = config;
    }

    /**
     * Initializes a {@link CaffeineMixinPlugin} instance.
     *
     * @param config The associated {@link CaffeineConfig} configuration object.
     * @return A new {@link CaffeineMixinPlugin} instance.
     */
    public static CaffeineMixinPlugin init(CaffeineConfig config) {
        config.getLogger().info("Loaded configuration file for {}: {} options available, {} override(s) found",
                config.getModName(), config.getOptionCount(), config.getOptionOverrideCount());
        return new CaffeineMixinPlugin(config);
    }

    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (enableDebug) {
            config.getLogger().info("Checking mixin '{}' for target '{}'", mixinClassName, targetClassName);
        }
        String mixin = null;
        for (String root : config.getMixinPackageRoot()) {
            if (mixinClassName.startsWith(root)) {
                mixin = mixinClassName.substring(root.length());
                break;
            }
        }
        if (mixin == null) {
            config.getLogger().error("Expected mixin '{}' to start with any of these package roots '{}', treating as foreign and " +
                    "disabling!", mixinClassName, config.getMixinPackageRoot());
            return false;
        }

        Option option = config.getEffectiveOptionForMixin(mixin);

        if (option == null) {
            config.getLogger().error("No rules matched mixin '{}', treating as foreign and disabling!", mixin);

            return false;
        }

        if (option.isOverridden()) {
            String source = "[unknown]";

            if (option.isUserDefined()) {
                source = "user configuration";
            } else if (option.isModDefined()) {
                source = "mods [" + String.join(", ", option.getDefiningMods()) + "]";
            }

            if (option.isEnabled()) {
                config.getLogger().info("Force-enabling mixin '{}' as rule '{}' (added by {}) enables it", mixin,
                        option.getName(), source);
            } else {
                config.getLogger().info("Force-disabling mixin '{}' as rule '{}' (added by {}) disables it and children", mixin,
                        option.getName(), source);
            }
        }

        boolean enabled = option.isEnabled();
        if (enableDebug) {
            if (!enabled) {
                config.getLogger().info("Disabling mixin '{}' due to rule '{}'", mixin, option.getName());
            } else {
                config.getLogger().info("Enabling mixin '{}' due to rule '{}'", mixin, option.getName());
            }
        }
        return enabled;
    }

    public boolean isEnableDebug() {
        return enableDebug;
    }

    public void setEnableDebug(boolean enableDebug) {
        this.enableDebug = enableDebug;
    }

    public CaffeineConfig getConfig() {
        return config;
    }
}
