package xyz.ravenbs.module.impl.movement;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;

public class Fly extends Module {
    private SliderSetting speed;
    
    public Fly() {
        super("Fly", ModuleCategory.movement);
        this.registerSetting(speed = new SliderSetting("Speed", 2.0, 0.1, 5.0, 0.1));
    }

    @Override
    public void onEnable() {
        // Option 1: Creative Fly (if supported/bypass)
        // mc.player.getAbilities().flying = true; 
        // mc.player.getAbilities().allowFlying = true;
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.getAbilities().flying = false;
        mc.player.getAbilities().allowFlying = false;
    }

    @Override
    public void onUpdate() {
        if (!Utils.isMoving()) {
            mc.player.setVelocity(0, 0, 0);
        } else {
            // Simple velocity fly
             mc.player.getAbilities().flying = false; // Disable creative fly to manage velocity manually if needed
             
             // Check jump/sneak for vertical
             double y = 0;
             if (mc.options.jumpKey.isPressed()) {
                 y = speed.getInput();
             } else if (mc.options.sneakKey.isPressed()) {
                 y = -speed.getInput();
             }
             
             Utils.setSpeed(speed.getInput());
             mc.player.setVelocity(mc.player.getVelocity().x, y, mc.player.getVelocity().z);
             mc.player.fallDistance = 0;
        }
    }
}
