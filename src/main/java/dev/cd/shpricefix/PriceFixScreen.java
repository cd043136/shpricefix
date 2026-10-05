package dev.cd.shpricefix;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

/**
 * Centered config popup opened by {@code /pricefix}. The world stays visible and blurred behind it.
 */
public final class PriceFixScreen extends Screen {
    private static final int PANEL_W = 236;
    private static final int PAD = 8;
    private static final int SEG_H = 22;
    private static final int ROW_H = 22;
    private static final int OPTION_BTN_W = 42;
    private static final int OPTION_BTN_H = 16;
    private static final int FADE_ALPHA = 158;

    private static final int PANEL = 0xFF161218;
    private static final int BRASS = 0xFF7A6244;
    private static final int SCRIM = 0x3308040A;
    private static final int SEG_BG = 0xFF0C0A0E;
    private static final int SEG_EDGE = 0xFF2C241C;
    private static final int LIST_BG = 0xFF100C10;
    private static final int LIST_EDGE = 0xFF2A241C;
    private static final int ROW_LINE = 0xFF241C18;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GOLD = 0xFFFFAA00;
    private static final int MUTED = 0xFF777777;
    private static final int OFF_BG = 0xFF5A1E24;
    private static final int OFF_INK = 0xFFFF5555;
    private static final int ON_BG = 0xFF145024;
    private static final int ON_INK = 0xFF55FF55;
    private static final int HOVER = 0x22FFFFFF;

    private static final String[] OPTIONS = {
        "The One IV Bundle",
        "Unfanged Vampire Part",
        "Quantum Bundle"
    };

