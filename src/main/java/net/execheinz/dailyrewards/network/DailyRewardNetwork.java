package net.execheinz.dailyrewards.network;

import net.minecraft.server.level.ServerPlayer;

public final class DailyRewardNetwork {
   private static DailyRewardNetwork.Sender sender = new DailyRewardNetwork.Sender() {
      @Override
      public void toClient(ServerPlayer player, Object message) {
      }

      @Override
      public void toServer(Object message) {
      }
   };

   public static void bind(DailyRewardNetwork.Sender platform) {
      sender = platform;
   }

   public static void toClient(ServerPlayer player, Object message) {
      sender.toClient(player, message);
   }

   public static void toServer(Object message) {
      sender.toServer(message);
   }

   private DailyRewardNetwork() {
   }

   public interface Sender {
      void toClient(ServerPlayer player, Object message);

      void toServer(Object message);
   }
}
