package org.bukkit.craftbukkit.v1_20_R2.entity;

import com.google.common.base.Preconditions;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.bukkit.entity.MushroomCow;

public class CraftMushroomCow extends CraftCow implements MushroomCow, io.papermc.paper.entity.PaperShearable { // Paper
    public CraftMushroomCow(CraftServer server, net.minecraft.world.entity.animal.MushroomCow entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.MushroomCow getHandle() {
        return (net.minecraft.world.entity.animal.MushroomCow) entity;
    }

    @Override
    public Variant getVariant() {
        return Variant.values()[getHandle().getVariant().ordinal()];
    }

    @Override
    public void setVariant(Variant variant) {
        Preconditions.checkArgument(variant != null, "variant");

        getHandle().setVariant(net.minecraft.world.entity.animal.MushroomCow.MushroomType.values()[variant.ordinal()]);
    }

    @Override
    public String toString() {
        return "CraftMushroomCow";
    }
}
