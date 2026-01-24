package xyz.ravenbs.module.impl.minigames;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;

public class SkyWars extends Module {
    public SkyWars() {
        super("SkyWars", ModuleCategory.minigames);
        this.registerSetting(new DescriptionSetting("SkyWars Utilities"));
        // Placeholder for future SkyWars specific tools
    }
}
