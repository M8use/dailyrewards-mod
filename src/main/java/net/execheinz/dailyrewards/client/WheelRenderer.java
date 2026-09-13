package net.execheinz.dailyrewards.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.Arrays;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

public final class WheelRenderer {
   private static final int AA = 4;
   private static final WheelRenderer.HexRuns[] HEX_CACHE = new WheelRenderer.HexRuns[]{new WheelRenderer.HexRuns(), new WheelRenderer.HexRuns()};
   private static int hexSlot;
   private static final int OFFSET = 32768;

   public static void arc(GuiGraphics graphics, float cx, float cy, float innerRadius, float outerRadius, float startDeg, float sweepDeg, int argb) {
      arc(graphics, cx, cy, innerRadius, outerRadius, startDeg, sweepDeg, argb, argb);
   }

   public static void arc(
      GuiGraphics graphics, float cx, float cy, float innerRadius, float outerRadius, float startDeg, float sweepDeg, int fromArgb, int toArgb
   ) {
      if (!(sweepDeg <= 0.0F)) {
         sweepDeg = Math.min(sweepDeg, 360.0F);
         int segments = Math.max(2, Math.round(sweepDeg / 2.0F));
         Matrix4f matrix = graphics.pose().last().pose();
         BufferBuilder buffer = begin(graphics, Mode.TRIANGLE_STRIP);

         for (int i = 0; i <= segments; i++) {
            float t = (float)i / (float)segments;
            int color = lerpColor(fromArgb, toArgb, t);
            double angle = Math.toRadians((double)(startDeg + sweepDeg * t));
            float sin = (float)Math.sin(angle);
            float cos = (float)Math.cos(angle);
            vertex(buffer, matrix, cx + sin * outerRadius, cy - cos * outerRadius, color);
            vertex(buffer, matrix, cx + sin * innerRadius, cy - cos * innerRadius, color);
         }

         end(buffer);
      }
   }

   public static void movingArc(GuiGraphics graphics, float cx, float cy, float innerRadius, float outerRadius, float startDeg, float sweepDeg, int argb) {
      if (!(sweepDeg <= 0.0F)) {
         sweepDeg = Math.min(sweepDeg, 360.0F);
         arc(graphics, cx, cy, innerRadius, outerRadius, startDeg, sweepDeg, argb);
      }
   }

   public static void rainbowArc(
      GuiGraphics graphics,
      float cx,
      float cy,
      float innerRadius,
      float outerRadius,
      float startDeg,
      float sweepDeg,
      float phaseDeg,
      float saturation,
      float brightness
   ) {
      if (!(sweepDeg <= 0.0F)) {
         sweepDeg = Math.min(sweepDeg, 360.0F);
         int segments = Math.max(2, Math.round(sweepDeg / 2.0F));
         Matrix4f matrix = graphics.pose().last().pose();
         BufferBuilder buffer = begin(graphics, Mode.TRIANGLE_STRIP);

         for (int i = 0; i <= segments; i++) {
            float t = (float)i / (float)segments;
            float angle = startDeg + sweepDeg * t;
            int color = hsb(phaseDeg + angle, saturation, brightness);
            double rad = Math.toRadians((double)angle);
            float sin = (float)Math.sin(rad);
            float cos = (float)Math.cos(rad);
            vertex(buffer, matrix, cx + sin * outerRadius, cy - cos * outerRadius, color);
            vertex(buffer, matrix, cx + sin * innerRadius, cy - cos * innerRadius, color);
         }

         end(buffer);
      }
   }

   public static void disc(GuiGraphics graphics, float cx, float cy, float radius, int argb) {
      arc(graphics, cx, cy, 0.0F, radius, 0.0F, 360.0F, argb);
   }

   public static void tickRing(
      GuiGraphics graphics,
      float cx,
      float cy,
      float innerRadius,
      float outerRadius,
      float rotationDeg,
      int count,
      int majorEvery,
      int minorArgb,
      int majorArgb
   ) {
      Matrix4f matrix = graphics.pose().last().pose();
      BufferBuilder buffer = begin(graphics, Mode.QUADS);

      for (int i = 0; i < count; i++) {
         boolean major = i % majorEvery == 0;
         int color = major ? majorArgb : minorArgb;
         float inner = major ? innerRadius - 1.5F : innerRadius;
         float outer = major ? outerRadius + 1.0F : outerRadius;
         float half = major ? 0.9F : 0.5F;
         double angle = Math.toRadians((double)(rotationDeg + 360.0F * (float)i / (float)count));
         float sin = (float)Math.sin(angle);
         float cos = (float)Math.cos(angle);
         float px = cos * half;
         float py = sin * half;
         vertex(buffer, matrix, cx + sin * outer + px, cy - cos * outer + py, color);
         vertex(buffer, matrix, cx + sin * inner + px, cy - cos * inner + py, color);
         vertex(buffer, matrix, cx + sin * inner - px, cy - cos * inner - py, color);
         vertex(buffer, matrix, cx + sin * outer - px, cy - cos * outer - py, color);
      }

      end(buffer);
   }

