package com.mohistmc.banner.mixin.world.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mohistmc.banner.injection.world.item.crafting.InjectionRecipeManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Mixin(RecipeManager.class)
public abstract class MixinRecipeManager implements InjectionRecipeManager {

    @Shadow private boolean hasErrors;
    @Shadow @Final
    private static Logger LOGGER;

    @Shadow public Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes;
    @Shadow
    protected static RecipeHolder<?> fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
        return null;
    }

    @Shadow private Map<ResourceLocation, RecipeHolder<?>> byName;

    @Shadow protected abstract <C extends Container, T extends Recipe<C>> Map<ResourceLocation, RecipeHolder<T>> byType(RecipeType<T> recipeType);

    public Map<RecipeType<?>, Object2ObjectLinkedOpenHashMap<ResourceLocation, Recipe<?>>> recipesCB = ImmutableMap.of(); // CraftBukkit  // Mohist use obf name


    // Banner - fix mixin
    protected Map<RecipeType<?>, Object2ObjectLinkedOpenHashMap<ResourceLocation, Recipe<?>>> map1;
    /**
     * @author wdog5
     * @reason bukkit current recipe
     */
    @Overwrite
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        this.hasErrors = false;

        // CraftBukkit start - SPIGOT-5667 make sure all types are populated and mutable
        map1 = Maps.newHashMap();
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            map1.put(recipeType, new Object2ObjectLinkedOpenHashMap<>());
        }
        // CraftBukkit end

        Map<RecipeType<?>, ImmutableMap.Builder<ResourceLocation, Recipe<?>>> map = Maps.newHashMap();
        ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> builder = ImmutableMap.builder();

        Iterator var6 = pObject.entrySet().iterator();

        while(var6.hasNext()) {
            Map.Entry<ResourceLocation, JsonElement> entry = (Map.Entry)var6.next();
            ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();

            try {
                RecipeHolder<?> recipe = fromJson(resourceLocation, GsonHelper.convertToJsonObject(entry.getValue(), "top element"));
                map.computeIfAbsent(recipe.value().getType(), (p_44075_) -> {
                    return ImmutableMap.builder();
                }).put(resourceLocation, recipe.value());

                // CraftBukkit start
                (map1.computeIfAbsent(recipe.value().getType(), (recipes) -> {
                    return new Object2ObjectLinkedOpenHashMap<>();
                    // CraftBukkit end
                })).put(resourceLocation, recipe.value());
                builder.put(resourceLocation, recipe);
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                LOGGER.error("Parsing error loading recipe {}", resourceLocation, jsonparseexception);
            }
        }

        this.recipes = map.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (p_44033_) -> {
            return p_44033_.getValue().build();
        }));
        this.recipesCB = map1.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (entry1) -> {
            return entry1.getValue(); // CraftBukkit
        }));
        this.byName = Maps.newHashMap(builder.build()); // CraftBukkit
        LOGGER.info("Loaded {} recipes", (int)map.size());
    }

    /**
     * @author wdog5
     * @reason bukkit current recipe
     */
    @Overwrite
    public <C extends Container, T extends Recipe<C>> Optional<RecipeHolder<T>> getRecipeFor(RecipeType<T> recipeTypeIn, C inventoryIn, Level worldIn) {
        Optional<RecipeHolder<T>> optional = this.byType(recipeTypeIn).values().stream().filter((recipe) -> {
            return recipe.value().matches(inventoryIn, worldIn);
        }).findFirst();
        inventoryIn.setCurrentRecipe(optional.orElse(null));
        return optional;
    }

    @Override
    public void addRecipe(RecipeHolder<?> irecipe) {
        if (this.recipes instanceof ImmutableMap) {
            this.recipes = new HashMap<>(recipes);
        }
        if (this.byName instanceof ImmutableMap) {
            this.byName = new HashMap<>(byName);
        }
        Map<ResourceLocation, Recipe<?>> map = this.recipes.get(irecipe.value().getType());
        Object2ObjectLinkedOpenHashMap<ResourceLocation, Recipe<?>> map0;
        if (!(map instanceof Object2ObjectLinkedOpenHashMap)) {
            Object2ObjectLinkedOpenHashMap<ResourceLocation, Recipe<?>> hashMap = new Object2ObjectLinkedOpenHashMap<>();
            hashMap.putAll(map);
            this.recipes.put(irecipe.value().getType(), hashMap);
            map0 = hashMap;
        } else {
            map0 = ((Object2ObjectLinkedOpenHashMap<ResourceLocation, Recipe<?>>) map);
        }

        Object2ObjectLinkedOpenHashMap<ResourceLocation, Recipe<?>> craftbukkit = this.recipesCB.get(irecipe.value().getType()); // CraftBukkit

        if (this.byName.containsKey(irecipe.id()) || map0.containsKey(irecipe.id()) || craftbukkit.containsKey(irecipe.id())) {
            throw new IllegalStateException("Duplicate recipe ignored with ID " + irecipe.id());
        } else {
            map0.putAndMoveToFirst(irecipe.id(), irecipe.value());
            craftbukkit.putAndMoveToFirst(irecipe.id(), irecipe.value()); // CraftBukkit - SPIGOT-4638: last recipe gets priority
            this.byName.put(irecipe.id(), irecipe);
        }
    }

    @Override
    public boolean removeRecipe(ResourceLocation mcKey) {
        if (this.recipes instanceof ImmutableMap) {
            this.recipes = new HashMap<>(recipes);
        }
        if (this.byName instanceof ImmutableMap) {
            this.byName = new HashMap<>(byName);
        }
        for (var recipes : recipes.values()) {
            if (!(recipes instanceof ImmutableMap)) {
                recipes.remove(mcKey);
            }
        }
        for (Object2ObjectLinkedOpenHashMap<ResourceLocation, Recipe<?>> recipes0 : recipesCB.values()) {
            recipes0.remove(mcKey);
        }
        return byName.remove(mcKey) != null;
    }

    @Override
    public void clearRecipes() {
        this.recipes = Maps.newHashMap();
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            this.recipes.put(recipeType, ImmutableMap.of());
        }

        this.recipesCB = Maps.newHashMap();
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            this.recipesCB.put(recipeType, new Object2ObjectLinkedOpenHashMap<>());
        }
        this.byName = new HashMap<>();
    }

}
