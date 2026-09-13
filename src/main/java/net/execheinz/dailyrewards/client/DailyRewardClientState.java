package net.execheinz.dailyrewards.client;

import java.util.List;
import net.execheinz.dailyrewards.reward.ClaimRecord;

/**
 * The client's best-known copy of the daily reward status, updated whenever a sync or result
 * packet arrives from the server. The server remains the source of truth for whether a claim is
 * actually valid — this is only used to decide what to draw (button glow, calendar layout).
 */
public final class DailyRewardClientState {
   private static int dayCount;
   private static int streak;
   private static boolean claimableToday;
   private static boolean everSynced;
   private static boolean pendingForceOpen;
   private static List<ClaimRecord> recentClaims = List.of();
   private static long nextResetEpochMillis;

   public static void apply(int dayCount, int streak, boolean claimableToday, List<ClaimRecord> recentClaims, long nextResetEpochMillis) {
      DailyRewardClientState.dayCount = dayCount;
      DailyRewardClientState.streak = streak;
      DailyRewardClientState.claimableToday = claimableToday;
      DailyRewardClientState.recentClaims = recentClaims;
      DailyRewardClientState.nextResetEpochMillis = nextResetEpochMillis;
      DailyRewardClientState.everSynced = true;
   }

   public static int dayCount() {
      return dayCount;
   }

   public static int streak() {
      return streak;
   }

   public static boolean claimableToday() {
      return claimableToday;
   }

   /** False until the first sync packet has ever arrived (e.g. the instant after login). */
   public static boolean everSynced() {
      return everSynced;
   }

   public static ClaimRecord claimFor(int day) {
      for (ClaimRecord record : recentClaims) {
         if (record.day() == day) {
            return record;
         }
      }

      return null;
   }

   /** Epoch millis the current lockout ends at (start of the next real-world calendar day). */
   public static long nextResetEpochMillis() {
      return nextResetEpochMillis;
   }

   /** Set when a sync says to auto-open the calendar; consumed once the client is safely in-game. */
   public static void setPendingForceOpen(boolean pending) {
      pendingForceOpen = pending;
   }

   public static boolean pendingForceOpen() {
      return pendingForceOpen;
   }

   private DailyRewardClientState() {
   }
}
