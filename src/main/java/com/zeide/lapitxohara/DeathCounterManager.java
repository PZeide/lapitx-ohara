package com.zeide.lapitxohara;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class DeathCounterManager {
    private static final String OBJECTIVE_NAME = "LapitxDeathCounter";

    private final MinecraftServer server;

    public DeathCounterManager(MinecraftServer server) {
        this.server = server;
    }

    public void initializeScoreboard() {
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(OBJECTIVE_NAME);
        if (objective == null) {
            objective = scoreboard.addObjective(
                    OBJECTIVE_NAME,
                    ScoreboardCriterion.DEATH_COUNT,
                    new LiteralText("Compteur de morts").setStyle(Style.EMPTY.withColor(Formatting.GOLD)),
                    ScoreboardCriterion.RenderType.INTEGER
            );

            scoreboard.setObjectiveSlot(Scoreboard.SIDEBAR_DISPLAY_SLOT_ID, objective);
        }
    }

    public void retrieveStatDataIfPresent(ServerPlayerEntity player) {
        // Retrieve data in player stat file if no there is no data in the scoreboard
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjective(OBJECTIVE_NAME);

        String playerName = player.getGameProfile().getName();

        if (scoreboard.getAllPlayerScores(objective).stream().anyMatch(score -> score.getPlayerName().equals(playerName)))
            return;

        ScoreboardPlayerScore score = scoreboard.getPlayerScore(playerName, objective);
        score.setScore(player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS)));
    }
}