   public static void pointer(GuiGraphics graphics, float cx, float cy, float tipRadius, float baseRadius, float angleDeg, float halfWidth, int argb) {
      double angle = Math.toRadians((double)angleDeg);
      float sin = (float)Math.sin(angle);
      float cos = (float)Math.cos(angle);
      float uy = -cos;
      float baseX = cx + sin * baseRadius;
      float baseY = cy + uy * baseRadius;
      triangle(
         graphics,
         cx + sin * tipRadius,
         cy + uy * tipRadius,
         baseX + cos * halfWidth,
         baseY + sin * halfWidth,
         baseX - cos * halfWidth,
         baseY - sin * halfWidth,
         argb
      );
   }

   public static void triangle(GuiGraphics graphics, float x1, float y1, float x2, float y2, float x3, float y3, int argb) {
      Matrix4f matrix = graphics.pose().last().pose();
      BufferBuilder buffer = begin(graphics, Mode.QUADS);
      vertex(buffer, matrix, x1, y1, argb);
      vertex(buffer, matrix, x2, y2, argb);
      vertex(buffer, matrix, x3, y3, argb);
      vertex(buffer, matrix, x3, y3, argb);
      end(buffer);
   }

   public static void thickLine(GuiGraphics graphics, float x1, float y1, float x2, float y2, float halfWidth, int argb) {
      Matrix4f matrix = graphics.pose().last().pose();
      BufferBuilder buffer = begin(graphics, Mode.QUADS);
      segment(buffer, matrix, x1, y1, x2, y2, halfWidth, argb);
      end(buffer);
   }

   /** Like {@link #arc}, but fades from full alpha at the inner edge to fully transparent at the outer edge. */
   public static void radialFadeArc(GuiGraphics graphics, float cx, float cy, float innerRadius, float outerRadius, float startDeg, float sweepDeg, int argb) {
      if (!(sweepDeg <= 0.0F)) {
         sweepDeg = Math.min(sweepDeg, 360.0F);
         int segments = Math.max(2, Math.round(sweepDeg / 6.0F));
         Matrix4f matrix = graphics.pose().last().pose();
         BufferBuilder buffer = begin(graphics, Mode.TRIANGLE_STRIP);
         int innerColor = argb;
         int outerColor = argb & 16777215;

         for (int i = 0; i <= segments; i++) {
            float t = (float)i / (float)segments;
            double angle = Math.toRadians((double)(startDeg + sweepDeg * t));
            float sin = (float)Math.sin(angle);
            float cos = (float)Math.cos(angle);
            vertex(buffer, matrix, cx + sin * outerRadius, cy - cos * outerRadius, outerColor);
            vertex(buffer, matrix, cx + sin * innerRadius, cy - cos * innerRadius, innerColor);
         }

         end(buffer);
      }
   }

   /**
    * A square, pixel-bevelled frame in the classic Minecraft GUI style: light highlight on the
    * top/left edges, dark shadow on the bottom/right edges, around a solid outline.
    */
   public static void pixelBevelBorder(GuiGraphics graphics, int x1, int y1, int x2, int y2, int outline, int highlight, int shadow) {
      graphics.fill(x1, y1, x2, y1 + 1, outline);
      graphics.fill(x1, y2 - 1, x2, y2, outline);
      graphics.fill(x1, y1, x1 + 1, y2, outline);
      graphics.fill(x2 - 1, y1, x2, y2, outline);
      graphics.fill(x1 + 1, y1 + 1, x2 - 1, y1 + 2, highlight);
      graphics.fill(x1 + 1, y1 + 1, x1 + 2, y2 - 1, highlight);
      graphics.fill(x1 + 1, y2 - 2, x2 - 1, y2 - 1, shadow);
      graphics.fill(x2 - 2, y1 + 1, x2 - 1, y2 - 1, shadow);
   }

