package com.odtheking.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(net.minecraft.client.KeyMapping.class)
public interface KeyMappingAccessor {
    @Accessor("key")
    com.mojang.blaze3d.platform.InputConstants.Key getKey();
}
