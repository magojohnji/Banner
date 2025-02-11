package com.mohistmc.banner.mixin.world.item.crafting;

import com.mohistmc.banner.bukkit.inventory.recipe.BannerModdedRecipe;
import com.mohistmc.banner.injection.world.item.crafting.InjectionRecipe;
import net.minecraft.world.item.crafting.Recipe;
import org.bukkit.NamespacedKey;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Recipe.class)
public interface MixinRecipe extends InjectionRecipe {

    @Override
    default org.bukkit.inventory.Recipe toBukkitRecipe(NamespacedKey id) {
        return new BannerModdedRecipe(id, ((Recipe<?>) (Object) this));
    }
}
