package net.execheinz.dailyrewards.client;

import java.util.ArrayList;
import java.util.List;
import net.execheinz.dailyrewards.Ids;
import net.execheinz.dailyrewards.network.ClientboundDailyRewardResultPacket;
import net.execheinz.dailyrewards.network.DailyRewardNetwork;
import net.execheinz.dailyrewards.network.ServerboundClaimDailyRewardPacket;
import net.execheinz.dailyrewards.network.ServerboundRequestDailySyncPacket;
import net.execheinz.dailyrewards.reward.ClaimRecord;
import net.execheinz.dailyrewards.reward.DailyRewardConfig;
import net.execheinz.dailyrewards.reward.RewardEntry;
import net.execheinz.dailyrewards.reward.RewardPools;
import net.execheinz.dailyrewards.reward.RewardRarity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The Daily Rewards calendar. A pure {@link Screen} driven entirely by
 * {@link DailyRewardClientState} plus two packets: a claim request, and the reveal that comes
 * back. Weeks are a visual grouping only — the day counter never resets — so the 7 cards shown are
 * always "this week", computed fresh from the current day count every time it renders.
 */
public final class DailyRewardScreen extends Screen {
   private static final int WIDTH = 310;
   private static final int HEIGHT = 178;
   private static final int PAD = 8;
   private static final int HEADER_H = 30;
   private static final int CARD_W = 58;
   private static final int CARD_H = 56;
   private static final int GAP = 5;
   private static final int GRID_X_OFFSET = 8;
   private static final int GRID_Y_OFFSET = PAD + HEADER_H + 8;
   private static final int BIG_X_OFFSET = GRID_X_OFFSET + 3 * CARD_W + 2 * GAP + GAP * 2;
   private static final int BIG_W = WIDTH - PAD - BIG_X_OFFSET;
   private static final int BIG_H = 2 * CARD_H + GAP;
   private static final int ROOT_BORDER = 0xFF16324A;
   private static final int HEADER_TOP = 0xFF6BD8FF;
   private static final int HEADER_BOTTOM = 0xFF2E8FE0;
   private static final int BODY_TOP = 0xFF3C8FC4;
   private static final int BODY_BOTTOM = 0xFF235A85;
   private static final int CARD_OUTLINE = 0xFF20242C;
   private static final int CARD_WHITE_TOP = 0xFFFFFFFF;
   private static final int CARD_WHITE_BOTTOM = 0xFFE3EBF2;
   private static final int LOCKED_YELLOW_TOP = 0xFFFFDD5C;
   private static final int LOCKED_YELLOW_BOTTOM = 0xFFE8A61C;
   private static final int PILL_LOCKED_OUTLINE = 0xFF7A140C;
   private static final int PILL_LOCKED_TOP = 0xFFF15A4E;
   private static final int PILL_LOCKED_BOTTOM = 0xFFD8332A;
   private static final int PILL_CLAIM_OUTLINE = 0xFF8A5900;
   private static final int PILL_CLAIM_TOP = 0xFFFFD24C;
   private static final int PILL_CLAIM_BOTTOM = 0xFFF2A31C;
   private static final int PILL_CLAIMED_OUTLINE = 0xFF1D6B3B;
   private static final int PILL_CLAIMED_TOP = 0xFF6EDB8F;
   private static final int PILL_CLAIMED_BOTTOM = 0xFF34A15C;
   private static final int TEXT_DARK = 0xFF1B2430;
   private static final int TEXT_WHITE = -1;
   private static final long REVEAL_NORMAL_MS = 2600L;
   private static final long REVEAL_WEEKLY_MS = 3400L;
   private static final long REVEAL_MILESTONE_MS = 4200L;
   private static final long FLAME_FLARE_MS = 900L;
   // calendar icon to the left of the title/streak block, and how far that block shifts to clear it
   private static final int CAL_OUTLINE = 0xFF1B1B1B;
   private static final int CAL_BODY_TOP = 0xFFFFFFFF;
   private static final int CAL_BODY_BOTTOM = 0xFFE7E2D2;
   private static final int CAL_HEADER = 0xFFE84A3F;
   private static final int CAL_RING = 0xFF2B2B2E;
   private static final int TITLE_SHIFT = 24;
   private static final int TIMER_VIBRANT = 0xFFFF7A1A;
   private static final int TIMER_OUTLINE = 0xFF5C0E08;

   private int leftPos;
   private int topPos;
   private final ConfettiBurst confetti = new ConfettiBurst();
   private boolean waitingForClaim;
   private float pendingClaimCx;
   private float pendingClaimCy;
   private long revealUntil;
   private long revealStartMs;
   private RewardRarity revealRarity = RewardRarity.COMMON;
   private ItemStack revealStack = ItemStack.EMPTY;
   private boolean revealWeekly;
   private boolean revealMilestone;
   private boolean revealStreakMilestone;
   private int revealStreakValue;
   private ItemStack revealBonusStack = ItemStack.EMPTY;
   private long flameFlareStart = -10000L;

