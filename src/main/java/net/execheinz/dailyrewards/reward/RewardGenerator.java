package net.execheinz.dailyrewards.reward;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/**
 * Turns (day number, streak) into an actual reward. Called exactly once, at the moment a claim is
 * processed on the server — never ahead of time — so the result can't be known or leaked early.
 */
public final class RewardGenerator {
   /** Day number determines general reward quality; RNG (and a little streak luck) picks the exact reward. */
   public static RewardRoll roll(ServerPlayer player, int dayNumber, int streak, RandomSource random) {
      if (dayNumber % DailyRewardConfig.MILESTONE_INTERVAL == 0) {
         ItemStack stack = pick(RewardPools.MILESTONE_REWARDS, random).roll(player, random);
         return new RewardRoll(RewardRarity.MYTHIC, stack, false, true);
      } else if (dayNumber % DailyRewardConfig.WEEKLY_INTERVAL == 0) {
         RewardRarity rarity = random.nextFloat() < DailyRewardConfig.WEEKLY_EPIC_CHANCE ? RewardRarity.EPIC : RewardRarity.LEGENDARY;
         ItemStack stack = pick(RewardPools.pool(rarity), random).roll(player, random);
         return new RewardRoll(rarity, stack, true, false);
      } else {
         RewardRarity rarity = rollNormalRarity(dayNumber, random);
         if (random.nextFloat() < DailyRewardConfig.luckBonus(streak)) {
            rarity = rarity.upgraded();
         }

         ItemStack stack = pick(RewardPools.pool(rarity), random).roll(player, random);
         applyStreakQuantityBonus(stack, streak);
         return new RewardRoll(rarity, stack, false, false);
      }
   }

   /** A small separate bonus, granted alongside (not instead of) the normal daily reward. */
   public static ItemStack rollStreakBonus(ServerPlayer player, RandomSource random) {
      return pick(RewardPools.STREAK_BONUS_REWARDS, random).roll(player, random);
   }

   private static RewardRarity rollNormalRarity(int dayNumber, RandomSource random) {
      float[] weights = DailyRewardConfig.oddsForDay(dayNumber);
      float total = 0.0F;

      for (float weight : weights) {
         total += weight;
      }

      float roll = random.nextFloat() * total;
      float cumulative = 0.0F;

      for (int i = 0; i < weights.length; i++) {
         cumulative += weights[i];
         if (roll < cumulative) {
            return RewardRarity.values()[i];
         }
      }

      return RewardRarity.values()[weights.length - 1];
   }

   private static void applyStreakQuantityBonus(ItemStack stack, int streak) {
      if (stack.getMaxStackSize() > 1 && stack.getCount() > 1) {
         float multiplier = DailyRewardConfig.quantityMultiplier(streak);
         if (multiplier > 1.0F) {
            int boosted = Math.round((float)stack.getCount() * multiplier);
            stack.setCount(Math.min(boosted, stack.getMaxStackSize()));
         }
      }
   }

   private static RewardEntry pick(List<RewardEntry> pool, RandomSource random) {
      return pool.get(random.nextInt(pool.size()));
   }

   private RewardGenerator() {
   }
}
