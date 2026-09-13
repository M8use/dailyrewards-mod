package net.execheinz.dailyrewards.network;

import net.execheinz.dailyrewards.reward.DailyRewardManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record ServerboundRequestDailySyncPacket() {
   public void encode(FriendlyByteBuf buf) {
   }

   public static ServerboundRequestDailySyncPacket decode(FriendlyByteBuf buf) {
      return new ServerboundRequestDailySyncPacket();
   }

   public void handle(ServerPlayer player) {
      if (player != null) {
         DailyRewardManager.requestSync(player);
      }
   }
}
