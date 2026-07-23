package xyz.ravenbs.utility;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.setting.impl.SliderSetting;

/** Shared persisted position and scale for lightweight HUD widgets. */
public final class HudLayout {
    @FunctionalInterface
    public interface PreviewRenderer {
        void render(DrawContext context, int x, int y, float scale);
    }

    private final SliderSetting x;
    private final SliderSetting y;
    private final SliderSetting scale;

    public HudLayout(Module owner, int defaultX, int defaultY) {
        x = new SliderSetting("HUD X", defaultX, 0, 3840, 1);
        y = new SliderSetting("HUD Y", defaultY, 0, 2160, 1);
        scale = new SliderSetting("HUD Scale", 1.0, 0.5, 2.0, 0.05);
        owner.registerSetting(x);
        owner.registerSetting(y);
        owner.registerSetting(scale);
    }

    public int getX() {
        return (int) x.getInput();
    }

    public int getY() {
        return (int) y.getInput();
    }

    public float getScale() {
        return (float) scale.getInput();
    }

    public void openEditor(String title, int width, int height, PreviewRenderer renderer) {
        MinecraftClient.getInstance().setScreen(new EditorScreen(title, width, height, renderer));
    }

    private final class EditorScreen extends Screen {
        private final int widgetWidth;
        private final int widgetHeight;
        private final PreviewRenderer renderer;
        private boolean dragging;
        private int dragX;
        private int dragY;

        private EditorScreen(String title, int widgetWidth, int widgetHeight, PreviewRenderer renderer) {
            super(Text.of(title));
            this.widgetWidth = widgetWidth;
            this.widgetHeight = widgetHeight;
            this.renderer = renderer;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            renderBackground(context);
            renderer.render(context, getX(), getY(), getScale());
            int scaledWidth = Math.round(widgetWidth * getScale());
            int scaledHeight = Math.round(widgetHeight * getScale());
            context.drawBorder(getX(), getY(), scaledWidth, scaledHeight, 0xFFFFFFFF);
            context.drawCenteredTextWithShadow(textRenderer, "Drag HUD. ESC to close.", width / 2, 12, 0xFFFFFFFF);
            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            int scaledWidth = Math.round(widgetWidth * getScale());
            int scaledHeight = Math.round(widgetHeight * getScale());
            if (button == 0 && mouseX >= getX() && mouseX <= getX() + scaledWidth
                    && mouseY >= getY() && mouseY <= getY() + scaledHeight) {
                dragging = true;
                dragX = (int) mouseX - getX();
                dragY = (int) mouseY - getY();
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (dragging) {
                x.setValue(Math.max(0, mouseX - dragX));
                y.setValue(Math.max(0, mouseY - dragY));
                return true;
            }
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            dragging = false;
            return super.mouseReleased(mouseX, mouseY, button);
        }
    }
}
