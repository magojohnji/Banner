package com.mohistmc.banner.mixin.world.item.crafting;

import com.mohistmc.banner.bukkit.inventory.recipe.BannerModdedRecipe;
import com.mohistmc.banner.injection.world.item.crafting.InjectionRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftRecipe;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftShapedRecipe;
import org.bukkit.inventory.RecipeChoice;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ShapedRecipe.class)
public abstract class MixinShapedRecipe implements CraftingRecipe, InjectionRecipe {

    @Shadow @Final
    ItemStack result;

    @Shadow @Final
    String group;

    @Shadow @Final
    CraftingBookCategory category;

    @Shadow @Final
    int width;

    @Shadow @Final
    int height;

    @Shadow @Final
    NonNullList<Ingredient> recipeItems;

    @Shadow public abstract int getHeight();

    @Shadow public abstract int getWidth();

    @Override
    // CraftBukkit start
    public org.bukkit.inventory.Recipe toBukkitRecipe(NamespacedKey id) {
        if (this.getWidth() < 1 || this.getWidth() > 3 || this.getHeight() < 1 || this.getHeight() > 3 || this.result.isEmpty()) {
            return new BannerModdedRecipe(id, this);
        }
        CraftItemStack result = CraftItemStack.asCraftMirror(this.result);
        CraftShapedRecipe recipe = new CraftShapedRecipe(id, result, ((ShapedRecipe) (Object) this));
        recipe.setGroup(this.group);
        recipe.setCategory(CraftRecipe.getCategory(this.category()));

        switch (this.height) {
            case 1 -> {
                switch (this.width) {
                    case 1 -> recipe.shape("a");
                    case 2 -> recipe.shape("ab");
                    case 3 -> recipe.shape("abc");
                }
            }
            case 2 -> {
                switch (this.width) {
                    case 1 -> recipe.shape("a", "b");
                    case 2 -> recipe.shape("ab", "cd");
                    case 3 -> recipe.shape("abc", "def");
                }
            }
            case 3 -> {
                switch (this.width) {
                    case 1 -> recipe.shape("a", "b", "c");
                    case 2 -> recipe.shape("ab", "cd", "ef");
                    case 3 -> recipe.shape("abc", "def", "ghi");
                }
            }
        }
        char c = 'a';
        for (Ingredient list : this.recipeItems) {
            RecipeChoice choice = CraftRecipe.toBukkit(list);
            if (choice != null) {
                recipe.setIngredient(c, choice);
            }

            c++;
        }
        return recipe;
    }
    // CraftBukkit end
}