   public DailyRewardScreen() {
      super(Component.translatable("gui.dailyrewards.title"));
   }

   @Override
   protected void init() {
      this.leftPos = (this.width - WIDTH) / 2;
      this.topPos = (this.height - HEIGHT) / 2;
      this.addRenderableWidget(
         StyledButton.red(this.leftPos + WIDTH - PAD - 20, this.topPos + 6, 20, 18, Component.literal("X"), b -> this.onClose())
      );
      DailyRewardNetwork.toServer(new ServerboundRequestDailySyncPacket());
   }

   /** Called by the packet handler when a plain status sync arrives while this screen is open. */
   public void onSync() {
      this.waitingForClaim = false;
   }

   /** Called by the packet handler when the server reveals what a claim actually rolled. */
   public void onClaimResult(ClientboundDailyRewardResultPacket packet) {
      this.waitingForClaim = false;
      this.revealRarity = parseRarity(packet.rarityId());
      Item item = Ids.item(packet.itemId());
      this.revealStack = item == null ? ItemStack.EMPTY : new ItemStack(item, Math.max(1, packet.amount()));
      this.revealWeekly = packet.weekly();
      this.revealMilestone = packet.milestone();
      this.revealStreakMilestone = packet.streakMilestoneHit();
      this.revealStreakValue = packet.streakMilestoneValue();
      Item bonusItem = packet.bonusItemId().isEmpty() ? null : Ids.item(packet.bonusItemId());
      this.revealBonusStack = bonusItem == null ? ItemStack.EMPTY : new ItemStack(bonusItem, Math.max(1, packet.bonusAmount()));
      long now = System.currentTimeMillis();
      this.revealStartMs = now;
      this.revealUntil = now + (this.revealMilestone ? REVEAL_MILESTONE_MS : this.revealWeekly ? REVEAL_WEEKLY_MS : REVEAL_NORMAL_MS);
      int confettiCount = this.revealMilestone ? 130 : this.revealWeekly ? 80 : 40;
      this.confetti.spawnConfetti(this.pendingClaimCx, this.pendingClaimCy, confettiCount);
      int rainCount = this.revealMilestone ? 52 : this.revealWeekly ? 36 : 24;
      long rainDuration = this.revealMilestone ? 2600L : this.revealWeekly ? 2000L : 1600L;
      this.confetti.spawnItemRain(0, 0, this.width, this.height, this.revealStack, rainCount, rainDuration);
      SoundEvent sound = this.revealMilestone || this.revealWeekly ? SoundEvents.UI_TOAST_CHALLENGE_COMPLETE : SoundEvents.EXPERIENCE_ORB_PICKUP;
      this.playSound(sound, this.revealMilestone ? 0.8F : 1.0F, 0.6F);
      if (this.revealMilestone) {
         this.playSound(SoundEvents.PLAYER_LEVELUP, 1.2F, 0.5F);
      }

      if (packet.newStreak() >= 2) {
         this.flameFlareStart = now;
         this.playSound(SoundEvents.FIRECHARGE_USE, 1.3F, 0.5F);
      }
   }

   private static RewardRarity parseRarity(String name) {
      try {
         return RewardRarity.valueOf(name);
      } catch (IllegalArgumentException exception) {
         return RewardRarity.COMMON;
      }
   }

   private void playSound(SoundEvent sound, float pitch, float volume) {
      if (this.minecraft != null) {
         this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
      }
   }

