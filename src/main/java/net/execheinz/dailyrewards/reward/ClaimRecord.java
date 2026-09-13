package net.execheinz.dailyrewards.reward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;

/**
 * What a player actually received on a given day. Kept for the current week only (see
 * {@link DailyRewardManager}), so the calendar can show "Day 3: +12 Golden Carrot" instead of just
 * a checkmark once a card has been claimed.
 */
public record ClaimRecord(int day, String itemId, int amount, String rarityId) {
   public static final Codec<ClaimRecord> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.INT.fieldOf("day").forGetter(ClaimRecord::day),
            Codec.STRING.fieldOf("item").forGetter(ClaimRecord::itemId),
            Codec.INT.fieldOf("amount").forGetter(ClaimRecord::amount),
            Codec.STRING.fieldOf("rarity").forGetter(ClaimRecord::rarityId)
         )
         .apply(instance, ClaimRecord::new)
   );

   public void encode(FriendlyByteBuf buf) {
      buf.writeVarInt(this.day);
      buf.writeUtf(this.itemId, 256);
      buf.writeVarInt(this.amount);
      buf.writeUtf(this.rarityId, 32);
   }

   public static ClaimRecord decode(FriendlyByteBuf buf) {
      return new ClaimRecord(buf.readVarInt(), buf.readUtf(256), buf.readVarInt(), buf.readUtf(32));
   }
}
