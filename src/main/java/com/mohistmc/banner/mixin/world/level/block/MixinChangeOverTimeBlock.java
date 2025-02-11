package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_20_R2.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChangeOverTimeBlock.class)
public interface MixinChangeOverTimeBlock<T extends Enum<T>> {

    @Redirect(method = "method_34726", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private static boolean banner$blockFrom(ServerLevel instance, BlockPos pos, BlockState state) {
        return CraftEventFactory.handleBlockFormEvent(instance, pos, state); // CraftBukkit;
    }
}