   private void requestClaim(DailyRewardScreen.SlotInfo slot) {
      if (!this.waitingForClaim) {
         this.waitingForClaim = true;
         this.pendingClaimCx = (float)(slot.x1() + slot.x2()) / 2.0F;
         this.pendingClaimCy = (float)(slot.y1() + slot.y2()) / 2.0F;
         DailyRewardNetwork.toServer(new ServerboundClaimDailyRewardPacket());
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (super.mouseClicked(mouseX, mouseY, button)) {
         return true;
      } else if (this.revealUntil > System.currentTimeMillis()) {
         this.revealUntil = 0L;
         return true;
      } else {
         for (DailyRewardScreen.SlotInfo slot : this.computeSlots()) {
            if (slot.state() == DailyRewardScreen.SlotState.AVAILABLE
               && inBox((int)mouseX, (int)mouseY, slot.x1(), slot.y1(), slot.x2() - slot.x1(), slot.y2() - slot.y1())) {
               this.requestClaim(slot);
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(graphics, mouseX, mouseY, partialTick);
      WheelRenderer.card(graphics, this.leftPos, this.topPos, this.leftPos + WIDTH, this.topPos + HEIGHT, 6, ROOT_BORDER, BODY_TOP, BODY_BOTTOM);
      WheelRenderer.roundedRect(graphics, this.leftPos + 2, this.topPos + 2, this.leftPos + WIDTH - 2, this.topPos + HEADER_H, 4, HEADER_TOP, HEADER_BOTTOM);
      this.renderHeader(graphics);
      this.renderGrid(graphics, mouseX, mouseY);

      for (Renderable renderable : this.renderables) {
         renderable.render(graphics, mouseX, mouseY, partialTick);
      }

      long now = System.currentTimeMillis();
      if (this.revealUntil > now) {
         this.renderReveal(graphics);
         this.confetti.render(graphics);
      } else {
         this.confetti.render(graphics);
      }
   }

   private void renderHeader(GuiGraphics graphics) {
      WheelRenderer.calendarIcon(
         graphics, (float)(this.leftPos + 20), (float)(this.topPos + 16), 1.3F, CAL_OUTLINE, CAL_BODY_TOP, CAL_BODY_BOTTOM, CAL_HEADER, CAL_RING
      );
      graphics.pose().pushPose();
      graphics.pose().translate((float)(this.leftPos + 14 + TITLE_SHIFT), (float)(this.topPos + 6), 0.0F);
      graphics.pose().scale(1.25F, 1.25F, 1.0F);
      graphics.drawString(this.font, this.getTitle(), 0, 0, TEXT_DARK, false);
      graphics.pose().popPose();
      long now = System.currentTimeMillis();
      float flareT = (float)(now - this.flameFlareStart) / (float)FLAME_FLARE_MS;
      boolean flaring = flareT >= 0.0F && flareT <= 1.0F;
      float flarePulse = flaring ? (float)Math.sin((double)(Math.PI * Math.min(1.0F, flareT))) : 0.0F;
      this.drawFlame(graphics, this.leftPos + 18 + TITLE_SHIFT, this.topPos + 23, 0.9F + flarePulse * 0.7F, flarePulse);
      Component streakLabel = Component.translatable("gui.dailyrewards.streak", DailyRewardClientState.streak());
      graphics.drawString(this.font, streakLabel, this.leftPos + 27 + TITLE_SHIFT, this.topPos + 20, TEXT_DARK, false);
      this.renderTimer(graphics, now);
   }

   /** The countdown to the next claim, scaled up to fill the space the "claim all" button used to take. */
   private void renderTimer(GuiGraphics graphics, long now) {
      if (!DailyRewardClientState.claimableToday()) {
         long remainingMs = DailyRewardClientState.nextResetEpochMillis() - now;
         if (remainingMs > 0L) {
            long totalMinutes = remainingMs / 60000L;
            Component timer = Component.translatable("gui.dailyrewards.timer", totalMinutes / 60L, totalMinutes % 60L);
            int areaLeft = this.leftPos + 134;
            int areaRight = this.leftPos + WIDTH - PAD - 30;
            int centerX = (areaLeft + areaRight) / 2;
            int centerY = this.topPos + 16;
            float fitScale = (float)(areaRight - areaLeft) / (float)Math.max(1, this.font.width(timer));
            float scale = Math.max(0.9F, Math.min(1.7F, fitScale)) * (0.94F + 0.06F * (float)Math.sin((double)now / 260.0));
            boolean urgent = remainingMs < 3600000L;
            int mainColor = urgent ? WheelRenderer.hsb((float)(now / 4L % 360L), 0.85F, 1.0F) : TIMER_VIBRANT;
            graphics.pose().pushPose();
            graphics.pose().translate((float)centerX, (float)centerY, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);

            for (int ox = -1; ox <= 1; ox++) {
               for (int oy = -1; oy <= 1; oy++) {
                  if (ox != 0 || oy != 0) {
                     graphics.drawCenteredString(this.font, timer, ox, -4 + oy, TIMER_OUTLINE);
                  }
               }
            }

            graphics.drawCenteredString(this.font, timer, 0, -4, mainColor);
            graphics.pose().popPose();
         }
      }
   }

   private void renderGrid(GuiGraphics graphics, int mouseX, int mouseY) {
      long now = System.currentTimeMillis();

      for (DailyRewardScreen.SlotInfo slot : this.computeSlots()) {
         boolean weekly = slot.day() % DailyRewardConfig.WEEKLY_INTERVAL == 0;
         boolean milestone = slot.day() % DailyRewardConfig.MILESTONE_INTERVAL == 0;
         boolean hovered = inBox(mouseX, mouseY, slot.x1(), slot.y1(), slot.x2() - slot.x1(), slot.y2() - slot.y1());
         this.renderCard(graphics, slot, weekly, milestone, hovered, now);
      }
   }

   private void renderCard(GuiGraphics graphics, DailyRewardScreen.SlotInfo slot, boolean weekly, boolean milestone, boolean hovered, long now) {
      int x1 = slot.x1();
      int y1 = slot.y1();
      int x2 = slot.x2();
      int y2 = slot.y2();
      boolean special = weekly || milestone;
      DailyRewardScreen.SlotState state = slot.state();
      if (special) {
         WheelRenderer.roundedRect(graphics, x1, y1, x2, y2, 6, CARD_OUTLINE);
         float phase = (float)(now / (milestone ? 10L : 9L) % 360L);
         if (slot.big()) {
            WheelRenderer.spiralRainbowRect(graphics, x1 + 1, y1 + 1, x2 - 1, y2 - 1, 5, phase);
         } else {
            WheelRenderer.rainbowRect(graphics, x1 + 1, y1 + 1, x2 - 1, y2 - 1, 5, phase);
         }
      } else {
         switch (state) {
            case CLAIMED:
               ClaimRecord record = DailyRewardClientState.claimFor(slot.day());
               RewardRarity rarity = record != null ? parseRarity(record.rarityId()) : RewardRarity.COMMON;
               int rarityLight = WheelRenderer.lerpColor(rarity.color(), -1, 0.35F);
               this.renderSunburstCard(graphics, x1, y1, x2, y2, 5, CARD_OUTLINE, rarityLight, rarity.color(), now, true);
               break;
            case AVAILABLE:
               this.renderSunburstCard(graphics, x1, y1, x2, y2, 5, CARD_OUTLINE, PILL_CLAIM_TOP, PILL_CLAIM_BOTTOM, now, true);
               break;
            default:
               this.renderSunburstCard(graphics, x1, y1, x2, y2, 5, CARD_OUTLINE, LOCKED_YELLOW_TOP, LOCKED_YELLOW_BOTTOM, now, true);
         }
      }

      if (state == DailyRewardScreen.SlotState.AVAILABLE) {
         float pulse = 0.5F + 0.5F * (float)Math.sin((double)now / 220.0);
         int glowAlpha = (int)(60.0F + 60.0F * pulse);
         WheelRenderer.roundedRect(graphics, x1 + 2, y1 + 2, x2 - 2, y2 - 2, 4, glowAlpha << 24 | 16764746);
      }

      if (hovered && state == DailyRewardScreen.SlotState.AVAILABLE) {
         WheelRenderer.roundedRect(graphics, x1 + 1, y1 + 1, x2 - 1, y2 - 1, 5, 0x22FFFFFF);
      }

      int cx = (x1 + x2) / 2;
      int iconCy = slot.big() ? y1 + 58 : y1 + 27;
      float iconScale = slot.big() ? 1.7F : 1.0F;
      // items/orbit draw first, but that alone doesn't guarantee stacking: renderItem always renders at
      // an internal Z~150 regardless of draw order, so the label text below is explicitly pushed above it.
      this.renderCardContent(graphics, slot, special, iconCy, iconScale, now, cx);
      graphics.pose().pushPose();
      graphics.pose().translate(0.0F, 0.0F, 400.0F);
      if (milestone) {
         graphics.drawCenteredString(this.font, Component.translatable("gui.dailyrewards.milestone_label"), cx, y1 + 5, TEXT_WHITE);
      } else if (weekly) {
         graphics.drawCenteredString(this.font, Component.translatable("gui.dailyrewards.weekly_label"), cx, y1 + 5, TEXT_WHITE);
      }

      int dayLabelY = special ? y1 + (slot.big() ? 18 : 15) : y1 + 4;
      graphics.drawCenteredString(this.font, Component.translatable("gui.dailyrewards.day", slot.day()), cx, dayLabelY, TEXT_WHITE);
      graphics.pose().popPose();
      Component pillText;
      int pillOutline;
      int pillTop;
      int pillBottom;
      int pillTextColor;
      switch (state) {
         case CLAIMED:
            pillText = Component.translatable("gui.dailyrewards.claimed");
            pillOutline = PILL_CLAIMED_OUTLINE;
            pillTop = PILL_CLAIMED_TOP;
            pillBottom = PILL_CLAIMED_BOTTOM;
            pillTextColor = TEXT_WHITE;
            break;
         case AVAILABLE:
            pillText = Component.translatable("gui.dailyrewards.claim_hint");
            pillOutline = PILL_CLAIM_OUTLINE;
            pillTop = PILL_CLAIM_TOP;
            pillBottom = PILL_CLAIM_BOTTOM;
            pillTextColor = TEXT_DARK;
            break;
         default:
            pillText = Component.translatable("gui.dailyrewards.locked");
            pillOutline = PILL_LOCKED_OUTLINE;
            pillTop = PILL_LOCKED_TOP;
            pillBottom = PILL_LOCKED_BOTTOM;
            pillTextColor = TEXT_WHITE;
      }

      this.drawPill(graphics, cx, y2 - (slot.big() ? 20 : 16), pillText, pillOutline, pillTop, pillBottom, pillTextColor);
   }

   /**
    * Draws the card's outline+gradient, then a two-tone radiating sunburst pattern behind whatever
    * icon sits on top. When {@code animate} is true (any not-yet-claimed card) the whole sunburst
    * slowly spins, echoing the same rotating flare the inventory button shows while unclaimed.
    */
   private void renderSunburstCard(GuiGraphics graphics, int x1, int y1, int x2, int y2, int radius, int outline, int top, int bottom, long now, boolean animate) {
      WheelRenderer.card(graphics, x1, y1, x2, y2, radius, outline, top, bottom);
      int cx = (x1 + x2) / 2;
      int cy = (y1 + y2) / 2;
      float maxRadius = (float)Math.max(x2 - x1, y2 - y1);
      int rays = 20;
      float sweep = 360.0F / (float)rays / 2.0F;
      float rotation = animate ? (float)(now / 30L % 360L) : 0.0F;
      int lightRay = WheelRenderer.lerpColor(top, -1, 0.35F);
      int darkRay = WheelRenderer.lerpColor(bottom, -16777216, 0.15F);
      graphics.enableScissor(x1 + 2, y1 + 2, x2 - 2, y2 - 2);

      for (int i = 0; i < rays; i++) {
         int color = i % 2 == 0 ? lightRay : darkRay;
         WheelRenderer.arc(graphics, (float)cx, (float)cy, 0.0F, maxRadius, rotation + (float)i * (360.0F / (float)rays), sweep, color);
      }

      graphics.disableScissor();
   }

   private void renderCardContent(GuiGraphics graphics, DailyRewardScreen.SlotInfo slot, boolean special, int iconCy, float iconScale, long now, int cx) {
      switch (slot.state()) {
         case CLAIMED:
            ClaimRecord record = DailyRewardClientState.claimFor(slot.day());
            if (record != null) {
               Item item = Ids.item(record.itemId());
               ItemStack stack = item == null ? ItemStack.EMPTY : new ItemStack(item, Math.max(1, record.amount()));
               if (!stack.isEmpty()) {
                  int px = cx - 8;
                  int py = iconCy - 8;
                  if (slot.big()) {
                     graphics.pose().pushPose();
                     graphics.pose().translate((float)cx, (float)iconCy, 0.0F);
                     graphics.pose().scale(1.6F, 1.6F, 1.0F);
                     graphics.renderItem(stack, -8, -8);
                     graphics.pose().popPose();
                     Component amountLine = Component.literal("+" + stack.getCount() + " ").append(stack.getHoverName());
                     graphics.drawCenteredString(this.font, amountLine, cx, iconCy + 22, TEXT_WHITE);
                  } else {
                     graphics.renderItem(stack, px, py);
                     graphics.renderItemDecorations(this.font, stack, px, py);
                  }

                  return;
               }
            }

            this.drawCheckmark(graphics, cx, iconCy, iconScale);
            break;
         case AVAILABLE: {
            if (special) {
               this.renderOrbitPreview(graphics, cx, iconCy, slot.day(), now, false);
            } else {
               ItemStack preview = previewIconFor(slot.day(), now);
               if (preview.isEmpty()) {
                  this.drawMystery(graphics, cx, iconCy, iconScale, now);
               } else {
                  this.renderPreviewItem(graphics, preview, cx, iconCy, iconScale, now);
               }
            }

            break;
         }
         default: {
            if (special) {
               this.renderOrbitPreview(graphics, cx, iconCy, slot.day(), now, true);
            } else {
               ItemStack lockedPreview = previewIconFor(slot.day(), now);
               if (!lockedPreview.isEmpty()) {
                  // drawn well behind (and bigger than) the lock so it isn't the lock hiding most of it
                  this.renderPreviewItem(graphics, lockedPreview, cx, iconCy, iconScale * 1.35F, now);
               }

               this.drawLock(graphics, cx, iconCy, iconScale);
            }
         }
      }
   }

   /** The weekly/milestone card's showcase: several real possible rewards orbiting together in one ring around a lock or "?". */
   private void renderOrbitPreview(GuiGraphics graphics, int cx, int cy, int day, long now, boolean locked) {
      List<RewardEntry> pool = previewPool(day);
      int count = Math.min(10, pool.size());
      float spin = (float)(now / 55L % 360L);
      this.renderOrbitRing(graphics, cx, cy, pool, 0, count, 36.0F, 0.72F, spin);
      if (locked) {
         this.drawLock(graphics, cx, cy, 1.4F);
      } else {
         this.drawMystery(graphics, cx, cy, 1.4F, now);
      }
   }

   private void renderOrbitRing(GuiGraphics graphics, int cx, int cy, List<RewardEntry> pool, int startIndex, int count, float radius, float itemScale, float spin) {
      for (int i = 0; i < count; i++) {
         float angle = spin + (float)i * (360.0F / (float)count);
         double rad = Math.toRadians((double)angle);
         float ix = (float)cx + (float)Math.sin(rad) * radius;
         float iy = (float)cy - (float)Math.cos(rad) * radius;
         ItemStack stack = new ItemStack(pool.get(startIndex + i).previewItem());
         graphics.pose().pushPose();
         graphics.pose().translate(ix, iy, 0.0F);
         graphics.pose().scale(itemScale, itemScale, 1.0F);
         graphics.renderItem(stack, -8, -8);
         graphics.pose().popPose();
      }
   }

   private void drawPill(GuiGraphics graphics, int cx, int y, Component text, int outline, int top, int bottom, int textColor) {
      int width = this.font.width(text) + 12;
      int height = 12;
      int x1 = cx - width / 2;
      int x2 = cx + width / 2;
      graphics.pose().pushPose();
      graphics.pose().translate(0.0F, 0.0F, 400.0F);
      WheelRenderer.card(graphics, x1, y, x2, y + height, height / 2, outline, top, bottom);
      graphics.drawCenteredString(this.font, text, cx, y + 2, textColor);
      graphics.pose().popPose();
   }

   private void renderPreviewItem(GuiGraphics graphics, ItemStack stack, int cx, int cy, float scale, long now) {
      float pulse = 0.95F + 0.05F * (float)Math.sin((double)now / 150.0);
      float rotation = (float)(now / 12L % 360L);
      int rays = 8;
      float sweep = 360.0F / (float)rays / 2.2F;

      for (int i = 0; i < rays; i++) {
         float start = rotation + (float)i * (360.0F / (float)rays);
         WheelRenderer.radialFadeArc(graphics, (float)cx, (float)cy, 2.0F, 13.0F * scale, start, sweep, 0x77FFFFFF);
      }

      graphics.pose().pushPose();
      graphics.pose().translate((float)cx, (float)cy, 0.0F);
      graphics.pose().scale(scale * pulse, scale * pulse, 1.0F);
      graphics.renderItem(stack, -8, -8);
      graphics.pose().popPose();
   }

   /** {@code shine} is 0 normally, rising to 1 for a moment whenever the streak just continued. */
   private void drawFlame(GuiGraphics graphics, int cx, int cy, float scale, float shine) {
      float flicker = 0.95F + 0.05F * (float)Math.sin((double)System.currentTimeMillis() / 150.0);
      if (shine > 0.0F) {
         int glowAlpha = (int)(200.0F * shine);
         WheelRenderer.radialFadeArc(graphics, (float)cx, (float)cy, 0.0F, 8.0F + 6.0F * shine, 0.0F, 360.0F, glowAlpha << 24 | 0xFFE066);
      }

      int outer = WheelRenderer.lerpColor(0xFFE8481F, -1, shine * 0.5F);
      int middle = WheelRenderer.lerpColor(0xFFFF9A2E, -1, shine * 0.5F);
      int inner = WheelRenderer.lerpColor(0xFFFFE066, -1, shine * 0.3F);
      WheelRenderer.flame(graphics, (float)cx, (float)cy, scale * flicker, outer, middle, inner);
   }

   private void drawCheckmark(GuiGraphics graphics, int cx, int cy, float scale) {
      int color = 0xFF2E9E52;
      float jointX = (float)cx - 1.5F * scale;
      float jointY = (float)cy + 5.0F * scale;
      WheelRenderer.thickLine(graphics, (float)cx - 6.0F * scale, (float)cy, jointX, jointY, 1.6F * scale, color);
      WheelRenderer.thickLine(graphics, jointX, jointY, (float)cx + 7.0F * scale, (float)cy - 6.0F * scale, 1.6F * scale, color);
      WheelRenderer.disc(graphics, jointX, jointY, 1.6F * scale, color);
   }

   private void drawLock(GuiGraphics graphics, int cx, int cy, float scale) {
      graphics.pose().pushPose();
      graphics.pose().translate(0.0F, 0.0F, 300.0F);
      float bodyTop = (float)cy - scale;
      float bodyBottom = (float)cy + 7.0F * scale;
      float bodyLeft = (float)cx - 6.0F * scale;
      float bodyRight = (float)cx + 6.0F * scale;
      int bodyColor = 0xFFAFB8C4;
      int shackleColor = 0xFF7B828F;
      WheelRenderer.arc(graphics, (float)cx, bodyTop, 2.6F * scale, 4.6F * scale, 270.0F, 180.0F, shackleColor);
      WheelRenderer.roundedRect(graphics, Math.round(bodyLeft), Math.round(bodyTop), Math.round(bodyRight), Math.round(bodyBottom), 2, bodyColor);
      graphics.fill(
         Math.round((float)cx - 0.8F * scale), Math.round((float)cy + 1.5F * scale), Math.round((float)cx + 0.8F * scale), Math.round((float)cy + 4.0F * scale), 0xFF3A3E47
      );
      graphics.pose().popPose();
   }

   private void drawMystery(GuiGraphics graphics, int cx, int cy, float scale, long now) {
      float pulse = 0.9F + 0.1F * (float)Math.sin((double)now / 180.0);
      graphics.pose().pushPose();
      graphics.pose().translate((float)cx, (float)cy, 300.0F);
      graphics.pose().scale(scale * 1.6F * pulse, scale * 1.6F * pulse, 1.0F);
      graphics.drawCenteredString(this.font, "?", 0, -4, 0xFF8A5900);
      graphics.pose().popPose();
   }

   private void renderReveal(GuiGraphics graphics) {
      long now = System.currentTimeMillis();
      // Elevated well above the grid's own Z (400, used so card labels/pills sit in front of item
      // icons) — otherwise this overlay wouldn't actually cover that text, letting it bleed through.
      graphics.pose().pushPose();
      graphics.pose().translate(0.0F, 0.0F, 450.0F);
      graphics.fill(0, 0, this.width, this.height, 0x99000000);
      graphics.pose().popPose();
      float elapsed = (float)(now - this.revealStartMs) / 220.0F;
      float pop = Math.min(1.0F, Math.max(0.0F, elapsed));
      float scale = 0.7F + 0.3F * easeOutBack(pop);
      int cardW = this.revealMilestone ? 220 : this.revealWeekly ? 200 : 160;
      int cardH = this.revealMilestone ? 170 : this.revealWeekly ? 150 : 130;
      int cx = this.width / 2;
      int cy = this.height / 2;
      graphics.pose().pushPose();
      graphics.pose().translate((float)cx, (float)cy, 500.0F);
      graphics.pose().scale(scale, scale, 1.0F);
      int x1 = -cardW / 2;
      int y1 = -cardH / 2;
      int x2 = cardW / 2;
      int y2 = cardH / 2;
      int textColor;
      if (this.revealMilestone) {
         float phase = (float)(now / 8L % 360L);
         WheelRenderer.rainbowRect(graphics, x1, y1, x2, y2, 8, phase);
         WheelRenderer.roundedRect(graphics, x1 + 3, y1 + 3, x2 - 3, y2 - 3, 6, CARD_WHITE_TOP, CARD_WHITE_BOTTOM);
         textColor = TEXT_DARK;
      } else {
         // The whole card is tinted to the rarity you actually got, not just a thin border on a
         // plain white panel — much easier to read at a glance what you just pulled.
         int rarityColor = this.revealRarity.color();
         int fillTop = WheelRenderer.lerpColor(rarityColor, -1, 0.6F);
         int fillBottom = WheelRenderer.lerpColor(rarityColor, -16777216, 0.2F);
         WheelRenderer.card(graphics, x1, y1, x2, y2, 8, rarityColor, fillTop, fillBottom);
         textColor = TEXT_WHITE;
      }

      Component title = Component.translatable(
         this.revealMilestone ? "gui.dailyrewards.milestone_title" : this.revealWeekly ? "gui.dailyrewards.weekly_title" : "gui.dailyrewards.reward_title"
      );
      graphics.drawCenteredString(this.font, title, 0, y1 + 10, textColor);
      graphics.drawCenteredString(this.font, Component.translatable(this.revealRarity.translationKey()), 0, y1 + 24, textColor);
      graphics.pose().pushPose();
      graphics.pose().translate(0.0F, (float)(y1 + 56), 0.0F);
      graphics.pose().scale(1.8F, 1.8F, 1.0F);
      graphics.renderItem(this.revealStack, -8, -8);
      graphics.pose().popPose();
      Component amountLine = Component.literal("+" + this.revealStack.getCount() + " ").append(this.revealStack.getHoverName());
      graphics.drawCenteredString(this.font, amountLine, 0, y1 + 82, textColor);
      if (this.revealStreakMilestone) {
         graphics.drawCenteredString(this.font, Component.translatable("gui.dailyrewards.streak_milestone", this.revealStreakValue), 0, y1 + 98, 0xFFFFD24C);
         if (!this.revealBonusStack.isEmpty()) {
            Component bonusLine = Component.literal("+" + this.revealBonusStack.getCount() + " ").append(this.revealBonusStack.getHoverName());
            graphics.drawCenteredString(this.font, bonusLine, 0, y1 + 110, 0xFFFFE49A);
         }
      }

      graphics.pose().popPose();
   }

   private static float easeOutBack(float t) {
      float c1 = 1.70158F;
      float c3 = c1 + 1.0F;
      float p = t - 1.0F;
      return 1.0F + c3 * p * p * p + c1 * p * p;
   }

   /** The pool shown in the flip/orbit preview. Weekly and milestone show their real featured pool;
    * every normal day shows the *entire* loot table across all rarities — a deliberate "look what's
    * possible" tease, not the actual (tier-limited) odds for that specific day. */
   private static List<RewardEntry> previewPool(int day) {
      if (day % DailyRewardConfig.MILESTONE_INTERVAL == 0) {
         return RewardPools.MILESTONE_REWARDS;
      } else if (day % DailyRewardConfig.WEEKLY_INTERVAL == 0) {
         List<RewardEntry> combined = new ArrayList<>(RewardPools.EPIC_REWARDS);
         combined.addAll(RewardPools.LEGENDARY_REWARDS);
         return combined;
      } else {
         List<RewardEntry> combined = new ArrayList<>();

         for (RewardRarity rarity : RewardRarity.values()) {
            if (rarity != RewardRarity.MYTHIC) {
               combined.addAll(RewardPools.pool(rarity));
            }
         }

         return combined;
      }
   }

   /**
    * Cycles through the preview pool plus one "?" slot, roughly every 650ms. Empty stack means "show
    * ?". Each (day, tick) pair is hashed independently, so different day cards genuinely show
    * different items in a different order rather than the same sequence just offset in time.
    */
   private static ItemStack previewIconFor(int day, long now) {
      List<RewardEntry> pool = previewPool(day);
      if (pool.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         int slots = pool.size() + 1;
         long tick = now / 650L;
         long h = tick * 0x9E3779B97F4A7C15L + (long)day * 0xBF58476D1CE4E5B9L;
         h ^= h >>> 30;
         h *= 0xBF58476D1CE4E5B9L;
         h ^= h >>> 27;
         h *= 0x94D049BB133111EBL;
         h ^= h >>> 31;
         long index = Math.floorMod(h, (long)slots);
         return index == (long)pool.size() ? ItemStack.EMPTY : new ItemStack(pool.get((int)index).previewItem());
      }
   }

   private List<DailyRewardScreen.SlotInfo> computeSlots() {
      int dayCount = DailyRewardClientState.dayCount();
      boolean claimableToday = DailyRewardClientState.claimableToday();
      int nextDay = dayCount + 1;
      int weekStart = (nextDay - 1) / 7 * 7 + 1;
      List<DailyRewardScreen.SlotInfo> slots = new ArrayList<>(7);

      for (int i = 0; i < 6; i++) {
         int day = weekStart + i;
         int col = i % 3;
         int row = i / 3;
         int x1 = this.leftPos + GRID_X_OFFSET + col * (CARD_W + GAP);
         int y1 = this.topPos + GRID_Y_OFFSET + row * (CARD_H + GAP);
         slots.add(new DailyRewardScreen.SlotInfo(day, x1, y1, x1 + CARD_W, y1 + CARD_H, false, slotState(day, nextDay, claimableToday)));
      }

      int bigDay = weekStart + 6;
      int bigX1 = this.leftPos + BIG_X_OFFSET;
      int bigY1 = this.topPos + GRID_Y_OFFSET;
      slots.add(new DailyRewardScreen.SlotInfo(bigDay, bigX1, bigY1, bigX1 + BIG_W, bigY1 + BIG_H, true, slotState(bigDay, nextDay, claimableToday)));
      return slots;
   }

   private static DailyRewardScreen.SlotState slotState(int day, int nextDay, boolean claimableToday) {
      if (day < nextDay) {
         return DailyRewardScreen.SlotState.CLAIMED;
      } else {
         return day == nextDay && claimableToday ? DailyRewardScreen.SlotState.AVAILABLE : DailyRewardScreen.SlotState.LOCKED;
      }
   }

   private static boolean inBox(int mouseX, int mouseY, int x, int y, int width, int height) {
      return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   private enum SlotState {
      LOCKED,
      AVAILABLE,
      CLAIMED;
   }

   private record SlotInfo(int day, int x1, int y1, int x2, int y2, boolean big, DailyRewardScreen.SlotState state) {
   }
}