   /**
    * The small pixel-art calendar icon shared by the inventory button and the daily rewards
    * screen header: a three-ring binding, a white body, a colored header strip, and a thin
    * accent underline near the bottom.
    */
   public static void calendarIcon(GuiGraphics graphics, float cx, float cy, float scale, int outline, int bodyTop, int bodyBottom, int header, int ring) {
      float left = cx - 7.0F * scale;
      float right = cx + 7.0F * scale;
      float top = cy - 7.0F * scale;
      float bottom = cy + 7.0F * scale;
      graphics.fill(Math.round(cx - 5.6F * scale), Math.round(top), Math.round(cx - 4.3F * scale), Math.round(top + 4.0F * scale), ring);
      graphics.fill(Math.round(cx - 0.7F * scale), Math.round(top), Math.round(cx + 0.7F * scale), Math.round(top + 4.0F * scale), ring);
      graphics.fill(Math.round(cx + 4.3F * scale), Math.round(top), Math.round(cx + 5.6F * scale), Math.round(top + 4.0F * scale), ring);
      card(
         graphics, Math.round(left), Math.round(top + 3.0F * scale), Math.round(right), Math.round(bottom), Math.max(1, Math.round(3.0F * scale)), outline, bodyTop, bodyBottom
      );
      graphics.fill(Math.round(left + 2.0F * scale), Math.round(top + 5.0F * scale), Math.round(right - 2.0F * scale), Math.round(top + 9.0F * scale), header);
      graphics.fill(Math.round(left + 2.0F * scale), Math.round(bottom - 3.0F * scale), Math.round(right - 2.0F * scale), Math.round(bottom - 2.0F * scale), header);
   }

   /**
    * A simple layered flame icon: a pointed-top, rounded-bottom silhouette in three nested colors
    * (outer/middle/inner), similar in spirit to a flat flame emoji.
    */
   public static void flame(GuiGraphics graphics, float cx, float cy, float scale, int outerColor, int middleColor, int innerColor) {
      flameLayer(graphics, cx, cy, scale, 1.0F, outerColor);
      flameLayer(graphics, cx, cy + 1.2F * scale, scale, 0.68F, middleColor);
      flameLayer(graphics, cx, cy + 2.6F * scale, scale, 0.36F, innerColor);
   }

   private static void flameLayer(GuiGraphics graphics, float cx, float cy, float scale, float size, int argb) {
      float tipY = cy - 7.0F * scale * size;
      float shoulderY = cy - 1.5F * scale * size;
      float width = 4.2F * scale * size;
      triangle(graphics, cx, tipY, cx - width, shoulderY, cx + width, shoulderY, argb);
      disc(graphics, cx, cy + 1.5F * scale * size, width, argb);
   }

   public static void hexGrid(GuiGraphics graphics, int x1, int y1, int x2, int y2, float size, int argb) {
      graphics.enableScissor(x1, y1, x2, y2);
      Matrix4f matrix = graphics.pose().last().pose();
      BufferBuilder buffer = begin(graphics, Mode.QUADS);
      float stepX = size * 1.5F;
      float stepY = (float)(Math.sqrt(3.0) * (double)size);
      int cols = (int)Math.ceil((double)((float)(x2 - x1) / stepX)) + 2;
      int rows = (int)Math.ceil((double)((float)(y2 - y1) / stepY)) + 2;

      for (int col = -1; col < cols; col++) {
         for (int row = -1; row < rows; row++) {
            float cx = (float)x1 + (float)col * stepX;
            float cy = (float)y1 + (float)row * stepY + (col % 2 == 0 ? 0.0F : stepY * 0.5F);

            for (int edge = 0; edge < 6; edge++) {
               double a1 = Math.toRadians(60.0 * (double)edge);
               double a2 = Math.toRadians(60.0 * (double)(edge + 1));
               segment(
                  buffer,
                  matrix,
                  cx + (float)Math.cos(a1) * size,
                  cy + (float)Math.sin(a1) * size,
                  cx + (float)Math.cos(a2) * size,
                  cy + (float)Math.sin(a2) * size,
                  0.5F,
                  argb
               );
            }
         }
      }

      end(buffer);
      graphics.disableScissor();
   }

