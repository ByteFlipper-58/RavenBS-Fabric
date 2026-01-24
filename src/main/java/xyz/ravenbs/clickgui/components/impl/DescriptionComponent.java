package xyz.ravenbs.clickgui.components.impl;

import xyz.ravenbs.clickgui.components.Component;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

public class DescriptionComponent extends Component {
    private final DescriptionSetting setting;
    private final ModuleComponent parent;
    private final int offset;

    public DescriptionComponent(DescriptionSetting setting, ModuleComponent parent, int offset) {
        this.setting = setting;
        this.parent = parent;
        this.offset = offset;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = parent.getParent().x;
        int y = parent.getParent().y + parent.getParent().height + parent.getOffset() + this.offset;
        int width = parent.getParent().width;
        int height = 12; // Smaller height for desc
        
        context.fill(x, y, x + width, y + height, new Color(0, 0, 0, 140).getRGB());
        context.drawText(MinecraftClient.getInstance().textRenderer, setting.getLocalizedName(), x + 4, y + 2, Color.GRAY.getRGB(), true);
    }
    
    public int getHeight() {
        return 12;
    }
}
