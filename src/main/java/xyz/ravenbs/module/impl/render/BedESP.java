package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.ModeSetting;
import xyz.ravenbs.utility.RenderUtils;
import net.minecraft.block.BedBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.client.util.math.MatrixStack;

public class BedESP extends Module {

    public BedESP() {
        super("BedESP", ModuleCategory.render);
    }

    @Override
    public void onRenderWorld(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context) {
        // Always draw box when module is enabled
        for (BlockEntity be : xyz.ravenbs.utility.Utils.getAllBlockEntities()) {
            if (be instanceof BedBlockEntity) {
                RenderUtils.drawBlockBox(context.matrixStack(), be.getPos(), java.awt.Color.RED);
            }
        }
    }
}