   public static void hexGridRainbow(
      GuiGraphics graphics, int x1, int y1, int x2, int y2, float size, float phaseDeg, float saturation, float brightness, int alpha
   ) {
      graphics.enableScissor(x1, y1, x2, y2);
      Matrix4f matrix = graphics.pose().last().pose();
      BufferBuilder buffer = begin(graphics, Mode.QUADS);
      float stepX = size * 1.5F;
      float stepY = (float)(Math.sqrt(3.0) * (double)size);
      int cols = (int)Math.ceil((double)((float)(x2 - x1) / stepX)) + 2;
      int rows = (int)Math.ceil((double)((float)(y2 - y1) / stepY)) + 2;
      float span = Math.max(1.0F, (float)(x2 - x1));
      int alphaBits = (alpha & 0xFF) << 24;

      for (int col = -1; col < cols; col++) {
         for (int row = -1; row < rows; row++) {
            float cx = (float)x1 + (float)col * stepX;
            float cy = (float)y1 + (float)row * stepY + (col % 2 == 0 ? 0.0F : stepY * 0.5F);
            float hue = phaseDeg + (cx - (float)x1) / span * 360.0F;
            int argb = alphaBits | hsb(hue, saturation, brightness) & 16777215;

            for (int edge = 0; edge < 6; edge++) {
               double a1 = Math.toRadians(60.0 * (double)edge);
               double a2 = Math.toRadians(60.0 * (double)(edge + 1));
               segment(
                  buffer,
                  matrix,
                  cx + (float)Math.cos(a1) * size,
                  cy + (float)Math.sin(a1) * size,
                  cx + (float)Math.cos(a2) * size,
                  cy + (float)Math.sin(a2) * size,
                  0.5F,
                  argb
               );
            }
         }
      }

      end(buffer);
      graphics.disableScissor();
   }

   public static void diagonalStripes(GuiGraphics graphics, int x1, int y1, int x2, int y2, float stripeWidth, int baseArgb, int stripeArgb) {
      int width = x2 - x1;
      int height = y2 - y1;
      if (width > 0 && height > 0) {
         graphics.fill(x1, y1, x2, y2, baseArgb);
         graphics.enableScissor(x1, y1, x2, y2);
         Matrix4f matrix = graphics.pose().last().pose();
         BufferBuilder buffer = begin(graphics, Mode.QUADS);
         float step = stripeWidth * 2.0F;
         float halfWidth = stripeWidth * 0.5F;
         float reach = (float)(width + height);
         float dir = 0.70710677F;

         for (float offset = -(float)height; offset <= (float)width + (float)height; offset += step) {
            float baseX = (float)x1 + offset;
            float baseY = (float)y1;
            segment(buffer, matrix, baseX - reach * dir, baseY - reach * dir, baseX + reach * dir, baseY + reach * dir, halfWidth, stripeArgb);
         }

         end(buffer);
         graphics.disableScissor();
      }
   }

   public static void roundedRect(GuiGraphics graphics, int x1, int y1, int x2, int y2, int radius, int topArgb, int bottomArgb) {
      int height = y2 - y1;
      if (height > 0 && x2 > x1) {
         radius = Math.min(radius, Math.min(height / 2, (x2 - x1) / 2));

         for (int y = y1; y < y2; y++) {
            int fromEdge = Math.min(y - y1, y2 - 1 - y);
            int inset = fromEdge >= radius
               ? 0
               : radius - (int)Math.round(Math.sqrt((double)(radius * radius - (radius - 1 - fromEdge) * (radius - 1 - fromEdge))));
            int color = lerpColor(topArgb, bottomArgb, (float)(y - y1) / (float)Math.max(1, height - 1));
            graphics.fill(x1 + inset, y, x2 - inset, y + 1, color);
         }
      }
   }

   public static void roundedRect(GuiGraphics graphics, int x1, int y1, int x2, int y2, int radius, int argb) {
      roundedRect(graphics, x1, y1, x2, y2, radius, argb, argb);
   }

   public static void card(GuiGraphics graphics, int x1, int y1, int x2, int y2, int radius, int borderArgb, int topArgb, int bottomArgb) {
      roundedRect(graphics, x1, y1, x2, y2, radius, borderArgb);
      roundedRect(graphics, x1 + 1, y1 + 1, x2 - 1, y2 - 1, Math.max(1, radius - 1), topArgb, bottomArgb);
   }

   public static void rainbowRect(GuiGraphics graphics, int x1, int y1, int x2, int y2, int radius, float phaseDeg) {
      int width = x2 - x1;
      int height = y2 - y1;
      if (width > 0 && height > 0) {
         radius = Math.min(radius, Math.min(width / 2, height / 2));

         for (int x = x1; x < x2; x++) {
            int fromEdge = Math.min(x - x1, x2 - 1 - x);
            int inset = fromEdge >= radius
               ? 0
               : radius - (int)Math.round(Math.sqrt((double)(radius * radius - (radius - 1 - fromEdge) * (radius - 1 - fromEdge))));
            float hue = phaseDeg + (float)(x - x1) / (float)width * 360.0F;
            graphics.fill(x, y1 + inset, x + 1, y2 - inset, hsb(hue, 0.75F, 1.0F));
         }
      }
   }

