package net.execheinz.dailyrewards.network;

import net.execheinz.dailyrewards.reward.DailyRewardManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record ServerboundClaimDailyRewardPacket() {
   public void encode(FriendlyByteBuf buf) {
   }

   public static ServerboundClaimDailyRewardPacket decode(FriendlyByteBuf buf) {
      return new ServerboundClaimDailyRewardPacket();
   }

   public void handle(ServerPlayer player) {
      if (player != null) {
         DailyRewardManager.claim(player);
      }
   }
}