    public PriceFixScreen() {
        super(Component.literal("Price Fix"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.extractBlurredBackground(graphics);
        graphics.fill(0, 0, this.width, this.height, SCRIM);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        Layout layout = this.layout();
        boolean enabled = PriceFix.enabled();

        graphics.fill(layout.panel.x - 1, layout.panel.y - 1, layout.panel.x + layout.panel.w + 1, layout.panel.y + layout.panel.h + 1, BRASS);
        graphics.fill(layout.panel.x, layout.panel.y, layout.panel.x + layout.panel.w, layout.panel.y + layout.panel.h, PANEL);

        this.drawLeft(graphics, "Main Toggle", layout.mainLabelX, layout.mainLabelY, WHITE);
        this.drawSegment(graphics, layout, mouseX, mouseY, enabled);

        this.drawLeft(graphics, "Options", layout.optionsLabelX, layout.optionsLabelY, GOLD);

        graphics.fill(layout.list.x, layout.list.y, layout.list.x + layout.list.w, layout.list.y + layout.list.h, tone(LIST_BG, enabled));
        graphics.outline(layout.list.x, layout.list.y, layout.list.w, layout.list.h, tone(LIST_EDGE, enabled));
        for (int i = 0; i < OPTIONS.length; i++) {
            Rect row = layout.rows[i];
            if (i > 0) {
                graphics.fill(layout.list.x, row.y, layout.list.x + layout.list.w, row.y + 1, tone(ROW_LINE, enabled));
            }
            int textY = row.y + (row.h - this.font.lineHeight) / 2;
            this.drawLeft(graphics, OPTIONS[i], layout.list.x + 6, textY, tone(WHITE, enabled));
            boolean on = optionOn(i);
            this.drawState(graphics, layout.options[i], on, enabled, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        Layout layout = this.layout();
        double x = event.x();
        double y = event.y();
        if (x < layout.panel.x - 1 || y < layout.panel.y - 1
            || x >= layout.panel.x + layout.panel.w + 1
            || y >= layout.panel.y + layout.panel.h + 1) {
            this.onClose();
            return true;
        }
        if (layout.off.contains(x, y)) {
            this.setMaster(false);
            return true;
        }
        if (layout.on.contains(x, y)) {
            this.setMaster(true);
            return true;
        }
        if (PriceFix.enabled()) {
            for (int i = 0; i < layout.options.length; i++) {
                if (layout.options[i].contains(x, y)) {
                    this.flipOption(i);
                    return true;
                }
            }
        }
        return true;
    }

    private void drawSegment(GuiGraphicsExtractor graphics, Layout layout, int mouseX, int mouseY, boolean enabled) {
        graphics.fill(layout.segment.x, layout.segment.y, layout.segment.x + layout.segment.w, layout.segment.y + layout.segment.h, SEG_BG);
        graphics.outline(layout.segment.x, layout.segment.y, layout.segment.w, layout.segment.h, SEG_EDGE);
        this.drawChoice(graphics, layout.off, "Off", !enabled, true, mouseX, mouseY);
        this.drawChoice(graphics, layout.on, "On", enabled, false, mouseX, mouseY);
    }

    private void drawChoice(GuiGraphicsExtractor graphics, Rect rect, String label, boolean selected, boolean off, int mouseX, int mouseY) {
        boolean hovered = rect.contains(mouseX, mouseY);
        if (selected) {
            graphics.fill(rect.x, rect.y, rect.x + rect.w, rect.y + rect.h, off ? OFF_BG : ON_BG);
            graphics.outline(rect.x, rect.y, rect.w, rect.h, off ? OFF_INK : ON_INK);
        }
        if (hovered) {
            graphics.fill(rect.x, rect.y, rect.x + rect.w, rect.y + rect.h, HOVER);
        }
        int color = selected ? (off ? OFF_INK : ON_INK) : (hovered ? WHITE : MUTED);
        this.drawCentered(graphics, label, rect, color);
    }

    private void drawState(GuiGraphicsExtractor graphics, Rect rect, boolean on, boolean live, int mouseX, int mouseY) {
        boolean hovered = live && rect.contains(mouseX, mouseY);
        graphics.fill(rect.x, rect.y, rect.x + rect.w, rect.y + rect.h, tone(on ? ON_BG : OFF_BG, live));
        graphics.outline(rect.x, rect.y, rect.w, rect.h, tone(on ? ON_INK : OFF_INK, live));
        if (hovered) {
            graphics.fill(rect.x, rect.y, rect.x + rect.w, rect.y + rect.h, HOVER);
        }
        this.drawCentered(graphics, on ? "On" : "Off", rect, tone(on ? ON_INK : OFF_INK, live));
    }

    private void drawLeft(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
        graphics.text(this.font, text, x, y, color, true);
    }

    private void drawCentered(GuiGraphicsExtractor graphics, String text, Rect rect, int color) {
        int x = rect.x + (rect.w - this.font.width(text)) / 2;
        int y = rect.y + (rect.h - this.font.lineHeight) / 2;
        graphics.text(this.font, text, x, y, color, true);
    }

    private void setMaster(boolean on) {
        if (PriceFix.enabled() == on) {
            return;
        }
        String failure = PriceFix.setEnabled(on);
        if (failure != null) {
            if (this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(Component.literal(failure).withStyle(ChatFormatting.RED));
            }
            return;
        }
        this.playClick();
    }

    private void flipOption(int index) {
        switch (index) {
            case 0 -> PriceFix.setTheOneBundle(!PriceFix.theOneBundle());
            case 1 -> PriceFix.setUnfanged(!PriceFix.unfanged());
            case 2 -> PriceFix.setQuantum(!PriceFix.quantum());
            default -> {
                return;
            }
        }
        this.playClick();
    }

    private static boolean optionOn(int index) {
        return switch (index) {
            case 0 -> PriceFix.theOneBundle();
            case 1 -> PriceFix.unfanged();
            case 2 -> PriceFix.quantum();
            default -> false;
        };
    }

    private void playClick() {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private Layout layout() {
        int labelH = this.font.lineHeight;
        int panelH = PAD + labelH + 4 + SEG_H + 6 + labelH + 4 + ROW_H * OPTIONS.length + PAD;
        int left = Math.max(8, (this.width - PANEL_W) / 2);
        int top = Math.max(8, (this.height - panelH) / 2);
        int x = left + PAD;
        int y = top + PAD;
        int innerW = PANEL_W - PAD * 2;

        int mainLabelY = y;
        y += labelH + 4;
        Rect segment = new Rect(x, y, innerW, SEG_H);
        int inset = 2;
        int gap = 2;
        int choiceW = (innerW - inset * 2 - gap) / 2;
        int choiceH = SEG_H - inset * 2;
        int choiceY = y + inset;
        Rect off = new Rect(x + inset, choiceY, choiceW, choiceH);
        Rect on = new Rect(off.x + choiceW + gap, choiceY, choiceW, choiceH);

        y += SEG_H + 6;
        int optionsLabelY = y;
        y += labelH + 4;
        Rect list = new Rect(x, y, innerW, ROW_H * OPTIONS.length);
        Rect[] rows = new Rect[OPTIONS.length];
        Rect[] options = new Rect[OPTIONS.length];
        for (int i = 0; i < OPTIONS.length; i++) {
            int rowY = y + i * ROW_H;
            rows[i] = new Rect(x, rowY, innerW, ROW_H);
            options[i] = new Rect(
                x + innerW - 6 - OPTION_BTN_W,
                rowY + (ROW_H - OPTION_BTN_H) / 2,
                OPTION_BTN_W,
                OPTION_BTN_H
            );
        }
        return new Layout(
            new Rect(left, top, PANEL_W, panelH),
            x,
            mainLabelY,
            segment,
            off,
            on,
            x,
            optionsLabelY,
            list,
            rows,
            options
        );
    }

    private static int tone(int argb, boolean live) {
        if (live) {
            return argb;
        }
        int alpha = (argb >>> 24) & 0xFF;
        if (alpha == 0) {
            alpha = 0xFF;
        }
        return ((alpha * FADE_ALPHA / 255) << 24) | (argb & 0xFFFFFF);
    }

    private record Rect(int x, int y, int w, int h) {
        private boolean contains(double mx, double my) {
            return mx >= this.x && my >= this.y && mx < this.x + this.w && my < this.y + this.h;
        }
    }

    private record Layout(
        Rect panel,
        int mainLabelX,
        int mainLabelY,
        Rect segment,
        Rect off,
        Rect on,
        int optionsLabelX,
        int optionsLabelY,
        Rect list,
        Rect[] rows,
        Rect[] options
    ) {
    }
}
