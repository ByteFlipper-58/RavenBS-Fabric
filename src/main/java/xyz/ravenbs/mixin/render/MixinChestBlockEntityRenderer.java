package xyz.ravenbs.mixin.render;

import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.module.impl.render.ChestESP;
import xyz.ravenbs.utility.ChamsVertexConsumerProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChestBlockEntityRenderer.class)
public class MixinChestBlockEntityRenderer<T extends BlockEntity> {

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    private VertexConsumerProvider modifyVertexConsumerProvider(VertexConsumerProvider vertexConsumerProvider, T entity) {
        if (ModuleManager.chestESP != null && ModuleManager.chestESP.isEnabled()) {
            // Check style: 1=Chams, 2=Both. If 0 (Box), do nothing here.
            int style = (int) ChestESP.style.getInput();
            if (style == 1 || style == 2) {
                 if (entity.getWorld() != null) {
                    // Use standard texture extraction or pass null to let provider extract
                    // For chests, textures vary (normal, double, christmas). Let's trust dynamic extraction or implement logic.
                    // Given previous success with dynamic extraction, we will use a dummy ID or null if allowed, 
                    // BUT BedESP used explicit ID.
                    // Actually, ChestRenderer uses textured layers. Let's try passing the texture if we can get it, or fallback.
                    // Since getting the exact texture here is hard without copying logic, we rely on the DYNAMIC EXTRACTION from string.
                    return new ChamsVertexConsumerProvider(vertexConsumerProvider, null, true, true);
                }
            }
        }
        return vertexConsumerProvider;
    }
}
