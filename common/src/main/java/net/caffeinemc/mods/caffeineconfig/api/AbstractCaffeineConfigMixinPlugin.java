package net.caffeinemc.mods.caffeineconfig.api;

import net.caffeinemc.mods.caffeineconfig.config.CaffeineConfig;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * An abstract Mixin configuration plugin class for integration with {@link CaffeineConfig}.
 * This class implements the {@link IMixinConfigPlugin} interface, providing basic functionality
 * for Mixin plugins and allowing configuration management through {@link CaffeineConfig}.
 *
 * <p>Subclasses must implement the {@link #createConfig()} method to provide a specific configuration object.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * public class MixinPlugin extends AbstractCaffeineConfigMixinPlugin {
 *     // Use the extended abstract class to apply CaffeineConfig
 *     @Override
 *     protected CaffeineConfig createConfig() {
 *         return CaffeineConfig.builder("Test", "test", MixinPlugin.class)
 *                 .addMixinOption("mixin.test1", true)
 *                 .addOptionDependency("mixin.test1", "mixin.test2", true)
 *                 .withDefaultPropertiesPath("/assets/test/default.properties")
 *                 .withDependenciesPath("/assets/test/dependencies.properties")
 *                 .withInfoUrl("www.test.com")
 *                 .withMixinPackageRoot("com.example.fabric.mixin")
 *                 .buildOrThrow("./config/test.properties");
 *     }
 * }
 * }
 * </pre>
 */
public abstract class AbstractCaffeineConfigMixinPlugin implements IMixinConfigPlugin {
    private CaffeineMixinPlugin plugin;

    /**
     * Creates a {@link CaffeineConfig} object.
     * Subclasses must implement this method to provide a specific configuration.
     *
     * @return A {@link CaffeineConfig} instance.
     */
    protected abstract CaffeineConfig createConfig();

    /**
     * Determines whether debug mode is enabled.
     * Subclasses can override this method to enable debug logging.
     *
     * @return {@code true} if debug mode is enabled, otherwise {@code false}(default).
     */
    protected boolean enableDebug() {
        return false;
    }

    @Override
    public void onLoad(String mixinPackage) {
        this.plugin = CaffeineMixinPlugin.init(createConfig());
        this.plugin.setEnableDebug(this.enableDebug());
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return this.plugin.shouldApplyMixin(targetClassName, mixinClassName);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
