package xyz.ravenbs.module.impl.minigames;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;

public class DuelsStats extends Module {

    public DuelsStats() {
        super("DuelsStats", ModuleCategory.minigames);
        this.registerSetting(new DescriptionSetting("Shows opponent stats in duels."));
    }

    @Override
    public void onUpdate() {
        // This module would need Hypixel API integration
        // Placeholder for now
    }
}
