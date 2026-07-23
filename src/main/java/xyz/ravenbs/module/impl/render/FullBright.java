package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;

public class FullBright extends Module {
    private Double originalGamma = null;

    public FullBright() {
        super("FullBright", ModuleCategory.render);
    }

    @Override
    public void onEnable() {
        if (mc.options != null && originalGamma == null) {
            originalGamma = mc.options.getGamma().getValue();
        }
        if (mc.options != null) {
            mc.options.getGamma().setValue(100.0);
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null && originalGamma != null) {
            mc.options.getGamma().setValue(originalGamma);
            originalGamma = null;
        }
    }

    @Override
    public void onWorldLeave() {
        if (mc.options != null && originalGamma != null) {
            mc.options.getGamma().setValue(originalGamma);
        }
    }

    @Override
    public void onWorldJoin() {
        if (mc.options != null && isEnabled()) {
            mc.options.getGamma().setValue(100.0);
        }
    }
}
