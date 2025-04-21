package net.caffeinemc.mods.caffeineconfig.config;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.caffeinemc.mods.caffeineconfig.CaffeineConfigMod;
import net.caffeinemc.mods.caffeineconfig.services.PlatformMixinOverrides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class CaffeineConfig {
    private final Logger logger;
    private final String name;
    private final String[] mixinPackageRoot;
    private final Map<String, Option> options = new HashMap<>();
    private final Set<Option> optionsWithDependencies = new ObjectLinkedOpenHashSet<>();

    public String[] getMixinPackageRoot() {
        return mixinPackageRoot;
    }

    public String getModName() {
        return this.name;
    }

    public Logger getLogger() {
        return this.logger;
    }

    private CaffeineConfig(Builder builder) {
        this.logger = LoggerFactory.getLogger(builder.name);
        this.name = builder.name;
        this.mixinPackageRoot = builder.mixinPackageRoot;

        InputStream defaultPropertiesStream = builder.defaultPropertiesStream;
        if (defaultPropertiesStream != null) {
            try (BufferedReader propertiesReader = new BufferedReader(new InputStreamReader(defaultPropertiesStream))) {
                Properties properties = new Properties();
                properties.load(propertiesReader);
                properties.forEach((ruleName, enabled) -> this.addMixinRule((String) ruleName, Boolean.parseBoolean((String) enabled)));
            } catch (IOException e) {
                this.logger.error(String.valueOf(e));
                throw new IllegalStateException(builder.name + " mixin config default properties could not be read!");
            }
        }
        builder.mixinOptionMap.forEach(this::addMixinRule);

        InputStream dependenciesStream = builder.dependenciesStream;
        if (dependenciesStream != null) {
            try (BufferedReader propertiesReader = new BufferedReader(new InputStreamReader(dependenciesStream))) {
                Properties properties = new Properties();
                properties.load(propertiesReader);
                properties.forEach(
                        (o1, o2) -> {
                            String rulename = (String) o1;
                            String dependencies = (String) o2;
                            String[] dependenciesSplit = dependencies.split(",");
                            for (String dependency : dependenciesSplit) {
                                String[] split = dependency.split(":");
                                if (split.length != 2) {
                                    return;
                                }
                                String dependencyName = split[0];
                                String requiredState = split[1];
                                this.addRuleDependency(rulename, dependencyName, Boolean.parseBoolean(requiredState));
                            }
                        }
                );
            } catch (IOException e) {
                this.logger.error(String.valueOf(e));
                throw new IllegalStateException(builder.name + " mixin config dependencies could not be read!");
            }
        }
        builder.mixinDependencyList.forEach((it) -> this.addRuleDependency(it.rulename, it.dependencyName, it.requiredState));
    }

    /**
     * Defines a dependency between two registered mixin rules. If a dependency is not satisfied, the mixin will
     * be disabled.
     *
     * @param rule          the mixin rule that requires another rule to be set to a given value
     * @param dependency    the mixin rule the given rule depends on
     * @param requiredValue the required value of the dependency
     */
    @SuppressWarnings("SameParameterValue")
    private void addRuleDependency(String rule, String dependency, boolean requiredValue) {
        Option option = this.options.get(rule);
        if (option == null) {
            this.logger.error("Option {} for dependency '{} depends on {}={}' not found. Skipping.", rule, rule, dependency, requiredValue);
            return;
        }
        Option dependencyOption = this.options.get(dependency);
        if (dependencyOption == null) {
            this.logger.error("Option {} for dependency '{} depends on {}={}' not found. Skipping.", dependency, rule, dependency, requiredValue);
            return;
        }
        option.addDependency(dependencyOption, requiredValue);
        this.optionsWithDependencies.add(option);
    }

    /**
     * Defines a Mixin rule which can be configured by users and other mods.
     *
     * @param mixin   The name of the mixin package which will be controlled by this rule
     * @param enabled True if the rule will be enabled by default, otherwise false
     * @throws IllegalStateException If a rule with that name already exists
     */
    private void addMixinRule(String mixin, boolean enabled) {
        if (this.options.putIfAbsent(mixin, new Option(mixin, enabled, false)) != null) {
            throw new IllegalStateException("Mixin rule already defined: " + mixin);
        }
    }

    private void readProperties(Properties props) {
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            Option option = this.options.get(key);

            if (option == null) {
                this.logger.warn("No configuration key exists with name '{}', ignoring", key);
                continue;
            }

            boolean enabled;

            if (value.equalsIgnoreCase("true")) {
                enabled = true;
            } else if (value.equalsIgnoreCase("false")) {
                enabled = false;
            } else {
                this.logger.warn("Invalid value '{}' encountered for configuration key '{}', ignoring", value, key);
                continue;
            }

            option.setEnabled(enabled, true);
        }
    }

    protected void applyModOverride(PlatformMixinOverrides.MixinOverride override) {
        Option option = this.options.get(override.option());

        if (option == null && !override.option().startsWith("mixin.")) {
            option = this.options.get("mixin." + override.option());
        }

        if (option == null) {
            this.logger.warn("Mod '{}' attempted to override option '{}', which doesn't exist, ignoring", override.modId(), override.option());
            return;
        }

        // disabling the option takes precedence over enabling
        if (!override.enabled() && option.isEnabled()) {
            option.clearModsDefiningValue();
        }

        if (!override.enabled() || option.isEnabled() || option.getDefiningMods().isEmpty()) {
            option.addModOverride(override.enabled(), override.modId());
        }
    }

    /**
     * Returns the effective option for the specified class name. This traverses the package path of the given mixin
     * and checks each root for configuration rules. If a configuration rule disables a package, all mixins located in
     * that package and its children will be disabled. The effective option is that of the highest-priority rule, either
     * a enable rule at the end of the chain or a disable rule at the earliest point in the chain.
     *
     * @return Null if no options matched the given mixin name, otherwise the effective option for this Mixin
     */
    public Option getEffectiveOptionForMixin(String mixinClassName) {
        int lastSplit = 0;
        int nextSplit;

        Option rule = null;

        while ((nextSplit = mixinClassName.indexOf('.', lastSplit)) != -1) {
            String key = getMixinRuleName(mixinClassName.substring(0, nextSplit));

            Option candidate = this.options.get(key);

            if (candidate != null) {
                rule = candidate;

                if (!rule.isEnabled()) {
                    return rule;
                }
            }

            lastSplit = nextSplit + 1;
        }

        return rule;
    }

    /**
     * Tests all dependencies and disables options when their dependencies are not met.
     */
    private void applyDependencies() {
        // Check dependencies several times, because one iteration may disable a rule required by another rule
        // This terminates because each additional iteration will disable one or more rules, and there is only a finite number of rules
        //noinspection StatementWithEmptyBody
        while (this.applyDependenciesOnce()) {
        }
    }

    private boolean applyDependenciesOnce() {
        boolean changed = false;
        for (Option optionWithDependency : this.optionsWithDependencies) {
            changed |= optionWithDependency.disableIfDependenciesNotMet(this.logger, this);
        }
        return changed;
    }

    private static void writeDefaultConfig(File file, String modName, String url) throws IOException {
        File dir = file.getParentFile();

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Could not create parent directories");
            }
        } else if (!dir.isDirectory()) {
            throw new IOException("The parent file is not a directory");
        }

        try (Writer writer = new FileWriter(file)) {
            writer.write(String.format("# This is the configuration file for %s.\n", modName));
            writer.write("#\n");
            writer.write("# You can find information on editing this file and all the available options here:\n");
            writer.write(String.format("# %s\n", url));
            writer.write("#\n");
            writer.write("# By default, this file will be empty except for this notice.\n");
        }
    }

    private static String getMixinRuleName(String name) {
        return "mixin." + name;
    }

    public int getOptionCount() {
        return this.options.size();
    }

    public int getOptionOverrideCount() {
        return (int) this.options.values()
                .stream()
                .filter(Option::isOverridden)
                .count();
    }

    public Option getParent(Option option) {
        String optionName = option.getName();
        int split;

        if ((split = optionName.lastIndexOf('.')) != -1) {
            String key = optionName.substring(0, split);
            return this.options.get(key);

        }
        return null;
    }

    /**
     * Creates a {@link Builder} instance for constructing a {@link CaffeineConfig} object.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * CaffeineConfig config = CaffeineConfig.builder("Test", "test", MixinPlugin.class)
     *     .addMixinOption("mixin.test1", "mixin.test2", true)
     *     .addOptionDependency("mixin.example", "mixin.dependency", true)
     *     .withDefaultPropertiesPath("/assets/test/default.properties")
     *     .withDependenciesPath("/assets/test/dependencies.properties")
     *     .withInfoUrl("https://example.com")
     *     .withMixinPackageRoot("com.example.fabric.mixin.")
     *     .buildOrThrow("./config/test.properties");
     * }
     * </pre>
     *
     * @param name  Your mod name.
     * @param id    Your mod id.
     * @param clazz You mixin plugin class.
     * @return A new {@link Builder} instance.
     */
    public static Builder builder(String name, String id, Class<?> clazz) {
        return new Builder(name, id, clazz);
    }

    @SuppressWarnings("unused")
    public static class Builder {
        private String id;
        private String name;
        private Class<?> clazz;
        private String infoUrl;
        private InputStream defaultPropertiesStream;
        private InputStream dependenciesStream;
        private final Map<String, Boolean> mixinOptionMap = new HashMap<>();
        private final List<MixinDependency> mixinDependencyList = new ArrayList<>();
        private String[] mixinPackageRoot;

        private Builder() {}

        private Builder(String name, String id, Class<?> clazz) {
            this.name = name;
            this.id = id;
            this.clazz = clazz;
        }

        /**
         * <p>Defines a Mixin option which can be configured by users and other mods.</p>
         *
         * @param mixin   The name of the mixin package which will be controlled by this option
         * @param enable {@code true} if the option will be enabled by default, {@code false} otherwise
         */
        public Builder addMixinOption(String mixin, boolean enable) {
            this.mixinOptionMap.put(mixin, enable);
            return this;
        }

        /**
         * <p>Defines a dependency between two registered mixin options. If a dependency is not satisfied, the mixin will
         * be disabled.</p>
         *
         * @param rulename        the mixin option that requires another option to be set to a given value
         * @param dependencyName    the mixin option the given option depends on
         * @param requiredState the required value of the dependency
         */
        public Builder addOptionDependency(String rulename, String dependencyName, boolean requiredState) {
            this.mixinDependencyList.add(new MixinDependency(rulename, dependencyName, requiredState));
            return this;
        }

        private record MixinDependency(String rulename, String dependencyName, boolean requiredState) {
        }

        /**
         * Sets the path to the default properties file and loads it as an input stream.
         *
         * @param defaultPropertiesPath The path to the default properties file.
         * @return The current Builder instance for method chaining.
         */
        public Builder withDefaultPropertiesPath(String defaultPropertiesPath) {
            this.defaultPropertiesStream = this.clazz.getResourceAsStream(defaultPropertiesPath);
            return this;
        }

        /**
         * Sets the path to the dependencies file and loads it as an input stream.
         *
         * @param dependenciesPath The path to the dependencies file.
         * @return The current Builder instance for method chaining.
         */
        public Builder withDependenciesPath(String dependenciesPath) {
            this.dependenciesStream = this.clazz.getResourceAsStream(dependenciesPath);
            return this;
        }

        /**
         * Sets the info URL, which provides documentation or links about the Mixin.
         *
         * @param infoUrl The info URL.
         * @return The current Builder instance for method chaining.
         */
        public Builder withInfoUrl(String infoUrl) {
            this.infoUrl = infoUrl;
            return this;
        }

        /**
         * Sets the root package paths for Mixin.
         *
         * @param mixinPackageRoot The root package paths for Mixin.
         * @return The current Builder instance for method chaining.
         */
        public Builder withMixinPackageRoot(String... mixinPackageRoot) {
            this.mixinPackageRoot = mixinPackageRoot;
            return this;
        }

        /**
         * Builds a {@link CaffeineConfig} object. If the configuration file does not exist,
         * a default configuration file will be created.
         *
         * @param path The path to the configuration file.
         * @return The constructed {@link CaffeineConfig} object.
         * @throws NullPointerException If {@code infoUrl} or {@code mixinPackageRoot} is null.
         * @throws RuntimeException    If an error occurs while loading or writing the configuration file.
         */
        public CaffeineConfig buildOrThrow(String path) {
            Objects.requireNonNull(this.infoUrl, "infoUrl can not be null!");
            Objects.requireNonNull(this.mixinPackageRoot, "mixinPackageRoot can not be null!");

            CaffeineConfig config = new CaffeineConfig(this);
            File file = new File(path);
            if (file.exists()) {
                Properties props = new Properties();

                try (FileInputStream fin = new FileInputStream(file)) {
                    props.load(fin);
                } catch (IOException e) {
                    throw new RuntimeException("Could not load config file", e);
                }

                config.readProperties(props);

            } else {
                try {
                    writeDefaultConfig(file, this.name, this.infoUrl);
                } catch (IOException e) {
                    CaffeineConfigMod.LOGGER.warn("Could not write default configuration file", e);
                }
            }
            PlatformMixinOverrides.getInstance().applyModOverrides(this.name, this.id).forEach(config::applyModOverride);
            config.applyDependencies();

            return config;
        }
    }
}
