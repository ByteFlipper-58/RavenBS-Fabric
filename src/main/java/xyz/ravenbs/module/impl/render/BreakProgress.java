package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import net.minecraft.client.util.math.MatrixStack;
import xyz.ravenbs.utility.RenderUtils;
import net.minecraft.util.math.BlockPos;

public class BreakProgress extends Module {
    public BreakProgress() {
        super("BreakProgress", ModuleCategory.render);
    }
    
    // Requires mixin to RenderGlobal or access to world.getBlockBreakingProgress()
    // Fabric API implies mixin for this.
    // Simplifying: If we assume we don't have mixin for break progress list map, we can't implement.
    // RavenBS usually highlights the block being broken.
    
    @Override
    public void onRenderWorld(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context) {
         if (mc.interactionManager.isBreakingBlock()) {
             // Accessor needed for currentBlockPos
             // stub
         }
    }
}
