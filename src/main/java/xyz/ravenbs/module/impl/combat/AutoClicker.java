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
    
    private ButtonSetting alwaysOn;
    
    private long lastClick;
    private long leftHold;
    private long rightHold;
    private Random rand = new Random();

    private Thread clickThread;

    public AutoClicker() {
        super("AutoClicker", ModuleCategory.combat);
        this.registerSetting(minCPS = new SliderSetting("Min CPS", 9, 1, 42, 0.5));
        this.registerSetting(maxCPS = new SliderSetting("Max CPS", 12, 1, 42, 0.5));
        this.registerSetting(jitter = new SliderSetting("Jitter", 0, 0, 3, 0.1));
        this.registerSetting(alwaysOn = new ButtonSetting("Always on via bind", false));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", false));
        this.registerSetting(breakBlocks = new ButtonSetting("Break blocks", false));
    }

    @Override
    public void onEnable() {
        if (clickThread == null || !clickThread.isAlive()) {
            clickThread = new Thread(this::clickLoop);
            clickThread.setName("AutoClicker-Thread");
            clickThread.start();
        }
    }

    @Override
    public void onDisable() {
        if (clickThread != null) {
            clickThread.interrupt();
            clickThread = null;
        }
    }

    private volatile boolean leftMouseDown = false;
    
    // ...

    private void clickLoop() {
        while (this.isEnabled()) {
            try {
                // Determine if we should click
                boolean shouldClick = false;
                if (mc.currentScreen == null && mc.player != null) {
                    // Use cached input from onUpdate (Main Thread)
                    if (alwaysOn.isToggled() || leftMouseDown) {
                        shouldClick = true;
                        
                        if (weaponOnly.isToggled() && !Utils.isHoldingWeapon()) shouldClick = false;
                        if (!breakBlocks.isToggled() && mc.interactionManager.isBreakingBlock()) shouldClick = false;
                    }
                }

                if (shouldClick) {
                    // Reset cooldown and attack
                    mc.execute(() -> {
                         if (mc.player == null) return;
                         xyz.ravenbs.mixin.accessor.IMixinMinecraftClient accessor = (xyz.ravenbs.mixin.accessor.IMixinMinecraftClient) mc;
                         accessor.setAttackCooldown(0);
                         accessor.invokeDoAttack();
                         
                         // Apply Jitter (must be on main thread usually, but yaw/pitch is volatile-ish, safe enough)
                         if (jitter.getInput() > 0) {
                            float yaw = mc.player.getYaw();
                            float pitch = mc.player.getPitch();
                            float yawRandom = (float) ((rand.nextDouble() - 0.5) * jitter.getInput());
                            float pitchRandom = (float) ((rand.nextDouble() - 0.5) * jitter.getInput());
                            mc.player.setYaw(yaw + yawRandom);
                            mc.player.setPitch(pitch + pitchRandom);
                         }
                    });

                    // Calculate delay from settings (thread-safe access to settings inputs usually ok if volatile/atomic, 
                    // but SliderSetting.getInput() just reads a double. It might tear but unlikely to crash. 
                    // Ideally settings should be synchronized, but for a hack client this is standard.)
                    double min = minCPS.getInput();
                    double max = maxCPS.getInput();
                    if (min > max) min = max;
                    
                    double cps = min + (rand.nextDouble() * (max - min));
                    long delay = (long) (1000.0 / cps);
                    
                    Thread.sleep(delay);
                } else {
                    Thread.sleep(10); // Idle small wait
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void onUpdate() {
        // Cache input on main thread
        if (mc.getWindow() != null) { // Anti-crash
             leftMouseDown = InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_1);
        }
    }
}