   /**
    * Like {@link #rainbowRect}, but the hue winds around the center instead of just sweeping
    * left-to-right, so the card reads as a spinning rainbow spiral instead of vertical stripes.
    * {@code phaseDeg} rotates the whole spiral over time. Cell-batched (not per-pixel) to keep the
    * cost reasonable on a card that redraws every frame.
    */
   public static void spiralRainbowRect(GuiGraphics graphics, int x1, int y1, int x2, int y2, int radius, float phaseDeg) {
      int width = x2 - x1;
      int height = y2 - y1;
      if (width > 0 && height > 0) {
         radius = Math.min(radius, Math.min(width / 2, height / 2));
         float cx = (float)(x1 + x2) / 2.0F;
         float cy = (float)(y1 + y2) / 2.0F;
         int cell = 2;

         for (int y = y1; y < y2; y += cell) {
            int fromEdge = Math.min(y - y1, y2 - 1 - y);
            int inset = fromEdge >= radius
               ? 0
               : radius - (int)Math.round(Math.sqrt((double)(radius * radius - (radius - 1 - fromEdge) * (radius - 1 - fromEdge))));
            int rowY2 = Math.min(y2, y + cell);

            for (int x = x1 + inset; x < x2 - inset; x += cell) {
               float dx = (float)x + (float)cell / 2.0F - cx;
               float dy = (float)y + (float)cell / 2.0F - cy;
               float dist = (float)Math.sqrt((double)(dx * dx + dy * dy));
               float angle = (float)Math.toDegrees(Math.atan2((double)dy, (double)dx));
               float hue = phaseDeg + angle + dist * 2.4F;
               int rowX2 = Math.min(x2 - inset, x + cell);
               graphics.fill(x, y, rowX2, rowY2, hsb(hue, 0.8F, 1.0F));
            }
         }
      }
   }

   public static int hsb(float hueDeg, float saturation, float brightness) {
      float hue = hueDeg % 360.0F;
      if (hue < 0.0F) {
         hue += 360.0F;
      }

      float h = hue / 60.0F;
      int sector = (int)h;
      float frac = h - (float)sector;
      float p = brightness * (1.0F - saturation);
      float q = brightness * (1.0F - saturation * frac);
      float t = brightness * (1.0F - saturation * (1.0F - frac));
      float r;
      float g;
      float b;
      switch (sector) {
         case 1:
            r = q;
            g = brightness;
            b = p;
            break;
         case 2:
            r = p;
            g = brightness;
            b = t;
            break;
         case 3:
            r = p;
            g = q;
            b = brightness;
            break;
         case 4:
            r = t;
            g = p;
            b = brightness;
            break;
         case 5:
            r = brightness;
            g = p;
            b = q;
            break;
         default:
            r = brightness;
            g = t;
            b = p;
      }

      int ri = Math.round(Math.max(0.0F, Math.min(1.0F, r)) * 255.0F);
      int gi = Math.round(Math.max(0.0F, Math.min(1.0F, g)) * 255.0F);
      int bi = Math.round(Math.max(0.0F, Math.min(1.0F, b)) * 255.0F);
      return 0xFF000000 | ri << 16 | gi << 8 | bi;
   }

   public static int chanceColor(float chance) {
      float t = Math.min(1.0F, (float)Math.sqrt((double)Math.max(0.0F, chance)));
      int r = (int)(232.0F + -156.0F * t);
      int g = (int)(59.0F + 158.0F * t);
      int b = (int)(59.0F + 41.0F * t);
      return 0xFF000000 | r << 16 | g << 8 | b;
   }

   public static int lerpColor(int from, int to, float t) {
      if (from == to) {
         return from;
      } else {
         t = Math.max(0.0F, Math.min(1.0F, t));
         int a = lerpChannel(from >>> 24, to >>> 24, t);
         int r = lerpChannel(from >> 16 & 0xFF, to >> 16 & 0xFF, t);
         int g = lerpChannel(from >> 8 & 0xFF, to >> 8 & 0xFF, t);
         int b = lerpChannel(from & 0xFF, to & 0xFF, t);
         return a << 24 | r << 16 | g << 8 | b;
      }
   }

