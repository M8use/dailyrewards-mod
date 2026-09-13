package net.execheinz.dailyrewards.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;

public class StyledButton extends Button {
   private static final int DISABLED_BORDER = -14012612;
   private static final int DISABLED_TOP = -14209992;
   private static final int DISABLED_BOTTOM = -14670802;
   private static final int DISABLED_TEXT = -10854030;
   private final int border;
   private final int top;
   private final int bottom;
   private final int textColor;

   public StyledButton(int x, int y, int width, int height, Component message, int border, int top, int bottom, int textColor, OnPress onPress) {
      super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
      this.border = border;
      this.top = top;
      this.bottom = bottom;
      this.textColor = textColor;
   }

   /** Bright gold banner, matching typical "claim"/"confirm" game buttons. */
   public static StyledButton gold(int x, int y, int width, int height, Component message, OnPress onPress) {
      return new StyledButton(x, y, width, height, message, 0xFFB8790A, 0xFFFFD24C, 0xFFF2A31C, 0xFF3C2400, onPress);
   }

   /** Bright red pill/square, matching typical "close"/"locked" game buttons. */
   public static StyledButton red(int x, int y, int width, int height, Component message, OnPress onPress) {
      return new StyledButton(x, y, width, height, message, 0xFF8E1616, 0xFFF15A4E, 0xFFD8332A, 0xFFFFFFFF, onPress);
   }

   @Override
   protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      int x1 = this.getX();
      int y1 = this.getY();
      int x2 = x1 + this.width;
      int y2 = y1 + this.height;
      boolean lit = this.active && this.isHovered();
      int borderColor = this.active ? (lit ? WheelRenderer.lerpColor(this.border, -1, 0.35F) : this.border) : DISABLED_BORDER;
      int topColor = this.active ? (lit ? WheelRenderer.lerpColor(this.top, -1, 0.18F) : this.top) : DISABLED_TOP;
      int bottomColor = this.active ? (lit ? WheelRenderer.lerpColor(this.bottom, -1, 0.18F) : this.bottom) : DISABLED_BOTTOM;
      WheelRenderer.roundedRect(graphics, x1, y1, x2, y2, 4, borderColor);
      WheelRenderer.roundedRect(graphics, x1 + 1, y1 + 1, x2 - 1, y2 - 1, 3, topColor, bottomColor);
      Font font = Minecraft.getInstance().font;
      int labelColor = this.active ? this.textColor : DISABLED_TEXT;
      int textWidth = font.width(this.getMessage());
      int textX = x1 + (this.width - textWidth) / 2;
      int textY = y1 + (this.height - 8) / 2;
      graphics.drawString(font, this.getMessage(), textX, textY, labelColor, false);
   }
}
