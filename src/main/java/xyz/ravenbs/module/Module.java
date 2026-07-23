package xyz.ravenbs.module;

import xyz.ravenbs.module.setting.Setting;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;
import xyz.ravenbs.utility.ModuleSafetyManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module {
    protected static MinecraftClient mc = MinecraftClient.getInstance();

    private final String id;
    protected String name;
    protected ModuleCategory category;
    protected int keycode;
    protected boolean enabled;
    private boolean isToggled = false;
    
    private final List<Setting> settings = new ArrayList<>();
    private int persistentSettingCount;
    
    public Module(String name, ModuleCategory category, int keycode) {
        this.id = getClass().getName();
        this.name = name;
        this.category = category;
        this.keycode = keycode;
    }

    public Module(String name, ModuleCategory category) {
        this(name, category, 0);
    }
    
    public String getName() {
        return name;
    }

    public final String getId() {
        return id;
    }
    
    public String getLocalizedName() {
        if (net.minecraft.client.resource.language.I18n.hasTranslation("raven.module." + name.toLowerCase() + ".name")) {
            return net.minecraft.client.resource.language.I18n.translate("raven.module." + name.toLowerCase() + ".name");
        }
        return name;
    }
    
    public String getDescription() {
        if (net.minecraft.client.resource.language.I18n.hasTranslation("raven.module." + name.toLowerCase() + ".desc")) {
            return net.minecraft.client.resource.language.I18n.translate("raven.module." + name.toLowerCase() + ".desc");
        }
        return "";
    }
    
    public ModuleCategory getCategory() {
        return category;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        setEnabled(enabled, false);
    }

    public void setEnabled(boolean enabled, boolean notify) {
        if (this.enabled == enabled) {
            return;
        }

        if (enabled) {
            ModuleManager.clearModuleFault(this);
        }
        this.enabled = enabled;
        try {
            if (enabled) {
                onEnable();
            } else {
                onDisable();
            }
        } catch (Throwable t) {
            if (enabled) {
                this.enabled = false;
                try {
                    onDisable();
                } catch (Throwable cleanupError) {
                    ModuleManager.handleModuleError(this, "onDisable cleanup", cleanupError);
                }
            }
            ModuleSafetyManager.releaseModule(this);
            ModuleManager.handleModuleError(this, enabled ? "onEnable" : "onDisable", t);
            return;
        }

        if (!enabled) {
            ModuleSafetyManager.releaseModule(this);
        }

        if (notify && this.enabled == enabled) {
            if (enabled) {
                xyz.ravenbs.utility.NotificationManager.show("Module", getName() + " Enabled", xyz.ravenbs.utility.Notification.Type.INFO);
            } else {
                xyz.ravenbs.utility.NotificationManager.show("Module", getName() + " Disabled", xyz.ravenbs.utility.Notification.Type.INFO);
            }
        }
    }
    
    public void toggle() {
        setEnabled(!enabled, true);
    }
    
    public void onEnable() {
        // Override
    }
    
    public void onPreMotion(xyz.ravenbs.event.PreMotionEvent e) {}
    public void onPostMotion(xyz.ravenbs.event.PostMotionEvent e) {}
    public void onPreUpdate() {}
    public void onPostUpdate() {}
    public void onReceivePacket(xyz.ravenbs.event.ReceivePacketEvent e) {}
    public void onSendPacket(xyz.ravenbs.event.SendPacketEvent e) {}
    public void onRenderWorld(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context) {}
    
    public void disable() {
        setEnabled(false);
    }

    public void enable() {
        setEnabled(true);
    }

    public void onDisable() {
        // Override
    }
    
    public void onRender(net.minecraft.client.gui.DrawContext context, float tickDelta) {}

    /** Called after a play connection is established. */
    public void onWorldJoin() {}

    /** Called while disconnecting, before client world state is discarded. */
    public void onWorldLeave() {}
    
    public void onUpdate() {
        // Frame/Tick update
    }
    
    public void onKeyBind() {
        if (mc.currentScreen != null) {
            // Preserve the held state so closing a screen while holding a bind does not retrigger it.
            isToggled = isBindPressed();
            return;
        }
        if (this.keycode != 0) {
           boolean isDown = isBindPressed();
           
           if (isDown && !this.isToggled) {
               this.toggle();
               this.isToggled = true;
           } else if (!isDown) {
               this.isToggled = false;
           }
        }
    }
    
    public int getKeycode() {
        return keycode;
    }
    
    public void setBind(int keycode) {
        this.keycode = keycode;
    }
    
    public void registerSetting(Setting setting) {
        if (setting == null) {
            return;
        }
        if (setting.isPersistent()) {
            setting.assignStorageId("setting_" + persistentSettingCount++);
        }
        this.settings.add(setting);
    }
    
    public List<Setting> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    void disableAfterError() {
        if (!enabled) {
            return;
        }
        enabled = false;
        try {
            onDisable();
        } catch (Throwable cleanupError) {
            ModuleManager.handleModuleError(this, "onDisable after error", cleanupError);
        } finally {
            ModuleSafetyManager.releaseModule(this);
        }
    }

    private boolean isBindPressed() {
        if (this.keycode == 0 || mc.getWindow() == null) {
            return false;
        }
        if (this.keycode < 0) {
            int button = -100 - this.keycode;
            return org.lwjgl.glfw.GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), button) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
        }
        return InputUtil.isKeyPressed(mc.getWindow().getHandle(), this.keycode);
    }
}
