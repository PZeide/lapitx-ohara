package com.zeide.lapitxohara;

import com.zeide.lapitxohara.commands.ForceSpawnPointCommand;
import com.zeide.lapitxohara.commands.LookupCommand;
import com.zeide.lapitxohara.config.LapitxOharaConfig;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LapitxOharaMod implements DedicatedServerModInitializer {
    public static LapitxOharaMod INSTANCE;
    public static final Logger LOGGER = LoggerFactory.getLogger("lapitx-ohara");

    private LapitxOharaConfig config;
    private DeathCounterManager deathCounterManager;

    @Override
    public void onInitializeServer() {
        INSTANCE = this;
        config = LapitxOharaConfig.loadConfig();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            deathCounterManager = new DeathCounterManager(server);
            deathCounterManager.initializeScoreboard();
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            if (!dedicated)
                return;

            LookupCommand.register(dispatcher);
            ForceSpawnPointCommand.register(dispatcher);
        });
    }

    public LapitxOharaConfig getConfig() {
        return config;
    }

    public DeathCounterManager getDeathCounterManager() {
        return deathCounterManager;
    }
}
