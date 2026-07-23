package xyz.ravenbs.module.impl.movement;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import xyz.ravenbs.utility.ModuleSafetyManager;

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
        if (mc.player != null && mc.options != null) {
            ModuleSafetyManager.setKeyPressed(this, mc.options.sprintKey, true);
        }
    }
    
    @Override
    public void onDisable() {
        if (mc.options != null && mc.getWindow() != null) {
            int keyCode = ((xyz.ravenbs.mixin.client.MixinKeyBindingAccessor) mc.options.sprintKey).getBoundKey().getCode();
            boolean pressed = net.minecraft.client.util.InputUtil.isKeyPressed(mc.getWindow().getHandle(), keyCode);
            ModuleSafetyManager.setKeyPressed(this, mc.options.sprintKey, pressed);
        }
    }
}
