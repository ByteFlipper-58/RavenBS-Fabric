package xyz.ravenbs.mixin.render;

import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.module.impl.render.Chams;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.minecraft.util.Identifier;
import com.mojang.blaze3d.systems.RenderSystem;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> {

    // Note: Polygon offset approach doesn't work in modern MC due to buffered rendering.
    // Chams through-wall effect is now implemented via MixinEntityGlowing (isGlowing override)
    
    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    private VertexConsumerProvider modifyVertexConsumerProvider(VertexConsumerProvider vertexConsumerProvider, T livingEntity) {
        if (ModuleManager.chams != null && ModuleManager.chams.isEnabled() && livingEntity instanceof PlayerEntity) {
            if (livingEntity != net.minecraft.client.MinecraftClient.getInstance().player) {
                if (Chams.ignoreDepth != null && Chams.ignoreDepth.isToggled()) {
                    return new xyz.ravenbs.utility.ChamsVertexConsumerProvider(vertexConsumerProvider, ((net.minecraft.client.render.entity.EntityRenderer<T>) (Object) this).getTexture(livingEntity), true, false);
                }
            }
        }
        return vertexConsumerProvider;
    }
}
