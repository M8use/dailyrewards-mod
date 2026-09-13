package net.execheinz.dailyrewards.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * Small square button rendered on the inventory screen. Opens the Daily Rewards calendar. The
 * rotating gold "solar flare" behind the calendar icon only renders while today's reward is still
 * unclaimed, so it stops nagging the player once they've already claimed for the day.
 *
 * <p>Holds a reference to the {@link InventoryScreen} itself and recomputes its own position every
 * frame from that screen's <em>current</em> layout, rather than a fixed (x, y) baked in once at
 * creation time. That's necessary because toggling the recipe book shifts the whole inventory panel
 * right without re-creating the screen (so {@code ScreenEvent.Init.Post} never fires again) — a
 * button positioned only once at init would simply be left behind.
 */
public final class DailyRewardButton extends Button {
   public static final int SIZE = 26;
   private static final int BEVEL_OUTLINE = 0xFF10150F;
   private static final int BEVEL_HIGHLIGHT = 0xFF7CEB9C;
   private static final int BEVEL_SHADOW = 0xFF10401F;
   private static final int FILL_TOP = 0xFF49D272;
   private static final int FILL_BOTTOM = 0xFF1D7A3F;
   private static final int FLARE_COLOR = 0xAAFFD54A;
   private static final int CAL_OUTLINE = 0xFF1B1B1B;
   private static final int CAL_BODY_TOP = 0xFFFFFFFF;
   private static final int CAL_BODY_BOTTOM = 0xFFE7E2D2;
   private static final int CAL_HEADER = 0xFFE84A3F;
   private static final int CAL_RING = 0xFF2B2B2E;
   private static final int GRASS_TOP = 0xFF5FBF3F;
   private static final int GRASS_BOTTOM = 0xFF7A5230;
   private static final int HOVER_WASH = 0x33FFFFFF;
   private static final int FLARE_RAYS = 8;
   // Reserves the same amount of vertical space the gambling mod's own button occupies (26px + 6px
   // gap) so the two sit stacked instead of overlapping, whether or not that mod is even installed.
   private static final int VERTICAL_RESERVE = SIZE + 6;

   private final InventoryScreen inventoryScreen;

   private DailyRewardButton(InventoryScreen inventoryScreen) {
      super(0, 0, SIZE, SIZE, Component.translatable("gui.dailyrewards.button"), DailyRewardButton::press, DEFAULT_NARRATION);
      this.inventoryScreen = inventoryScreen;
      this.setTooltip(Tooltip.create(Component.translatable("gui.dailyrewards.button")));
   }

   public static DailyRewardButton create(InventoryScreen inventoryScreen) {
      return new DailyRewardButton(inventoryScreen);
   }

   private static void press(Button button) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player != null) {
         minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 0.4F));
         minecraft.setScreen(new DailyRewardScreen());
      }
   }

   /** Re-reads the inventory screen's current layout every frame, so toggling the recipe book (which
    * shifts the panel right without re-initializing the screen) carries this button along with it. */
   private void followInventoryLayout() {
      int x = Math.min(this.inventoryScreen.getGuiLeft() + this.inventoryScreen.getXSize() + 6, this.inventoryScreen.width - SIZE - 4);
      int y = this.inventoryScreen.getGuiTop() + 6 + VERTICAL_RESERVE;
      this.setX(x);
      this.setY(y);
   }

   @Override
   protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.followInventoryLayout();
      int x1 = this.getX();
      int y1 = this.getY();
      int x2 = x1 + this.width;
      int y2 = y1 + this.height;
      float cx = (float)(x1 + x2) / 2.0F;
      float cy = (float)(y1 + y2) / 2.0F;
      WheelRenderer.pixelBevelBorder(graphics, x1, y1, x2, y2, BEVEL_OUTLINE, BEVEL_HIGHLIGHT, BEVEL_SHADOW);
      WheelRenderer.roundedRect(graphics, x1 + 2, y1 + 2, x2 - 2, y2 - 2, 0, FILL_TOP, FILL_BOTTOM);
      if (this.isHoveredOrFocused()) {
         graphics.fill(x1 + 2, y1 + 2, x2 - 2, y2 - 2, HOVER_WASH);
      }

      if (DailyRewardClientState.claimableToday()) {
         float rotation = (float)(System.currentTimeMillis() / 15L % 360L);
         float sweep = 360.0F / (float)FLARE_RAYS / 2.0F;

         for (int i = 0; i < FLARE_RAYS; i++) {
            float start = rotation + (float)i * (360.0F / (float)FLARE_RAYS);
            WheelRenderer.radialFadeArc(graphics, cx, cy, 3.0F, 14.0F, start, sweep, FLARE_COLOR);
         }
      }

      WheelRenderer.calendarIcon(graphics, cx, cy, 1.0F, CAL_OUTLINE, CAL_BODY_TOP, CAL_BODY_BOTTOM, CAL_HEADER, CAL_RING);
      int top = Math.round(cy - 7.0F);
      graphics.fill(Math.round(cx) - 3, top + 11, Math.round(cx) + 3, top + 13, GRASS_TOP);
      graphics.fill(Math.round(cx) - 3, top + 13, Math.round(cx) + 3, top + 15, GRASS_BOTTOM);
   }
}
