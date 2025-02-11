package org.bukkit.craftbukkit.v1_20_R2.generator;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSource;

import java.util.function.Function;

// Do not implement functions to this class, add to NormalChunkGenerator
public abstract class InternalChunkGenerator extends net.minecraft.world.level.chunk.ChunkGenerator {

    public InternalChunkGenerator(BiomeSource worldchunkmanager, Function<Holder<Biome>, BiomeGenerationSettings> function) {
        super(worldchunkmanager, function);
    }
}
