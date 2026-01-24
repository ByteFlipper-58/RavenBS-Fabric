package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;

public class Shaders extends Module {
    private SliderSetting shader;
    private String[] shaderNames = {"Blur", "Bits", "Antialias", "Creeper", "Desaturate", "Flip", "Invert", "Notch", "NTSC", "Outline", "Phosphor", "Sobel", "Spider", "Wobble"};

    public Shaders() {
        super("Shaders", ModuleCategory.render);
        this.registerSetting(new DescriptionSetting("Applies post-processing shaders."));
        this.registerSetting(shader = new SliderSetting("Shader", 0, shaderNames));
    }

    @Override
    public void onEnable() {
        if (mc.gameRenderer == null) {
            this.disable();
            return;
        }
        
        applyShader();
    }

    @Override
    public void onUpdate() {
        // Shader is applied via mixin or entity renderer
    }

    @Override
    public void onDisable() {
        if (mc.gameRenderer != null) {
            mc.gameRenderer.disablePostProcessor();
        }
    }
    
    private void applyShader() {
        // In Fabric 1.20, shaders are loaded differently
        // This would require a mixin to GameRenderer.loadPostProcessor
        // For now, this is a placeholder
    }
    
    public int getShaderIndex() {
        return (int) shader.getInput();
    }
}
