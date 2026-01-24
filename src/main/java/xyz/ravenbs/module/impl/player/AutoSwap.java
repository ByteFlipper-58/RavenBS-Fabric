package xyz.ravenbs.module.impl.player;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import net.minecraft.item.SwordItem;

public class AutoSwap extends Module {
    public AutoSwap() {
        super("AutoSwap", ModuleCategory.player);
    }

    // Usually triggered on attack (Left click or Killaura).
    // Hook into attack or update loop?
    // If we are holding left click on an entity, swap to sword.
    
    @Override
    public void onUpdate() {
        if (mc.options.attackKey.isPressed() && mc.targetedEntity != null) {
            swapToWeapon();
        }
    }
    
    public void swapToWeapon() {
        int slot = -1;
        float damage = 1;
        
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof SwordItem) {
                 // Check damage
                 // For now just pick first sword/axe
                 slot = i;
                 break;
            }
        }
        
        if (slot != -1 && mc.player.getInventory().selectedSlot != slot) {
            mc.player.getInventory().selectedSlot = slot;
        }
    }
}
