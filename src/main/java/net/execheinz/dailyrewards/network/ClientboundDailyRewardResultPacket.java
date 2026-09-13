package net.execheinz.dailyrewards.network;

import java.util.List;
import net.execheinz.dailyrewards.reward.ClaimRecord;
import net.minecraft.network.FriendlyByteBuf;

public record ClientboundDailyRewardResultPacket(
   String rarityId,
   String itemId,
   int amount,
   boolean weekly,
   boolean milestone,
   int newDayCount,
   int newStreak,
   boolean streakMilestoneHit,
   int streakMilestoneValue,
   String bonusItemId,
   int bonusAmount,
   List<ClaimRecord> recentClaims,
   long nextResetEpochMillis
) {
   public void encode(FriendlyByteBuf buf) {
      buf.writeUtf(this.rarityId, 32);
      buf.writeUtf(this.itemId, 256);
      buf.writeVarInt(this.amount);
      buf.writeBoolean(this.weekly);
      buf.writeBoolean(this.milestone);
      buf.writeVarInt(this.newDayCount);
      buf.writeVarInt(this.newStreak);
      buf.writeBoolean(this.streakMilestoneHit);
      buf.writeVarInt(this.streakMilestoneValue);
      buf.writeUtf(this.bonusItemId, 256);
      buf.writeVarInt(this.bonusAmount);
      buf.writeCollection(this.recentClaims, (b, record) -> record.encode(b));
      buf.writeVarLong(this.nextResetEpochMillis);
   }

   public static ClientboundDailyRewardResultPacket decode(FriendlyByteBuf buf) {
      return new ClientboundDailyRewardResultPacket(
         buf.readUtf(32),
         buf.readUtf(256),
         buf.readVarInt(),
         buf.readBoolean(),
         buf.readBoolean(),
         buf.readVarInt(),
         buf.readVarInt(),
         buf.readBoolean(),
         buf.readVarInt(),
         buf.readUtf(256),
         buf.readVarInt(),
         buf.readList(ClaimRecord::decode),
         buf.readVarLong()
      );
   }
}
