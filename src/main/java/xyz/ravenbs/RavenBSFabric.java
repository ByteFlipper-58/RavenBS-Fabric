package xyz.ravenbs;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RavenBSFabric implements ModInitializer {
    public static final String MOD_ID = "ravenbs-fabric";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Hello from RavenBS-Fabric!");
        
        // Reset configuration on server join (Strict Config)
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String currentProfile = xyz.ravenbs.config.ConfigManager.getCurrentProfileName();
            if (currentProfile != null && !currentProfile.isEmpty()) {
                xyz.ravenbs.config.ConfigManager.loadConfig(currentProfile);
                xyz.ravenbs.utility.NotificationManager.show("Config", "Profile '" + currentProfile + "' loaded.", xyz.ravenbs.utility.Notification.Type.INFO);
            } else if (xyz.ravenbs.config.ConfigManager.isLoaded()) {
                // If loaded default config
                xyz.ravenbs.config.ConfigManager.loadConfig();
                 xyz.ravenbs.utility.NotificationManager.show("Config", "Default profile loaded.", xyz.ravenbs.utility.Notification.Type.INFO);
            }
        });
    }
}
