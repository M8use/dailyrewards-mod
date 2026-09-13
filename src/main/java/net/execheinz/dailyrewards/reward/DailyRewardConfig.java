package net.execheinz.dailyrewards.reward;

/**
 * All the tunable numbers for the daily reward system in one place. Nothing else in this package
 * should hardcode a probability, interval, or bonus value — add it here instead.
 *
 * <p>Rarity weight arrays are {Common, Uncommon, Rare, Epic, Legendary} and don't need to sum to
 * any particular total; they're relative weights. This class is referenced from both client and
 * server code (the numbers themselves aren't secret — only the exact roll for a given claim is).
 */
public final class DailyRewardConfig {
   /** Every Nth claimed day is a big featured "weekly" reward (guaranteed Epic or Legendary). */
   public static final int WEEKLY_INTERVAL = 7;
   /** Every Nth claimed day is a guaranteed Mythic milestone reward. */
   public static final int MILESTONE_INTERVAL = 50;
   /** Chance the weekly reward rolls Epic instead of Legendary. */
   public static final float WEEKLY_EPIC_CHANCE = 0.75F;

   /** Days 1-6: still learning the ropes, so nothing above Rare. */
   public static final float[] EARLY_ODDS = {50.0F, 35.0F, 14.0F, 1.0F, 0.0F};
   /** Days 8-29 (multiples of 7 are always weekly rewards, so they're skipped here). */
   public static final float[] MID_ODDS = {20.0F, 40.0F, 30.0F, 10.0F, 0.0F};
   /** Day 30 onward: best average rewards, but still real variance. */
   public static final float[] LATE_ODDS = {1.0F, 34.0F, 35.0F, 25.0F, 5.0F};

   /**
    * Streak "luck": a chance to bump the rolled rarity up exactly one tier (never past Legendary).
    * Highest threshold the streak meets or exceeds wins. Index-aligned with {@link #STREAK_LUCK_CHANCE}.
    */
   public static final int[] STREAK_LUCK_THRESHOLDS = {3, 5, 7, 14, 30, 50};
   public static final float[] STREAK_LUCK_CHANCE = {0.05F, 0.10F, 0.15F, 0.22F, 0.30F, 0.35F};

   /** Quantity multiplier applied to stackable rewards only, capped and diminishing. */
   public static final int[] STREAK_QUANTITY_THRESHOLDS = {3, 5, 7, 14, 30, 50};
   public static final float[] STREAK_QUANTITY_MULTIPLIER = {1.0F, 1.1F, 1.2F, 1.3F, 1.4F, 1.5F};

   /** Streak lengths that hand out a small bonus reward (separate from the normal daily reward). */
   public static final int[] STREAK_MILESTONES = {7, 14, 30, 50, 100};

   /** Day range boundaries used to pick which odds table (and preview pool) applies to a given day. */
   public static final int EARLY_DAY_MAX = 6;
   public static final int MID_DAY_MAX = 29;

   public static float[] oddsForDay(int dayNumber) {
      return dayNumber <= EARLY_DAY_MAX ? EARLY_ODDS : dayNumber <= MID_DAY_MAX ? MID_ODDS : LATE_ODDS;
   }

   public static float luckBonus(int streak) {
      return lookup(STREAK_LUCK_THRESHOLDS, STREAK_LUCK_CHANCE, streak, 0.0F);
   }

   public static float quantityMultiplier(int streak) {
      return lookup(STREAK_QUANTITY_THRESHOLDS, STREAK_QUANTITY_MULTIPLIER, streak, 1.0F);
   }

   public static boolean isStreakMilestone(int streak) {
      for (int milestone : STREAK_MILESTONES) {
         if (milestone == streak || streak >= 100) {
            return true;
         }
      }

      return false;
   }

   private static float lookup(int[] thresholds, float[] values, int streak, float fallback) {
      float best = fallback;

      for (int i = 0; i < thresholds.length; i++) {
         if (streak >= thresholds[i]) {
            best = values[i];
         }
      }

      return best;
   }

   private DailyRewardConfig() {
   }
}
