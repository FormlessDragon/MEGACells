package com.example.modid.mixin;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public class EntityLivingBaseMixin {

    @Inject(method = "setCustomNameTag", at = @At("HEAD"))
    private void example$setCustomNameTag(String name, CallbackInfo ci) {
    }
}