   private static int lerpChannel(int from, int to, float t) {
      return (int)((float)from + (float)(to - from) * t) & 0xFF;
   }

   private static void rows(WheelRenderer.Sink sink, int minX, int minY, int maxX, int maxY, WheelRenderer.Sampler sampler) {
      for (int y = minY; y < maxY; y++) {
         int runStart = -1;
         int runColor = 0;

         for (int x = minX; x <= maxX; x++) {
            int color = x < maxX ? sampler.at(x, y) : 0;
            if (color != 0 && runStart < 0) {
               runStart = x;
               runColor = color;
            } else if (runStart >= 0 && color != runColor) {
               sink.run(runStart, y, x, runColor);
               runStart = color != 0 ? x : -1;
               runColor = color;
            }
         }
      }
   }

   private static WheelRenderer.Sink into(GuiGraphics graphics) {
      return (x1, y, x2, argb) -> graphics.fill(x1, y, x2, y + 1, argb);
   }

   private static WheelRenderer.Sampler hard(WheelRenderer.Mask mask, WheelRenderer.Shade shade) {
      return (x, y) -> mask.test((float)x + 0.5F, (float)y + 0.5F) ? shade.at((float)x + 0.5F, (float)y + 0.5F) : 0;
   }

   private static WheelRenderer.Sampler soft(WheelRenderer.Mask mask, WheelRenderer.Shade shade) {
      return (x, y) -> {
         int hits = 0;

         for (int sy = 0; sy < 4; sy++) {
            float py = (float)y + ((float)sy + 0.5F) / 4.0F;

            for (int sx = 0; sx < 4; sx++) {
               if (mask.test((float)x + ((float)sx + 0.5F) / 4.0F, py)) {
                  hits++;
               }
            }
         }

         if (hits == 0) {
            return 0;
         } else {
            int argb = shade.at((float)x + 0.5F, (float)y + 0.5F);
            int alpha = (argb >>> 24) * hits / 16;
            return alpha == 0 ? 0 : alpha << 24 | argb & 16777215;
         }
      };
   }

   private static float bearing(float dx, float dy) {
      float deg = (float)Math.toDegrees(Math.atan2((double)dx, (double)(-dy)));
      return deg < 0.0F ? deg + 360.0F : deg;
   }

   private static void arcFill(
      GuiGraphics graphics, float cx, float cy, float innerRadius, float outerRadius, float startDeg, float sweepDeg, int fromArgb, int toArgb, boolean smooth
   ) {
      float inner2 = innerRadius * innerRadius;
      float outer2 = outerRadius * outerRadius;
      boolean full = sweepDeg >= 360.0F;
      float wrapped = startDeg % 360.0F;
      float start = wrapped < 0.0F ? wrapped + 360.0F : wrapped;
      WheelRenderer.Mask mask = (px, py) -> {
         float dx = px - cx;
         float dy = py - cy;
         float d2 = dx * dx + dy * dy;
         if (d2 < inner2 || d2 > outer2) {
            return false;
         } else if (full) {
            return true;
         } else {
            float delta = bearing(dx, dy) - start;
            if (delta < 0.0F) {
               delta += 360.0F;
            }

            return delta <= sweepDeg;
         }
      };
      WheelRenderer.Shade shade = (px, py) -> {
         if (fromArgb == toArgb) {
            return fromArgb;
         } else {
            float delta = bearing(px - cx, py - cy) - start;
            if (delta < 0.0F) {
               delta += 360.0F;
            }

            return lerpColor(fromArgb, toArgb, sweepDeg <= 0.0F ? 0.0F : delta / sweepDeg);
         }
      };
      float[] box = full
         ? new float[]{cx - outerRadius, cy - outerRadius, cx + outerRadius, cy + outerRadius}
         : sectorBounds(cx, cy, innerRadius, outerRadius, start, sweepDeg);
      rows(
         into(graphics),
         Math.round(box[0]) - 1,
         Math.round(box[1]) - 1,
         Math.round(box[2]) + 1,
         Math.round(box[3]) + 1,
         smooth ? soft(mask, shade) : hard(mask, shade)
      );
   }

