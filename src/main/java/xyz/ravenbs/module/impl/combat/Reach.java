package xyz.ravenbs.module.impl.combat;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;

public class Reach extends Module {
    public static SliderSetting min;
    public static SliderSetting max;
    public static SliderSetting weaponOnly;

    public Reach() {
        super("Reach", ModuleCategory.combat);
        this.registerSetting(min = new SliderSetting("Min", 3.1, 3.0, 6.0, 0.05));
        this.registerSetting(max = new SliderSetting("Max", 3.3, 3.0, 6.0, 0.05));
        this.registerSetting(weaponOnly = new SliderSetting("Weapon only", 0, 0, 1, 1)); // Boolean as slider for now or we can use Button
    }

    public static double getReach() {
        if (min.getInput() >= max.getInput()) {
            return min.getInput();
        }
        return min.getInput() + Utils.random.nextDouble() * (max.getInput() - min.getInput());
    }
}
