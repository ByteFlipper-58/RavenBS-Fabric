package xyz.ravenbs.module.impl.movement;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class InvMove extends Module {
    public InvMove() {
        super("InvMove", ModuleCategory.movement);
    }

    @Override
    public void onUpdate() {
        if (mc.currentScreen instanceof HandledScreen) {
             handleKey(mc.options.forwardKey);
             handleKey(mc.options.backKey);
             handleKey(mc.options.leftKey);
             handleKey(mc.options.rightKey);
             handleKey(mc.options.jumpKey);
             handleKey(mc.options.sprintKey);
             
             // Rotation logic usually goes here (allowing looking around in inventory)
             // Requires detecting arrow keys or mouse delta
        }
    }
    
    private void handleKey(net.minecraft.client.option.KeyBinding key) {
        // Manually set pressed state if the physical key is down
        boolean pressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), ((xyz.ravenbs.mixin.client.MixinKeyBindingAccessor)key).getBoundKey().getCode());
        key.setPressed(pressed);
    }
}