   private static float[] sectorBounds(float cx, float cy, float innerRadius, float outerRadius, float startDeg, float sweepDeg) {
      float minX = Float.MAX_VALUE;
      float minY = Float.MAX_VALUE;
      float maxX = -Float.MAX_VALUE;
      float maxY = -Float.MAX_VALUE;

      for (int edge = 0; edge < 2; edge++) {
         double angle = Math.toRadians((double)(startDeg + (edge == 0 ? 0.0F : sweepDeg)));
         float sin = (float)Math.sin(angle);
         float cos = (float)Math.cos(angle);

         for (int ring = 0; ring < 2; ring++) {
            float radius = ring == 0 ? innerRadius : outerRadius;
            float x = cx + sin * radius;
            float y = cy - cos * radius;
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
         }
      }

      for (int quarter = 0; quarter < 4; quarter++) {
         float deg = (float)quarter * 90.0F;
         float delta = deg - startDeg;
         if (delta < 0.0F) {
            delta += 360.0F;
         }

         if (!(delta > sweepDeg)) {
            double angle = Math.toRadians((double)deg);
            float x = cx + (float)Math.sin(angle) * outerRadius;
            float y = cy - (float)Math.cos(angle) * outerRadius;
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
         }
      }

      return new float[]{minX, minY, maxX, maxY};
   }

   private static void quadRuns(WheelRenderer.Sink sink, float[] xs, float[] ys, int argb, boolean smooth) {
      float minX = Math.min(Math.min(xs[0], xs[1]), Math.min(xs[2], xs[3]));
      float maxX = Math.max(Math.max(xs[0], xs[1]), Math.max(xs[2], xs[3]));
      float minY = Math.min(Math.min(ys[0], ys[1]), Math.min(ys[2], ys[3]));
      float maxY = Math.max(Math.max(ys[0], ys[1]), Math.max(ys[2], ys[3]));
      WheelRenderer.Mask mask = (px, py) -> {
         boolean neg = false;
         boolean pos = false;

         for (int i = 0; i < 4; i++) {
            int j = i + 1 & 3;
            float cross = (xs[j] - xs[i]) * (py - ys[i]) - (ys[j] - ys[i]) * (px - xs[i]);
            if (cross < -1.0E-4F) {
               neg = true;
            }

            if (cross > 1.0E-4F) {
               pos = true;
            }

            if (neg && pos) {
               return false;
            }
         }

         return true;
      };
      WheelRenderer.Shade shade = (px, py) -> argb;
      rows(
         sink,
         (int)Math.floor((double)minX),
         (int)Math.floor((double)minY),
         (int)Math.ceil((double)maxX) + 1,
         (int)Math.ceil((double)maxY) + 1,
         smooth ? soft(mask, shade) : hard(mask, shade)
      );
   }

   private static void quadFill(GuiGraphics graphics, float[] xs, float[] ys, int argb, boolean smooth) {
      quadRuns(into(graphics), xs, ys, argb, smooth);
   }

   private static void triangleFill(GuiGraphics graphics, float x1, float y1, float x2, float y2, float x3, float y3, int argb, boolean smooth) {
      quadFill(graphics, new float[]{x1, x2, x3, x3}, new float[]{y1, y2, y3, y3}, argb, smooth);
   }

   private static void segmentRuns(WheelRenderer.Sink sink, float x1, float y1, float x2, float y2, float halfWidth, int argb) {
      float dx = x2 - x1;
      float dy = y2 - y1;
      float length = (float)Math.sqrt((double)(dx * dx + dy * dy));
      if (!(length < 1.0E-4F)) {
         float nx = -dy / length * halfWidth;
         float ny = dx / length * halfWidth;
         quadRuns(sink, new float[]{x1 + nx, x2 + nx, x2 - nx, x1 - nx}, new float[]{y1 + ny, y2 + ny, y2 - ny, y1 - ny}, argb, false);
      }
   }

   private static WheelRenderer.HexRuns hexRuns(int x1, int y1, int x2, int y2, float size, int argb) {
      for (WheelRenderer.HexRuns cached : HEX_CACHE) {
         if (cached.holds(x1, y1, x2, y2, size, argb)) {
            return cached;
         }
      }

      WheelRenderer.HexRuns target = HEX_CACHE[hexSlot];
      hexSlot = (hexSlot + 1) % HEX_CACHE.length;
      target.x1 = x1;
      target.y1 = y1;
      target.x2 = x2;
      target.y2 = y2;
      target.size = size;
      target.argb = argb;
      target.runs = merge(collectHex(x1, y1, x2, y2, size, argb));
      return target;
   }

