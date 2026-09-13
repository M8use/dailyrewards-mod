package net.execheinz.dailyrewards.reward;

import net.minecraft.world.item.ItemStack;

public record RewardRoll(RewardRarity rarity, ItemStack stack, boolean weekly, boolean milestone) {
}
