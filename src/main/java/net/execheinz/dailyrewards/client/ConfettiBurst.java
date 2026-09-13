package net.execheinz.dailyrewards.client;

import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/**
 * A small, screen-owned particle system for two effects used by reward-claim animations:
 * a colored confetti burst, and item icons "raining" down from the top of a rectangle. Both are
 * pure GUI draw calls driven by wall-clock time (no ticking, no entities), so they're cheap and
 * stay smooth regardless of server tick rate.
 */
public final class ConfettiBurst {
   private final List<ConfettiBurst.Particle> particles = new ArrayList<>();

   public void spawnConfetti(float x, float y, int count) {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      long now = System.currentTimeMillis();

      for (int i = 0; i < count; i++) {
         double angle = Math.toRadians(-90.0 + (random.nextDouble() - 0.5) * 150.0);
         float speed = 55.0F + random.nextFloat() * 110.0F;
         float vx = (float)(Math.cos(angle) * (double)speed);
         float vy = (float)(Math.sin(angle) * (double)speed);
         int color = WheelRenderer.hsb(random.nextFloat() * 360.0F, 0.8F, 1.0F);
         float phase = random.nextFloat() * 6.2831855F;
         this.particles.add(new ConfettiBurst.Particle(x, y, vx, vy, 260.0F, now, 2200L, color, phase, 0.0F, 1.0F, ItemStack.EMPTY));
      }
   }

   /**
    * Spawns {@code count} icons of {@code item} that rain down from just above {@code y1}, across the
    * full {@code x1}-{@code x2} width, timed so most land around {@code y2} by the end of {@code durationMs}.
    * Staggered so they keep arriving across roughly the first half of the duration.
    */
   public void spawnItemRain(int x1, int y1, int x2, int y2, ItemStack item, int count, long durationMs) {
      if (!item.isEmpty()) {
         ThreadLocalRandom random = ThreadLocalRandom.current();
         long now = System.currentTimeMillis();
         int width = Math.max(1, x2 - x1);
         float distance = Math.max(40.0F, (float)(y2 - y1) + 60.0F);
         float baseVy = distance / ((float)durationMs / 1000.0F);

         for (int i = 0; i < count; i++) {
            float startX = (float)x1 + random.nextFloat() * (float)width;
            float startY = (float)y1 - 20.0F - random.nextFloat() * 40.0F;
            float vy = baseVy * (0.8F + random.nextFloat() * 0.5F);
            float vx = (random.nextFloat() - 0.5F) * 24.0F;
            float rotationSpeed = (random.nextFloat() - 0.5F) * 240.0F;
            float scale = 0.7F + random.nextFloat() * 0.7F;
            long delay = (long)(random.nextFloat() * (float)durationMs * 0.4F);
            this.particles
               .add(new ConfettiBurst.Particle(startX, startY, vx, vy, 0.0F, now + delay, durationMs, 0, 0.0F, rotationSpeed, scale, item.copy()));
         }
      }
   }

   public boolean isEmpty() {
      return this.particles.isEmpty();
   }

   public void clear() {
      this.particles.clear();
   }

   public void render(GuiGraphics graphics) {
      if (!this.particles.isEmpty()) {
         long now = System.currentTimeMillis();
         graphics.pose().pushPose();
         graphics.pose().translate(0.0F, 0.0F, 550.0F);

         for (int i = this.particles.size() - 1; i >= 0; i--) {
            ConfettiBurst.Particle particle = this.particles.get(i);
            long age = now - particle.spawnMs();
            if (age >= particle.lifetimeMs()) {
               this.particles.remove(i);
            } else if (age >= 0L) {
               float t = (float)age / 1000.0F;
               float x = particle.x0() + particle.vx() * t;
               float y = particle.y0() + particle.vy() * t + 0.5F * particle.gravity() * t * t;
               if (particle.item().isEmpty()) {
                  renderConfettiPiece(graphics, particle, t, x, y);
               } else {
                  renderRainItem(graphics, particle, t, x, y);
               }
            }
         }

         graphics.pose().popPose();
      }
   }

   private static void renderConfettiPiece(GuiGraphics graphics, ConfettiBurst.Particle particle, float t, float x, float y) {
      float wobble = (float)Math.sin((double)(t * 9.0F + particle.phase())) * 2.5F;
      float fadeStart = (float)(particle.lifetimeMs() - 500L) / 1000.0F;
      float alpha = t <= fadeStart ? 1.0F : Math.max(0.0F, 1.0F - (t - fadeStart) / 0.5F);
      int argb = (int)(alpha * 255.0F) << 24 | particle.color() & 16777215;
      int px = Math.round(x + wobble);
      int py = Math.round(y);
      graphics.fill(px - 2, py - 1, px + 2, py + 1, argb);
   }

   private static void renderRainItem(GuiGraphics graphics, ConfettiBurst.Particle particle, float t, float x, float y) {
      float rotation = particle.rotationSpeed() * t;
      graphics.pose().pushPose();
      graphics.pose().translate(x, y, 0.0F);
      graphics.pose().mulPose(Axis.ZP.rotationDegrees(rotation));
      graphics.pose().scale(particle.scale(), particle.scale(), 1.0F);
      graphics.renderItem(particle.item(), -8, -8);
      graphics.pose().popPose();
   }

   private record Particle(
      float x0,
      float y0,
      float vx,
      float vy,
      float gravity,
      long spawnMs,
      long lifetimeMs,
      int color,
      float phase,
      float rotationSpeed,
      float scale,
      ItemStack item
   ) {
   }
}
