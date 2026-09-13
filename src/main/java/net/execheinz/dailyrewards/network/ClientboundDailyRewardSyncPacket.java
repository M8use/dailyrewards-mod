package net.execheinz.dailyrewards.network;

import java.util.List;
import net.execheinz.dailyrewards.reward.ClaimRecord;
import net.minecraft.network.FriendlyByteBuf;

public record ClientboundDailyRewardSyncPacket(
   int dayCount, int streak, boolean claimableToday, boolean forceOpen, List<ClaimRecord> recentClaims, long nextResetEpochMillis
) {
   public void encode(FriendlyByteBuf buf) {
      buf.writeVarInt(this.dayCount);
      buf.writeVarInt(this.streak);
      buf.writeBoolean(this.claimableToday);
      buf.writeBoolean(this.forceOpen);
      buf.writeCollection(this.recentClaims, (b, record) -> record.encode(b));
      buf.writeVarLong(this.nextResetEpochMillis);
   }

   public static ClientboundDailyRewardSyncPacket decode(FriendlyByteBuf buf) {
      return new ClientboundDailyRewardSyncPacket(
         buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean(), buf.readList(ClaimRecord::decode), buf.readVarLong()
      );
   }
}
