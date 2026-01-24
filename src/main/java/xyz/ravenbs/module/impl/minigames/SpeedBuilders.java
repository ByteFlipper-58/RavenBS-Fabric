package xyz.ravenbs.module.impl.minigames;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;

public class SpeedBuilders extends Module {

    public SpeedBuilders() {
        super("SpeedBuilders", ModuleCategory.minigames);
        this.registerSetting(new DescriptionSetting("Helpers for Speed Builders game."));
    }

    @Override
    public void onUpdate() {
        // This module would need specific Speed Builders detection
        // Placeholder for now
    }
}
