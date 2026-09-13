package net.execheinz.dailyrewards.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.LocalDate;
import net.execheinz.dailyrewards.reward.DailyRewardAttachments;
import net.execheinz.dailyrewards.reward.DailyRewardData;
import net.execheinz.dailyrewards.reward.DailyRewardManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Cheats-only testing command: {@code /dailyrewards skip|setday|reset|status}. Gated behind
 * permission level 2, the same level vanilla uses for {@code /give}, {@code /gamemode}, etc., so
 * it only works for ops or when "Allow Cheats" is on — never in normal survival play.
 */
public final class DailyRewardsCommand {
   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         Commands.literal("dailyrewards")
            .requires(source -> source.hasPermission(2))
            .then(
               Commands.literal("skip")
                  .then(
                     Commands.argument("days", IntegerArgumentType.integer(1))
                        .executes(context -> skip(context.getSource(), IntegerArgumentType.getInteger(context, "days")))
                  )
            )
            .then(
               Commands.literal("setday")
                  .then(
                     Commands.argument("day", IntegerArgumentType.integer(0))
                        .executes(context -> setDay(context.getSource(), IntegerArgumentType.getInteger(context, "day"), null))
                        .then(
                           Commands.argument("streak", IntegerArgumentType.integer(0))
                              .executes(
                                 context -> setDay(
                                       context.getSource(), IntegerArgumentType.getInteger(context, "day"), IntegerArgumentType.getInteger(context, "streak")
                                    )
                              )
                        )
                  )
            )
            .then(Commands.literal("reset").executes(context -> reset(context.getSource())))
            .then(Commands.literal("status").executes(context -> status(context.getSource())))
      );
   }

   /** Winds the last-claim date back N days, so the next claim is immediately available. Skipping exactly 1 day keeps the streak going; more than 1 breaks it, just like really missing days would. */
   private static int skip(CommandSourceStack source, int days) throws CommandSyntaxException {
      ServerPlayer player = source.getPlayerOrException();
      DailyRewardData data = player.getData(DailyRewardAttachments.DATA);
      data.lastClaimEpochDay -= (long)days;
      DailyRewardManager.requestSync(player);
      source.sendSuccess(() -> Component.literal("[Daily Rewards] Skipped " + days + " day(s) — you can claim again now."), false);
      return 1;
   }

   /** Jumps straight to a given day count (and optionally a streak), with the next claim immediately available. */
   private static int setDay(CommandSourceStack source, int day, Integer streak) throws CommandSyntaxException {
      ServerPlayer player = source.getPlayerOrException();
      DailyRewardData data = player.getData(DailyRewardAttachments.DATA);
      data.dayCount = Math.max(0, day);
      if (streak != null) {
         data.streak = Math.max(0, streak);
      }

      data.lastClaimEpochDay = LocalDate.now().toEpochDay() - 1L;
      data.recentClaims.clear();
      DailyRewardManager.requestSync(player);
      int nextDay = data.dayCount + 1;
      source.sendSuccess(
         () -> Component.literal("[Daily Rewards] Day set to " + data.dayCount + " (streak " + data.streak + "). Next claim will be Day " + nextDay + "."),
         false
      );
      return 1;
   }

   /** Wipes all progress and re-fires the first-join popup, so that flow can be tested repeatedly too. */
   private static int reset(CommandSourceStack source) throws CommandSyntaxException {
      ServerPlayer player = source.getPlayerOrException();
      DailyRewardData data = player.getData(DailyRewardAttachments.DATA);
      data.dayCount = 0;
      data.streak = 0;
      data.lastClaimEpochDay = -1L;
      data.lastStreakMilestoneAwarded = 0;
      data.introShown = false;
      data.recentClaims.clear();
      DailyRewardManager.onPlayerLogin(player);
      source.sendSuccess(() -> Component.literal("[Daily Rewards] Progress reset. The welcome popup will reappear."), false);
      return 1;
   }

   private static int status(CommandSourceStack source) throws CommandSyntaxException {
      ServerPlayer player = source.getPlayerOrException();
      DailyRewardData data = player.getData(DailyRewardAttachments.DATA);
      boolean claimableToday = LocalDate.now().toEpochDay() > data.lastClaimEpochDay;
      source.sendSuccess(
         () -> Component.literal(
               "[Daily Rewards] day="
                  + data.dayCount
                  + " streak="
                  + data.streak
                  + " lastClaimEpochDay="
                  + data.lastClaimEpochDay
                  + " claimableToday="
                  + claimableToday
            ),
         false
      );
      return 1;
   }

   private DailyRewardsCommand() {
   }
}
