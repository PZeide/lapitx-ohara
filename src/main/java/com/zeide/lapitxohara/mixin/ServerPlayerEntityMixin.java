package com.zeide.lapitxohara.mixin;

import com.zeide.lapitxohara.LapitxOharaMod;
import com.zeide.lapitxohara.config.SpawnPointData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Shadow private BlockPos spawnPointPosition;

    @Inject(method = "getSpawnPointDimension", at = @At("HEAD"), cancellable = true)
    private void getSpawnPointDimension(CallbackInfoReturnable<RegistryKey<World>> infoReturnable) {
        if (spawnPointPosition == null) {
            // No spawn point set
            Optional<SpawnPointData> spawnPointData = LapitxOharaMod.INSTANCE.getConfig().getSpawnPointData();
            spawnPointData.ifPresent(data -> infoReturnable.setReturnValue(data.dimension()));
        }
    }

    @Inject(method = "getSpawnPointPosition", at = @At("HEAD"), cancellable = true)
    private void getSpawnPointPosition(CallbackInfoReturnable<BlockPos> infoReturnable) {
        if (spawnPointPosition == null) {
            // No spawn point set
            Optional<SpawnPointData> spawnPointData = LapitxOharaMod.INSTANCE.getConfig().getSpawnPointData();
            spawnPointData.ifPresent(data -> infoReturnable.setReturnValue(new BlockPos(data.x(), data.y(), data.z())));
        }
    }

    @Inject(method = "getSpawnAngle", at = @At("HEAD"), cancellable = true)
    private void getSpawnAngle(CallbackInfoReturnable<Float> infoReturnable) {
        if (spawnPointPosition == null) {
            // No spawn point set
            Optional<SpawnPointData> spawnPointData = LapitxOharaMod.INSTANCE.getConfig().getSpawnPointData();
            spawnPointData.ifPresent(data -> infoReturnable.setReturnValue(data.yaw()));
        }
    }

    @Inject(method = "moveToSpawn", at = @At("HEAD"), cancellable = true)
    private void moveToSpawn(ServerWorld world, CallbackInfo info) {
        LapitxOharaMod.INSTANCE.getConfig().getSpawnPointData().ifPresent(data -> {
            ((ServerPlayerEntity) (Object) this).refreshPositionAndAngles(data.x(), data.y(), data.z(), data.yaw(), data.pitch());
            info.cancel();
        });
    }
}
