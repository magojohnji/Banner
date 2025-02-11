package com.mohistmc.banner.mixin.world.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.effect.HungerMobEffect")

public class MixinHungerMobEffect {

    @Inject(method = "applyEffectTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"))
    private void banner$reason(LivingEntity livingEntity, int amplifier, CallbackInfo ci) {
        ((ServerPlayer) livingEntity).pushExhaustReason(EntityExhaustionEvent.ExhaustionReason.HUNGER_EFFECT);
    }
}
