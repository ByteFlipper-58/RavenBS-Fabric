package xyz.ravenbs.utility;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {
    public static boolean isUpdateAvailable = false;
    public static String latestVersion = "";
    public static String downloadURL = "https://github.com/ByteFlipper-58/RavenBS-Fabric/releases";

    public static void checkForUpdates() {
        new Thread(() -> {
            try {
                String currentVersion = FabricLoader.getInstance().getModContainer("ravenbs-fabric").get().getMetadata().getVersion().getFriendlyString();
                
                // GitHub API URL
                URL url = new URL("https://api.github.com/repos/ByteFlipper-58/RavenBS-Fabric/releases/latest");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "RavenBS-Fabric");

                if (connection.getResponseCode() == 200) {
                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    latestVersion = json.get("tag_name").getAsString();

                    String cleanCurrent = currentVersion.replace("v", "");
                    String cleanLatest = latestVersion.replace("v", "");

                    if (!cleanCurrent.equals(cleanLatest)) {
                        isUpdateAvailable = true;
                    }
                }
            } catch (Exception e) {
                xyz.ravenbs.RavenBSFabric.LOGGER.error("Update check failed", e);
            }
        }).start();
    }

    public static void onJoin() {
        if (isUpdateAvailable) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                String currentVersion = FabricLoader.getInstance().getModContainer("ravenbs-fabric").get().getMetadata().getVersion().getFriendlyString();
                
                // Message 1: GitHub Link ([RavenBS] New version available...)
                Text prefix = Text.translatable("raven.prefix");
                Text versionMsg = Text.translatable("raven.update.new_version", latestVersion, currentVersion);
                
                Text message = Text.empty().append(prefix).append(versionMsg);
                mc.player.sendMessage(message, false);

                // Message 2: Website Link (Also available at...)
                Text siteMsg = Text.translatable("raven.update.site_msg");
                Text siteLink = Text.literal("§bravenbs.xyz")
                        .styled(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://ravenbs.xyz"))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("raven.update.site_hover")))
                        );
                
                // No prefix for the second line
                Text siteMessage = Text.empty().append(siteMsg).append(siteLink);
                mc.player.sendMessage(siteMessage, false);
            }
        }
    }
}
