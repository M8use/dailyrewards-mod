package net.execheinz.dailyrewards.reward;

import java.util.function.BiFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/**
 * One possible reward inside a {@link RewardRarity} pool. Add new rewards by calling one of the
 * static factories below in {@link RewardPools} — nothing else needs to change.
 */
public final class RewardEntry {
   private final BiFunction<ServerPlayer, RandomSource, ItemStack> factory;
   private final Item previewItem;

   private RewardEntry(Item previewItem, BiFunction<ServerPlayer, RandomSource, ItemStack> factory) {
      this.previewItem = previewItem;
      this.factory = factory;
   }

   /** A plain stack of {@code item}, random count between {@code minAmount} and {@code maxAmount} (inclusive). */
   public static RewardEntry of(Item item, int minAmount, int maxAmount) {
      return new RewardEntry(item, (player, random) -> new ItemStack(item, randomAmount(random, minAmount, maxAmount)));
   }

   /** A plain stack of exactly {@code amount} of {@code item}. */
   public static RewardEntry of(Item item, int amount) {
      return of(item, amount, amount);
   }

   /** A single enchanted tool/weapon/armor piece with one enchantment at a fixed level. */
   public static RewardEntry enchantedTool(Item item, ResourceKey<Enchantment> enchantment, int level) {
      return new RewardEntry(item, (player, random) -> {
         ItemStack stack = new ItemStack(item);
         stack.enchant(enchantmentHolder(player, enchantment), level);
         return stack;
      });
   }

   /** A single enchanted book carrying one enchantment at a fixed level. */
   public static RewardEntry enchantedBook(ResourceKey<Enchantment> enchantment, int level) {
      return new RewardEntry(
         net.minecraft.world.item.Items.ENCHANTED_BOOK,
         (player, random) -> EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantmentHolder(player, enchantment), level))
      );
   }

   private static Holder<Enchantment> enchantmentHolder(ServerPlayer player, ResourceKey<Enchantment> key) {
      return player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
   }

   private static int randomAmount(RandomSource random, int min, int max) {
      return min >= max ? min : min + random.nextInt(max - min + 1);
   }

   /** Rolls a concrete stack for this entry. Called once per claim, never before. */
   public ItemStack roll(ServerPlayer player, RandomSource random) {
      return this.factory.apply(player, random);
   }

   /** A representative item for client-only "possible reward" flip previews. Never used for the actual grant. */
   public Item previewItem() {
      return this.previewItem;
   }
}
