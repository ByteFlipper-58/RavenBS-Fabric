package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.utility.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.util.math.Box;

import java.awt.Color;

public class ChestESP extends Module {
    public static xyz.ravenbs.module.setting.impl.ModeSetting style;

    public ChestESP() {
        super("ChestESP", ModuleCategory.render);
        this.registerSetting(style = new xyz.ravenbs.module.setting.impl.ModeSetting("Style", new String[]{"Box", "Chams", "Both"}, 0));
    }

    @Override
    public void onRenderWorld(WorldRenderContext context) {
        if (mc.world == null) return;
        
        int mode = (int) style.getInput(); // 0=Box, 1=Chams, 2=Both
        if (mode == 1) return; // Chams only, skip box

        for (BlockEntity blockEntity : xyz.ravenbs.utility.Utils.getAllBlockEntities()) {
            
            if (blockEntity instanceof ChestBlockEntity || blockEntity instanceof EnderChestBlockEntity) {
                Box box = new Box(blockEntity.getPos());
                Color color = (blockEntity instanceof EnderChestBlockEntity) ? Color.MAGENTA : Color.ORANGE;
                
                RenderUtils.drawBoxLines(context, box, color);
            }
        }
    }
}
