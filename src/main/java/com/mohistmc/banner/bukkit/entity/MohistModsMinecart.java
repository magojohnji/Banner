package com.mohistmc.banner.bukkit.entity;

import com.mohistmc.banner.api.EntityAPI;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftMinecart;

public class MohistModsMinecart extends CraftMinecart {

    public String entityName;

    public MohistModsMinecart(CraftServer server, AbstractMinecart entity) {
        super(server, entity);
        this.entityName = EntityAPI.entityName(entity);
    }

    @Override
    public AbstractMinecart getHandle() {
        return (AbstractMinecart) this.entity;
    }

    @Override
    public String toString() {
        return "MohistModsMinecart{" + entityName + '}';
    }
}
