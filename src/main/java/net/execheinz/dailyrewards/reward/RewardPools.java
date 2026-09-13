package net.execheinz.dailyrewards.reward;

import java.util.List;
import java.util.Random;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Every possible daily reward, grouped by rarity. This is the one place to edit to add, remove,
 * or tweak rewards — {@link RewardGenerator} only ever picks a rarity and then a random entry
 * from that rarity's list here.
 */
public final class RewardPools {
   public static final List<RewardEntry> COMMON_REWARDS = List.of(
      RewardEntry.of(Items.COOKED_BEEF, 8, 16),
           RewardEntry.of(Items.DEAD_BUSH, 64, 64),
           RewardEntry.of(Items.FISHING_ROD, 1, 1),
      RewardEntry.of(Items.BREAD, 8, 16),
      RewardEntry.of(Items.COAL, 8, 16),
           RewardEntry.of(Items.LEATHER, 8, 16),
      RewardEntry.of(Items.TORCH, 16, 32),
           RewardEntry.of(Items.MELON_SEEDS, 3, 6),
           RewardEntry.of(Items.PUMPKIN_SEEDS, 3, 6),
      RewardEntry.of(Items.BEETROOT_SEEDS, 3, 6),
      RewardEntry.of(Items.COCOA_BEANS, 3, 6),
           RewardEntry.of(Items.CHERRY_SAPLING, 1, 3),
      RewardEntry.of(Items.COBBLESTONE, 32, 64),
      RewardEntry.of(Items.OAK_LOG, 8, 16),
      RewardEntry.of(Items.ARROW, 16, 32)
   );
   public static final List<RewardEntry> UNCOMMON_REWARDS = List.of(
      RewardEntry.of(Items.IRON_INGOT, 4, 8),
           RewardEntry.of(Items.WHITE_WOOL, 16, 64),
           RewardEntry.of(Items.GUNPOWDER, 10, 25),
      RewardEntry.of(Items.GOLD_INGOT, 4, 8),
      RewardEntry.of(Items.IRON_PICKAXE, 1),
      RewardEntry.of(Items.IRON_SWORD, 1),
           RewardEntry.of(Items.LAPIS_BLOCK, 2, 4),
      RewardEntry.of(Items.GOLDEN_APPLE, 1, 2),
      RewardEntry.of(Items.EXPERIENCE_BOTTLE, 4, 16),
      RewardEntry.of(Items.REDSTONE_BLOCK, 1, 3)
   );
   public static final List<RewardEntry> RARE_REWARDS = List.of(
      RewardEntry.of(Items.IRON_BLOCK, 2, 4),
      RewardEntry.of(Items.GOLDEN_CARROT, 8, 16),
      RewardEntry.of(Items.EMERALD, 8, 16),
      RewardEntry.enchantedBook(Enchantments.SHARPNESS, 2),
      RewardEntry.enchantedBook(Enchantments.EFFICIENCY, 3),
           RewardEntry.of(Items.SLIME_BALL, 10, 32),
           RewardEntry.of(Items.GOLD_INGOT, 10, 20)
   );
   public static final List<RewardEntry> EPIC_REWARDS = List.of(
      RewardEntry.of(Items.DIAMOND, 6, 10),
           RewardEntry.of(Items.SPONGE, 1, 8),
           RewardEntry.of(Items.SADDLE, 1, 1),
           RewardEntry.of(Items.TRIDENT, 1, 1),
        RewardEntry.of(Items.GOLDEN_CARROT, 16, 32),
           RewardEntry.of(Items.BLAZE_ROD, 10, 25),
           RewardEntry.enchantedTool(Items.BOW, Enchantments.POWER, new Random().nextInt(5) + 1),
        RewardEntry.enchantedBook(Enchantments.UNBREAKING, 3),
      RewardEntry.enchantedTool(Items.DIAMOND_PICKAXE, Enchantments.EFFICIENCY, 4),
      RewardEntry.enchantedTool(Items.DIAMOND_SWORD, Enchantments.SHARPNESS, 4),
      RewardEntry.enchantedBook(Enchantments.MENDING, 1),
      RewardEntry.of(Items.GOLDEN_APPLE, 4, 6),
           RewardEntry.of(Items.NAUTILUS_SHELL, 1, 8),
           RewardEntry.of(Items.EMERALD, 16, 24),
           RewardEntry.of(Items.OBSIDIAN, 16, 64)
           );
   public static final List<RewardEntry> LEGENDARY_REWARDS = List.of(
      RewardEntry.of(Items.DIAMOND, 12, 20),
           RewardEntry.of(Items.BREEZE_ROD, 32, 64),
      RewardEntry.of(Items.NETHERITE_INGOT, 2, 3),
           RewardEntry.of(Items.ENDER_EYE, 16, 64),
           RewardEntry.of(Items.WITHER_SKELETON_SKULL, 1, 2),
           RewardEntry.of(Items.ENCHANTED_GOLDEN_APPLE, 1),
           RewardEntry.of(Items.ANCIENT_DEBRIS, 10, 20),
           RewardEntry.enchantedTool(Items.DIAMOND_PICKAXE, Enchantments.FORTUNE, 3),
      RewardEntry.enchantedTool(Items.DIAMOND_SWORD, Enchantments.SHARPNESS, 5),
      RewardEntry.of(Items.TOTEM_OF_UNDYING, 1),
           RewardEntry.of(Items.END_CRYSTAL, 5, 64)

           );
   public static final List<RewardEntry> MILESTONE_REWARDS = List.of(
      RewardEntry.of(Items.NETHERITE_INGOT, 4, 6),
      RewardEntry.of(Items.TOTEM_OF_UNDYING, 5),
           RewardEntry.enchantedTool(Items.MACE, Enchantments.WIND_BURST, 3),
           RewardEntry.enchantedTool(Items.MACE, Enchantments.DENSITY, 5),
           RewardEntry.enchantedTool(Items.MACE, Enchantments.BREACH, 4),
           RewardEntry.of(Items.ENCHANTED_GOLDEN_APPLE, 2, 5),
      RewardEntry.enchantedTool(Items.NETHERITE_PICKAXE, Enchantments.EFFICIENCY, 5),
           RewardEntry.of(Items.DRAGON_EGG, 1, 1),
           RewardEntry.enchantedTool(Items.NETHERITE_SWORD, Enchantments.SHARPNESS, 5),
      RewardEntry.of(Items.NETHER_STAR, 1),
      RewardEntry.of(Items.ELYTRA, 1),
      RewardEntry.of(Items.DIAMOND, 32, 64)
   );

   /** Bonus rewards handed out separately when a streak milestone (7, 14, 30, 50, 100...) is hit. */
   public static final List<RewardEntry> STREAK_BONUS_REWARDS = List.of(
      RewardEntry.of(Items.GOLDEN_CARROT, 8, 16),
      RewardEntry.of(Items.DIAMOND, 3, 6),
      RewardEntry.of(Items.EMERALD, 12, 20),
      RewardEntry.enchantedBook(Enchantments.UNBREAKING, 2)
   );

   public static List<RewardEntry> pool(RewardRarity rarity) {
      return switch (rarity) {
         case COMMON -> COMMON_REWARDS;
         case UNCOMMON -> UNCOMMON_REWARDS;
         case RARE -> RARE_REWARDS;
         case EPIC -> EPIC_REWARDS;
         case LEGENDARY -> LEGENDARY_REWARDS;
         case MYTHIC -> MILESTONE_REWARDS;
      };
   }

   private RewardPools() {
   }
}
