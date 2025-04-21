package com.example.mixin.test1;

import com.example.ExampleMod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class TestMixin1 {
    @Inject(
            method = "loadWorld",
            at = @At("HEAD")
    )
    private void mixin1(CallbackInfo ci) {
        ExampleMod.LOGGER.info("mixin1");
    }
}
