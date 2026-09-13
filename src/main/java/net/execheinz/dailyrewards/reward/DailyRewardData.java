package net.execheinz.dailyrewards.reward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistent per-player state for the Daily Rewards system. Stored as a NeoForge data attachment
 * on the player entity, which is saved/loaded with the player's normal NBT data (playerdata
 * files), so it survives world reloads, restarts, and crashes.
 *
 * <p>This is a plain mutable holder: callers read the current attachment instance via
 * {@code player.getData(DailyRewardAttachments.DATA)} and mutate its fields directly. Entity
 * attachments are re-serialized from the live object at save time, so no extra "mark dirty" or
 * re-{@code setData} call is required after mutating fields (unlike block entities/chunks).
 */
public final class DailyRewardData {
   /** How many daily rewards have been successfully claimed, ever. Never resets. */
   public int dayCount;
   /** Consecutive real-life calendar days claimed in a row. Resets to 0 on a missed day. */
   public int streak;
   /** Epoch day (see {@link java.time.LocalDate#toEpochDay()}) the last reward was claimed on, or -1 if never. */
   public long lastClaimEpochDay = -1L;
   /** Highest streak length a streak-milestone bonus has already been granted for. Prevents double-grants. */
   public int lastStreakMilestoneAwarded;
   /** Whether the "welcome" auto-open has already been shown to this player on this world/save. */
   public boolean introShown;
   /** What was claimed on each day of the current week only; cleared automatically once the week rolls over. */
   public List<ClaimRecord> recentClaims = new ArrayList<>();

   public DailyRewardData() {
   }

   public DailyRewardData(
      int dayCount, int streak, long lastClaimEpochDay, int lastStreakMilestoneAwarded, boolean introShown, List<ClaimRecord> recentClaims
   ) {
      this.dayCount = dayCount;
      this.streak = streak;
      this.lastClaimEpochDay = lastClaimEpochDay;
      this.lastStreakMilestoneAwarded = lastStreakMilestoneAwarded;
      this.introShown = introShown;
      this.recentClaims = new ArrayList<>(recentClaims);
   }

   public static final Codec<DailyRewardData> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.INT.fieldOf("day_count").forGetter(data -> data.dayCount),
            Codec.INT.fieldOf("streak").forGetter(data -> data.streak),
            Codec.LONG.fieldOf("last_claim_epoch_day").forGetter(data -> data.lastClaimEpochDay),
            Codec.INT.fieldOf("last_streak_milestone_awarded").forGetter(data -> data.lastStreakMilestoneAwarded),
            Codec.BOOL.fieldOf("intro_shown").forGetter(data -> data.introShown),
            ClaimRecord.CODEC.listOf().fieldOf("recent_claims").forGetter(data -> data.recentClaims)
         )
         .apply(instance, DailyRewardData::new)
   );
}
