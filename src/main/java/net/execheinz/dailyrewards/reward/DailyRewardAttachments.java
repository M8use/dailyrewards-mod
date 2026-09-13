package net.execheinz.dailyrewards.reward;

import net.execheinz.dailyrewards.DailyRewardsMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class DailyRewardAttachments {
   private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
      NeoForgeRegistries.Keys.ATTACHMENT_TYPES, DailyRewardsMod.MODID
   );

   /** Per-player daily reward progress. Serialized to disk and copied across respawns/deaths. */
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<DailyRewardData>> DATA = ATTACHMENT_TYPES.register(
      "daily_reward_data", () -> AttachmentType.builder(DailyRewardData::new).serialize(DailyRewardData.CODEC).copyOnDeath().build()
   );

   public static void register(IEventBus modBus) {
      ATTACHMENT_TYPES.register(modBus);
   }

   private DailyRewardAttachments() {
   }
}
