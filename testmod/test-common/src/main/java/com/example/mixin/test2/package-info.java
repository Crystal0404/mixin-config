@MixinConfigOption(
        description = "this is test2!",
        enabled = false,
        depends = @MixinConfigDependency(dependencyPath = "mixin.test3")
)
package com.example.mixin.test2;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;