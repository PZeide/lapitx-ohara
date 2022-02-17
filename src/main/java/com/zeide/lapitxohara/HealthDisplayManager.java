package com.zeide.lapitxohara;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class HealthDisplayManager {
    private static final String OBJECTIVE_NAME = "LapitxHealthDisplay";

    private final MinecraftServer server;

    public HealthDisplayManager(MinecraftServer server) {
        this.server = server;
    }

    public void initializeScoreboard() {
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(OBJECTIVE_NAME);
        if (objective == null) {
            objective = scoreboard.addObjective(
                    OBJECTIVE_NAME,
                    ScoreboardCriterion.HEALTH,
                    new LiteralText("❤").setStyle(Style.EMPTY.withColor(Formatting.DARK_RED)),
                    ScoreboardCriterion.RenderType.INTEGER
            );
        }

        scoreboard.setObjectiveSlot(Scoreboard.BELOW_NAME_DISPLAY_SLOT_ID, objective);
    }
}
