package net.execheinz.dailyrewards.reward;

/**
 * Reward rarity tiers, ordered lowest to highest. {@link #MYTHIC} is reserved for the every-50-day
 * milestone reward and is never rolled by the normal day-to-day generator.
 */
public enum RewardRarity {
   COMMON("gui.dailyrewards.rarity.common", 0xFF9AA0B0, 0xFF6B7080),
   UNCOMMON("gui.dailyrewards.rarity.uncommon", 0xFF3FCB6B, 0xFF1E9E4A),
   RARE("gui.dailyrewards.rarity.rare", 0xFF3F9BFF, 0xFF1D6FE0),
   EPIC("gui.dailyrewards.rarity.epic", 0xFFB24EFF, 0xFF8318E0),
   LEGENDARY("gui.dailyrewards.rarity.legendary", 0xFFFFB020, 0xFFE88A00),
   MYTHIC("gui.dailyrewards.rarity.mythic", 0xFFFF4FA3, 0xFF4FD6FF);

   private final String translationKey;
   private final int color;
   private final int glowColor;

   RewardRarity(String translationKey, int color, int glowColor) {
      this.translationKey = translationKey;
      this.color = color;
      this.glowColor = glowColor;
   }

   public String translationKey() {
      return this.translationKey;
   }

   /** Primary color used for card accents/text. */
   public int color() {
      return this.color;
   }

   /** Secondary color used for glow/gradient effects. */
   public int glowColor() {
      return this.glowColor;
   }

   /**
    * One tier up, capped at {@link #LEGENDARY} — used for the streak "luck" bonus, which should
    * never be able to promote a normal day's roll all the way to the milestone-only MYTHIC tier.
    */
   public RewardRarity upgraded() {
      return this == LEGENDARY || this == MYTHIC ? this : RewardRarity.values()[this.ordinal() + 1];
   }
}
