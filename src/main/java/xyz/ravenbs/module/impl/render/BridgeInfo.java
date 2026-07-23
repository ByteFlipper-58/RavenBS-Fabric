package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.utility.HudLayout;
import net.minecraft.client.gui.DrawContext;

public class BridgeInfo extends Module {
    private final HudLayout layout;
    private final ButtonSetting editPosition;

    public BridgeInfo() {
        super("BridgeInfo", ModuleCategory.render);
        layout = new HudLayout(this, 10, 100);
        editPosition = new ButtonSetting("Edit position", false);
        registerSetting(editPosition);
    }
    
    // Logic mostly handled in HUD for simplicity, or we can hook here.
    // Let's hook into HUD rendering or generic render event.
    // For simplicity, we'll expose a static string that HUD can pick up, or render directly.
    
    // Ideally BridgeInfo renders "Blocks To Block: X"
    
    // Actually, let's implement a simple direct renderer.
    
    @Override
    public void onUpdate() {
        if (editPosition.isToggled()) {
            layout.openEditor("BridgeInfo Editor", 90, 12, this::renderPreview);
            editPosition.setEnabled(false);
        }
    }

    private void render(DrawContext context, float tickDelta) {
        if (!isEnabled() || mc.player == null) return;
        String text = "Blocks: " + getBlocksToBlock();
        renderText(context, text, layout.getX(), layout.getY(), layout.getScale());
    }

    private void renderPreview(DrawContext context, int x, int y, float scale) {
        renderText(context, "Blocks: 12", x, y, scale);
    }

    private void renderText(DrawContext context, String text, int x, int y, float scale) {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.drawText(mc.textRenderer, text, 0, 0, -1, true);
        context.getMatrices().pop();
    }
    
    private String getBlocksToBlock() {
        if (mc.player == null) return "?";
        double y = mc.player.getY();
        
        // Raycast down to find ground
        // Simplified: just check distance to nearest block below
        for (int i = 0; i < 50; i++) {
             if (!mc.world.isAir(mc.player.getBlockPos().down(i))) {
                 return String.valueOf(i - 1);
             }
        }
        return ">50";
    }
    
    // Use the HUD callback
    public static void onRender(DrawContext context) {
         if (xyz.ravenbs.module.ModuleManager.bridgeInfo != null && xyz.ravenbs.module.ModuleManager.bridgeInfo.isEnabled()) {
             ((BridgeInfo)xyz.ravenbs.module.ModuleManager.bridgeInfo).render(context, 1.0f);
         }
    }
}
