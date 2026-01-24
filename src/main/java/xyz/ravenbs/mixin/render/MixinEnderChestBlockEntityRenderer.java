package xyz.ravenbs.mixin.render;

import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.module.impl.render.ChestESP;
import xyz.ravenbs.utility.ChamsVertexConsumerProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChestBlockEntityRenderer.class)
public class MixinEnderChestBlockEntityRenderer<T extends BlockEntity> {

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    private VertexConsumerProvider modifyVertexConsumerProvider(VertexConsumerProvider vertexConsumerProvider, T entity) {
        if (!(entity instanceof EnderChestBlockEntity)) return vertexConsumerProvider;
        if (ModuleManager.chestESP != null && ModuleManager.chestESP.isEnabled()) {
            int style = (int) ChestESP.style.getInput();
            if (style == 1 || style == 2) {
                 if (entity.getWorld() != null) {
                    // Ender Chests have a specific texture, but usually it's handled by the renderer requesting it.
                    // We trust dynamic extraction again.
                    return new ChamsVertexConsumerProvider(vertexConsumerProvider, null, true, true);
                }
            }
        }
        return vertexConsumerProvider;
    }
}
