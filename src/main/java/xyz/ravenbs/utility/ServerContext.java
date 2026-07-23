package xyz.ravenbs.utility;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;

import java.util.Locale;

/** Provides one observable source of server, lobby, and minigame state for client modules. */
public final class ServerContext {
    public enum GameMode {
        UNKNOWN,
        LOBBY,
        DUELS,
        BED_WARS,
        SKY_WARS,
        SPEED_BUILDERS,
        MURDER_MYSTERY
    }

    private static String serverAddress = "singleplayer";
    private static String sidebarTitle = "";
    private static GameMode gameMode = GameMode.UNKNOWN;
    private static String detectionReason = "No world loaded";

    private ServerContext() {
    }

    public static void update(MinecraftClient client) {
        if (client == null || client.world == null || client.player == null) {
            reset();
            return;
        }

        ServerInfo entry = client.getCurrentServerEntry();
        serverAddress = entry == null ? "singleplayer" : entry.address;
        sidebarTitle = readSidebarTitle(client);
        gameMode = detectMode(sidebarTitle);
        detectionReason = gameMode == GameMode.UNKNOWN
                ? "No supported mode keyword in sidebar"
                : "Sidebar: " + sidebarTitle;
    }

    public static void reset() {
        serverAddress = "singleplayer";
        sidebarTitle = "";
        gameMode = GameMode.UNKNOWN;
        detectionReason = "No world loaded";
    }

    public static String getServerAddress() {
        return serverAddress;
    }

    public static String getSidebarTitle() {
        return sidebarTitle;
    }

    public static GameMode getGameMode() {
        return gameMode;
    }

    public static boolean is(GameMode expectedMode) {
        return gameMode == expectedMode;
    }

    public static String getDetectionReason() {
        return detectionReason;
    }

    public static String describe() {
        return "server=" + serverAddress + ", mode=" + gameMode + ", sidebar="
                + (sidebarTitle.isEmpty() ? "-" : sidebarTitle) + ", reason=" + detectionReason;
    }

    private static String readSidebarTitle(MinecraftClient client) {
        Scoreboard scoreboard = client.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(1);
        return objective == null ? "" : objective.getDisplayName().getString() + " " + objective.getName();
    }

    private static GameMode detectMode(String rawTitle) {
        String title = rawTitle.toLowerCase(Locale.ROOT);
        if (containsAny(title, "duel", "boxing", "sumo", "bridge", "uhc", "nodebuff", "combo")) {
            return GameMode.DUELS;
        }
        if (containsAny(title, "bedwars", "bed wars")) {
            return GameMode.BED_WARS;
        }
        if (containsAny(title, "skywars", "sky wars")) {
            return GameMode.SKY_WARS;
        }
        if (containsAny(title, "speed builders", "speedbuilders")) {
            return GameMode.SPEED_BUILDERS;
        }
        if (containsAny(title, "murder mystery", "murder")) {
            return GameMode.MURDER_MYSTERY;
        }
        if (containsAny(title, "lobby", "hub")) {
            return GameMode.LOBBY;
        }
        return GameMode.UNKNOWN;
    }

    private static boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }
}
