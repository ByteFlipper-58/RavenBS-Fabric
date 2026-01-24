package xyz.ravenbs.module.impl.movement;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;

public class Sprint extends Module {
    private ButtonSetting displayText;
    private ButtonSetting rainbow;
    
    public Sprint() {
        super("Sprint", ModuleCategory.movement, 0);
        this.registerSetting(new DescriptionSetting("Command: 'sprint [msg]'"));
        this.registerSetting(displayText = new ButtonSetting("Display text", false));
        this.registerSetting(rainbow = new ButtonSetting("Rainbow", false));
    }

    @Override
    public void onUpdate() {
        if (mc.player != null) {
            // Force sprint key state
            // In Fabric/Intermediary: mc.options.sprintKey.setPressed(true)
            // But we can also direct set sprinting if we want strict behavior
             mc.options.sprintKey.setPressed(true);
        }
    }
    
    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.sprintKey.setPressed(false);
        }
    }
}
