package com.zeide.lapitxohara.config;

import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public record SpawnPointData(RegistryKey<World> dimension, double x, double y, double z, float yaw, float pitch) {}
