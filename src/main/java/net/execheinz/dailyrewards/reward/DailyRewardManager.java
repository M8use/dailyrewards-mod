package net.execheinz.dailyrewards.reward;

import java.time.LocalDate;
import java.time.ZoneId;
import net.execheinz.dailyrewards.Ids;
import net.execheinz.dailyrewards.network.ClientboundDailyRewardResultPacket;
import net.execheinz.dailyrewards.network.ClientboundDailyRewardSyncPacket;
import net.execheinz.dailyrewards.network.DailyRewardNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/**
 * The server-authoritative entry points for the daily reward system.
 *
 * <p>"Today" is always computed fresh from the server's real-world clock ({@link LocalDate#now()}),
 * never from Minecraft ticks or client input, and every claim re-checks it against the persisted
 * {@link DailyRewardData#lastClaimEpochDay} before granting anything — that's what makes a claim
 * safe to request repeatedly (menu spam, reconnects, etc.) without ever duplicating a reward.
 */
public final class DailyRewardManager {
   /** Call on player login: syncs current state to the client, and flags a one-time welcome popup. */
   public static void onPlayerLogin(ServerPlayer player) {
      DailyRewardData data = player.getData(DailyRewardAttachments.DATA);
      boolean firstTime = !data.introShown;
      if (firstTime) {
         data.introShown = true;
      }

      sendSync(player, data, firstTime);
   }

   /** Call when the client wants a fresh read (e.g. opening the calendar screen), to catch a midnight rollover. */
   public static void requestSync(ServerPlayer player) {
      sendSync(player, player.getData(DailyRewardAttachments.DATA), false);
   }

   private static void sendSync(ServerPlayer player, DailyRewardData data, boolean forceOpen) {
      boolean claimable = LocalDate.now().toEpochDay() > data.lastClaimEpochDay;
      DailyRewardNetwork.toClient(
         player,
         new ClientboundDailyRewardSyncPacket(data.dayCount, data.streak, claimable, forceOpen, data.recentClaims, nextResetEpochMillis())
      );
   }

   private static long nextResetEpochMillis() {
      return LocalDate.now().plusDays(1L).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
   }

   /** Attempts to claim today's reward. Safe to call repeatedly; only ever grants once per real day. */
   public static void claim(ServerPlayer player) {
      DailyRewardData data = player.getData(DailyRewardAttachments.DATA);
      long today = LocalDate.now().toEpochDay();
      if (today <= data.lastClaimEpochDay) {
         requestSync(player);
         return;
      }

      boolean consecutive = data.lastClaimEpochDay >= 0L && today == data.lastClaimEpochDay + 1L;
      data.streak = consecutive ? data.streak + 1 : 1;
      data.dayCount++;
      data.lastClaimEpochDay = today;
      RandomSource random = player.getRandom();
      RewardRoll roll = RewardGenerator.roll(player, data.dayCount, data.streak, random);
      String rewardItemId = Ids.of(roll.stack().getItem());
      int rewardAmount = roll.stack().getCount();
      grant(player, roll.stack());
      recordClaim(data, new ClaimRecord(data.dayCount, rewardItemId, rewardAmount, roll.rarity().name()));
      boolean streakMilestoneHit = DailyRewardConfig.isStreakMilestone(data.streak) && data.lastStreakMilestoneAwarded < data.streak;
      String bonusItemId = "";
      int bonusAmount = 0;
      if (streakMilestoneHit) {
         data.lastStreakMilestoneAwarded = data.streak;
         ItemStack bonusStack = RewardGenerator.rollStreakBonus(player, random);
         bonusItemId = Ids.of(bonusStack.getItem());
         bonusAmount = bonusStack.getCount();
         grant(player, bonusStack);
      }

      DailyRewardNetwork.toClient(
         player,
         new ClientboundDailyRewardResultPacket(
            roll.rarity().name(),
            rewardItemId,
            rewardAmount,
            roll.weekly(),
            roll.milestone(),
            data.dayCount,
            data.streak,
            streakMilestoneHit,
            data.streak,
            bonusItemId,
            bonusAmount,
            data.recentClaims,
            nextResetEpochMillis()
         )
      );
   }

   /** Keeps only this week's claims: {@code recentClaims} is a history for display, not a full audit log. */
   private static void recordClaim(DailyRewardData data, ClaimRecord record) {
      int currentWeek = (record.day() - 1) / 7;
      data.recentClaims.removeIf(existing -> (existing.day() - 1) / 7 != currentWeek);
      data.recentClaims.removeIf(existing -> existing.day() == record.day());
      data.recentClaims.add(record);
   }

   private static void grant(ServerPlayer player, ItemStack stack) {
      if (!stack.isEmpty()) {
         player.getInventory().add(stack);
         if (!stack.isEmpty()) {
            player.drop(stack, false);
         }
      }
   }

   private DailyRewardManager() {
   }
}
