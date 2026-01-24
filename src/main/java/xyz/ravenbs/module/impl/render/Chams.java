package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;

public class Chams extends Module {
    public static DescriptionSetting description;
    public static ButtonSetting ignoreDepth;

    public Chams() {
        super("Chams", ModuleCategory.render);
        this.registerSetting(description = new DescriptionSetting("See players through walls."));
        this.registerSetting(ignoreDepth = new ButtonSetting("Ignore Depth", true));
    }
}
