package net.execheinz.dailyrewards.client;

import net.execheinz.dailyrewards.network.ClientboundDailyRewardResultPacket;
import net.execheinz.dailyrewards.network.ClientboundDailyRewardSyncPacket;
import net.minecraft.client.Minecraft;

public final class ClientPacketHandler {
   public static void handleDailySync(ClientboundDailyRewardSyncPacket packet) {
      DailyRewardClientState.apply(packet.dayCount(), packet.streak(), packet.claimableToday(), packet.recentClaims(), packet.nextResetEpochMillis());
      if (packet.forceOpen()) {
         // Don't try to open right here: this packet can arrive while a loading/connecting screen is
         // still up (very common right after creating a new world), so opening now would silently no-op.
         // DailyRewardsNeoForge polls this flag every client tick and opens as soon as it's actually safe.
         DailyRewardClientState.setPendingForceOpen(true);
      } else if (Minecraft.getInstance().screen instanceof DailyRewardScreen screen) {
         screen.onSync();
      }
   }

   public static void handleDailyResult(ClientboundDailyRewardResultPacket packet) {
      DailyRewardClientState.apply(packet.newDayCount(), packet.newStreak(), false, packet.recentClaims(), packet.nextResetEpochMillis());
      if (Minecraft.getInstance().screen instanceof DailyRewardScreen screen) {
         screen.onClaimResult(packet);
      }
   }

   private ClientPacketHandler() {
   }
}
