package xyz.ravenbs.module.impl.minigames;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;

public class BedWars extends Module {
    public BedWars() {
        super("BedWars", ModuleCategory.minigames);
        this.registerSetting(new DescriptionSetting("BedWars Utilities"));
        // Placeholder for future BedWars specific tools (WhiteList, AutoRequeue, etc)
    }
}
