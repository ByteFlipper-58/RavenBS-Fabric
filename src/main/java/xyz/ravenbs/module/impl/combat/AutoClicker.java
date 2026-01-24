package xyz.ravenbs.module.impl.combat;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class AutoClicker extends Module {
    private SliderSetting minCPS;
    private SliderSetting maxCPS;
    private SliderSetting jitter;
    private ButtonSetting weaponOnly;
    private ButtonSetting breakBlocks; // prevent clicking while breaking
    
    private long lastClick;
    private long leftHold;
    private long rightHold;
    private Random rand = new Random();

    public AutoClicker() {
        super("AutoClicker", ModuleCategory.combat);
        this.registerSetting(minCPS = new SliderSetting("Min CPS", 9, 1, 20, 0.5));
        this.registerSetting(maxCPS = new SliderSetting("Max CPS", 12, 1, 20, 0.5));
        this.registerSetting(jitter = new SliderSetting("Jitter", 0, 0, 3, 0.1));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", false));
        this.registerSetting(breakBlocks = new ButtonSetting("Break blocks", false));
    }

    @Override
    public void onUpdate() {
        if (mc.currentScreen != null) return;
        if (!InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_1)) return; // Only when holding left click
        
        if (weaponOnly.isToggled() && !Utils.isHoldingWeapon()) return;
        
        // Preventing clicking while mining requires checking if we are hitting a block
        if (!breakBlocks.isToggled() && mc.interactionManager.isBreakingBlock()) return;

        long time = System.currentTimeMillis();
        if (time - lastClick < leftHold) return;

        lastClick = time;
        
        // Calculate next delay
        double min = minCPS.getInput();
        double max = maxCPS.getInput();
        if (min > max) min = max;
        
        double cps = min + (rand.nextDouble() * (max - min));
        leftHold = (long) (1000.0 / cps);
        
        // Perform click
        // In 1.20 we can simulate key press or invoke attack
        // Invoking attack directly is safer for "Legit" feel than raw packets, but key press is most legit
        
        // Option 1: KeyBinding.onKeyPressed (Simulates raw input)
        // mc.options.attackKey.setPressed(true); 
        // But we need to unpress it.
        
        // Option 2: Direct method call
        // ((MinecraftClientAccessor)mc).doAttack();
        // We don't have accessors yet.
        
        // Let's us InputUtil approach or standard swing
        mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
        if (mc.crosshairTarget != null) {
            switch (mc.crosshairTarget.getType()) {
                case ENTITY:
                    mc.interactionManager.attackEntity(mc.player, ((net.minecraft.util.hit.EntityHitResult)mc.crosshairTarget).getEntity());
                    break;
                case BLOCK:
                    // Block breaking is handled by holding, so we might interrupt it if we spam attack?
                    // Actually, if we are in "attack" mode (holding click), vanilla auto-mines.
                    // AutoClicker usually is for PvP.
                    break;
                case MISS:
                    // Just swing
                    break;
            }
        }
        
        // Apply Jitter
        if (jitter.getInput() > 0) {
            float yaw = mc.player.getYaw();
            float pitch = mc.player.getPitch();
            float yawRandom = (float) ((rand.nextDouble() - 0.5) * jitter.getInput());
            float pitchRandom = (float) ((rand.nextDouble() - 0.5) * jitter.getInput());
            
            mc.player.setYaw(yaw + yawRandom);
            mc.player.setPitch(pitch + pitchRandom);
        }
    }
}
