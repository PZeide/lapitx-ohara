package com.zeide.lapitxohara.mixin;

import com.zeide.lapitxohara.LapitxOharaMod;
import com.zeide.lapitxohara.config.SpawnPointData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.source.BiomeAccess;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow @Final private MinecraftServer server;
    @Shadow @Final private List<ServerPlayerEntity> players;
    @Shadow @Final private Map<UUID, ServerPlayerEntity> playerMap;

    @Shadow public abstract void sendWorldInfo(ServerPlayerEntity player, ServerWorld world);
    @Shadow public abstract void sendCommandTree(ServerPlayerEntity player);

    @Inject(method = "loadPlayerData(Lnet/minecraft/server/network/ServerPlayerEntity;)Lnet/minecraft/nbt/NbtCompound;", at = @At("RETURN"), cancellable = true)
    public void loadPlayerData(ServerPlayerEntity player, @NotNull CallbackInfoReturnable<NbtCompound> infoReturnable) {
        // This value is null if it's a new player
        NbtCompound currentCompound = infoReturnable.getReturnValue();
        Optional<SpawnPointData> spawnPointData = LapitxOharaMod.INSTANCE.getConfig().getSpawnPointData();

        if (currentCompound == null && spawnPointData.isPresent()) {
            // Set his spawn point as early as possible
            NbtCompound spawnPointCompound = createSpawnPointCompound(spawnPointData.get());
            player.readNbt(spawnPointCompound);
            infoReturnable.setReturnValue(spawnPointCompound);
        }
    }

    @Inject(method = "respawnPlayer", at = @At("HEAD"), cancellable = true)
    public void respawnPlayer(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> infoReturnable) {
        Optional<SpawnPointData> spawnPointData = LapitxOharaMod.INSTANCE.getConfig().getSpawnPointData();
        if (spawnPointData.isEmpty())
            return;

        ServerWorld spawnPointDimension = server.getWorld(player.getSpawnPointDimension());
        if (spawnPointDimension == null)
            return;

        ServerWorld newSpawnPointDimension = server.getWorld(spawnPointData.get().dimension());
        if (newSpawnPointDimension == null) {
            LapitxOharaMod.LOGGER.warn(String.format("Unable to find world for identifier %s !", spawnPointData.get().dimension()));
            return;
        }

        // Find bed or anchor spawn point if any
        Optional<Vec3d> candidateSpawnPoint = PlayerEntity.findRespawnPosition(
                spawnPointDimension,
                player.getSpawnPointPosition(),
                player.getSpawnAngle(),
                player.isSpawnForced(),
                player.isSpawnForced()
        );

        if (candidateSpawnPoint.isEmpty())
            infoReturnable.setReturnValue(vanillaRespawnPlayer(spawnPointData.get(), player, alive));
    }

    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
        LapitxOharaMod.INSTANCE.getDeathCounterManager().retrieveStatDataIfPresent(player);
    }

    private NbtCompound createSpawnPointCompound(SpawnPointData spawnPointData) {
        NbtCompound spawnPointCompound = new NbtCompound();
        spawnPointCompound.putString("Dimension", spawnPointData.dimension().getValue().toString());

        NbtList posList = new NbtList();
        posList.add(0, NbtDouble.of(spawnPointData.x()));
        posList.add(1, NbtDouble.of(spawnPointData.y()));
        posList.add(2, NbtDouble.of(spawnPointData.z()));

        spawnPointCompound.put("Pos", posList);

        NbtList rotationList = new NbtList();
        rotationList.add(0, NbtFloat.of(spawnPointData.yaw()));
        rotationList.add(1, NbtFloat.of(spawnPointData.pitch()));

        spawnPointCompound.put("Rotation", rotationList);

        return spawnPointCompound;
    }

    private ServerPlayerEntity vanillaRespawnPlayer(SpawnPointData spawnPointData, ServerPlayerEntity originalPlayer, boolean alive) {
        // Modified default vanilla behaviour
        players.remove(originalPlayer);
        originalPlayer.getWorld().removePlayer(originalPlayer, Entity.RemovalReason.DISCARDED);

        ServerWorld spawnPointDimension = server.getWorld(spawnPointData.dimension());
        ServerPlayerEntity newPlayer = new ServerPlayerEntity(server, Objects.requireNonNull(spawnPointDimension), originalPlayer.getGameProfile());
        newPlayer.networkHandler = originalPlayer.networkHandler;
        newPlayer.copyFrom(originalPlayer, alive);
        newPlayer.setId(originalPlayer.getId());
        newPlayer.setMainArm(originalPlayer.getMainArm());

        for (String scoreBoardTag : originalPlayer.getScoreboardTags()) {
            newPlayer.addScoreboardTag(scoreBoardTag);
        }

        while(!spawnPointDimension.isSpaceEmpty(newPlayer) && newPlayer.getY() < (double)spawnPointDimension.getTopY()) {
            newPlayer.setPosition(newPlayer.getX(), newPlayer.getY() + 1.0D, newPlayer.getZ());
        }

        WorldProperties worldProperties = newPlayer.world.getLevelProperties();
        newPlayer.networkHandler.sendPacket(new PlayerRespawnS2CPacket(newPlayer.world.getDimension(), newPlayer.world.getRegistryKey(), BiomeAccess.hashSeed(newPlayer.getWorld().getSeed()), newPlayer.interactionManager.getGameMode(), newPlayer.interactionManager.getPreviousGameMode(), newPlayer.getWorld().isDebugWorld(), newPlayer.getWorld().isFlat(), alive));
        newPlayer.networkHandler.requestTeleport(newPlayer.getX(), newPlayer.getY(), newPlayer.getZ(), newPlayer.getYaw(), newPlayer.getPitch());
        newPlayer.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(newPlayer.getSpawnPointPosition(), newPlayer.getSpawnAngle()));
        newPlayer.networkHandler.sendPacket(new DifficultyS2CPacket(worldProperties.getDifficulty(), worldProperties.isDifficultyLocked()));
        newPlayer.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(newPlayer.experienceProgress, newPlayer.totalExperience, newPlayer.experienceLevel));
        sendWorldInfo(newPlayer, spawnPointDimension);
        sendCommandTree(newPlayer);
        spawnPointDimension.onPlayerRespawned(newPlayer);
        players.add(newPlayer);
        playerMap.put(newPlayer.getUuid(), newPlayer);
        newPlayer.onSpawn();
        newPlayer.setHealth(newPlayer.getHealth());

        return newPlayer;
    }
}
