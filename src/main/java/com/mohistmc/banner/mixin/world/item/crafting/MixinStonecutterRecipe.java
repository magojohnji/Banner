package com.mohistmc.banner.mixin.world.item.crafting;

import com.mohistmc.banner.bukkit.inventory.recipe.BannerModdedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftRecipe;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftStonecuttingRecipe;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StonecutterRecipe.class)
public abstract class MixinStonecutterRecipe extends SingleItemRecipe {

    public MixinStonecutterRecipe(RecipeType<?> recipeType, RecipeSerializer<?> recipeSerializer, String string, Ingredient ingredient, ItemStack itemStack) {
        super(recipeType, recipeSerializer, string, ingredient, itemStack);
    }

    @Override
    public Recipe toBukkitRecipe(NamespacedKey id) {
        if (this.result.isEmpty()) {
            return new BannerModdedRecipe(id, this);
        }
        CraftItemStack result = CraftItemStack.asCraftMirror(this.result);

        CraftStonecuttingRecipe recipe = new CraftStonecuttingRecipe(id, result, CraftRecipe.toBukkit(this.ingredient));
        recipe.setGroup(this.group);

        return recipe;
    }
}
