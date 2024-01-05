package com.mohistmc.banner.mixin.world.level.block.entity;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.block.CampfireStartEvent;
import org.bukkit.inventory.CampfireRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CampfireBlockEntity.class)
public abstract class MixinCampfireBlockEntity extends BlockEntity {

    @Shadow @Final private RecipeManager.CachedCheck<Container, CampfireCookingRecipe> quickCheck;
    @Shadow @Final public int[] cookingTime;

    @Shadow public abstract Optional<RecipeHolder<CampfireCookingRecipe>> getCookableRecipe(ItemStack stack);

    public MixinCampfireBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }


    // Banner - fix mixin(locals = LocalCapture.CAPTURE_FAILSOFT)
    private static Optional<RecipeHolder<CampfireCookingRecipe>> recipe;
    private static CraftItemStack source;
    private static  org.bukkit.inventory.ItemStack result;
    private static BlockCookEvent blockCookEvent;

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public static void cookTick(Level world, BlockPos blockposition, BlockState iblockdata, CampfireBlockEntity tileentitycampfire) {
        boolean flag = false;

        for (int i = 0; i < tileentitycampfire.getItems().size(); ++i) {
            ItemStack itemstack = tileentitycampfire.getItems().get(i);

            if (!itemstack.isEmpty()) {
                flag = true;

                if (tileentitycampfire.cookingProgress[i] >= tileentitycampfire.cookingTime[i]) {
                    Container inventorysubcontainer = new SimpleContainer(itemstack);
                    recipe = ((MixinCampfireBlockEntity) (Object) tileentitycampfire).quickCheck.getRecipeFor(inventorysubcontainer, world);
                    ItemStack itemStack2 = recipe.map((recipecampfire) -> {
                        // Paper end
                        return recipecampfire.value().assemble(inventorysubcontainer, world.registryAccess());
                    }).orElse(itemstack);

                    if (itemStack2.isItemEnabled(world.enabledFeatures())) {
                        // CraftBukkit start - fire BlockCookEvent
                        source = CraftItemStack.asCraftMirror(itemstack);
                        result = CraftItemStack.asBukkitCopy(itemStack2);

                        blockCookEvent = new BlockCookEvent(CraftBlock.at(world, blockposition), source, result, (org.bukkit.inventory.CookingRecipe<?>) recipe.map(cookingRecipeHolder -> {
                            return cookingRecipeHolder.toBukkitRecipe();
                        }).orElse(null)); // Paper
                        world.getCraftServer().getPluginManager().callEvent(blockCookEvent);

                        if (blockCookEvent.isCancelled()) {
                            return;
                        }

                        result = blockCookEvent.getResult();
                        itemStack2 = CraftItemStack.asNMSCopy(result);
                        // CraftBukkit end
                        Containers.dropItemStack(world, (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), itemStack2);
                        tileentitycampfire.getItems().set(i, ItemStack.EMPTY);
                        world.sendBlockUpdated(blockposition, iblockdata, iblockdata, 3);
                        world.gameEvent(GameEvent.BLOCK_CHANGE, blockposition, GameEvent.Context.of(iblockdata));
                    }
                }
            }
        }

        if (flag) {
            setChanged(world, blockposition, iblockdata);
        }

    }

    @Inject(method = "placeFood", locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/entity/CampfireBlockEntity;cookingProgress:[I"))
    private void banner$cookStart(Entity p_238285_, ItemStack stack, int p_238287_, CallbackInfoReturnable<Boolean> cir, int i) {
        var event = new CampfireStartEvent(CraftBlock.at(this.level, this.worldPosition), CraftItemStack.asCraftMirror(stack), (CampfireRecipe)  getCookableRecipe(stack).get().toBukkitRecipe());
        Bukkit.getPluginManager().callEvent(event);
        this.cookingTime[i] = event.getTotalCookTime();
    }
}