   private static long[] collectHex(int x1, int y1, int x2, int y2, float size, int argb) {
      long[][] packed = new long[][]{new long[4096]};
      int[] count = new int[]{0};
      WheelRenderer.Sink sink = (runX1, y, runX2, colour) -> {
         int from = Math.max(runX1, x1);
         int to = Math.min(runX2, x2);
         if (from < to && y >= y1 && y < y2) {
            if (count[0] == packed[0].length) {
               packed[0] = Arrays.copyOf(packed[0], count[0] * 2);
            }

            packed[0][count[0]++] = (long)(y + 32768) << 40 | (long)(from + 32768) << 20 | (long)(to + 32768);
         }
      };
      float stepX = size * 1.5F;
      float stepY = (float)(Math.sqrt(3.0) * (double)size);
      int cols = (int)Math.ceil((double)((float)(x2 - x1) / stepX)) + 2;
      int rows = (int)Math.ceil((double)((float)(y2 - y1) / stepY)) + 2;

      for (int col = -1; col < cols; col++) {
         for (int row = -1; row < rows; row++) {
            float cx = (float)x1 + (float)col * stepX;
            float cy = (float)y1 + (float)row * stepY + (col % 2 == 0 ? 0.0F : stepY * 0.5F);

            for (int edge = 0; edge < 6; edge++) {
               double a1 = Math.toRadians(60.0 * (double)edge);
               double a2 = Math.toRadians(60.0 * (double)(edge + 1));
               segmentRuns(
                  sink,
                  cx + (float)Math.cos(a1) * size,
                  cy + (float)Math.sin(a1) * size,
                  cx + (float)Math.cos(a2) * size,
                  cy + (float)Math.sin(a2) * size,
                  0.5F,
                  argb
               );
            }
         }
      }

      long[] all = Arrays.copyOf(packed[0], count[0]);
      Arrays.sort(all);
      return all;
   }

   private static int[] merge(long[] sorted) {
      int[] out = new int[sorted.length * 3];
      int count = 0;

      for (long entry : sorted) {
         int y = (int)(entry >>> 40) - 32768;
         int from = (int)(entry >>> 20 & 1048575L) - 32768;
         int to = (int)(entry & 1048575L) - 32768;
         if (count > 0 && out[count - 2] == y && from <= out[count - 1]) {
            out[count - 1] = Math.max(out[count - 1], to);
         } else {
            out[count++] = from;
            out[count++] = y;
            out[count++] = to;
         }
      }

      return Arrays.copyOf(out, count);
   }

   private static BufferBuilder begin(GuiGraphics graphics, Mode mode) {
      graphics.flush();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      return Tesselator.getInstance().begin(mode, DefaultVertexFormat.POSITION_COLOR);
   }

   private static void end(BufferBuilder buffer) {
      BufferUploader.drawWithShader(buffer.buildOrThrow());
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }

   private static void vertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, int argb) {
      buffer.addVertex(matrix, x, y, 0.0F).setColor(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, argb >>> 24);
   }

   private static void segment(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float x2, float y2, float halfWidth, int argb) {
      float dx = x2 - x1;
      float dy = y2 - y1;
      float length = (float)Math.sqrt((double)(dx * dx + dy * dy));
      if (!(length < 1.0E-4F)) {
         float nx = -dy / length * halfWidth;
         float ny = dx / length * halfWidth;
         vertex(buffer, matrix, x1 + nx, y1 + ny, argb);
         vertex(buffer, matrix, x2 + nx, y2 + ny, argb);
         vertex(buffer, matrix, x2 - nx, y2 - ny, argb);
         vertex(buffer, matrix, x1 - nx, y1 - ny, argb);
      }
   }

   private WheelRenderer() {
   }

   private static final class HexRuns {
      private int x1;
      private int y1;
      private int x2;
      private int y2;
      private int argb;
      private float size;
      private int[] runs = new int[0];

      boolean holds(int x1, int y1, int x2, int y2, float size, int argb) {
         return this.runs.length > 0 && this.x1 == x1 && this.y1 == y1 && this.x2 == x2 && this.y2 == y2 && this.size == size && this.argb == argb;
      }

      void draw(GuiGraphics graphics) {
         for (int i = 0; i < this.runs.length; i += 3) {
            graphics.fill(this.runs[i], this.runs[i + 1], this.runs[i + 2], this.runs[i + 1] + 1, this.argb);
         }
      }
   }

   private interface Mask {
      boolean test(float var1, float var2);
   }

   private interface Sampler {
      int at(int var1, int var2);
   }

   private interface Shade {
      int at(float var1, float var2);
   }

   private interface Sink {
      void run(int var1, int var2, int var3, int var4);
   }
}
