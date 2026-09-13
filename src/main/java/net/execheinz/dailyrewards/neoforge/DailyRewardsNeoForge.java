package net.execheinz.dailyrewards.neoforge;

import net.execheinz.dailyrewards.client.DailyRewardButton;
import net.execheinz.dailyrewards.client.DailyRewardClientState;
import net.execheinz.dailyrewards.client.DailyRewardScreen;
import net.execheinz.dailyrewards.command.DailyRewardsCommand;
import net.execheinz.dailyrewards.reward.DailyRewardAttachments;
import net.execheinz.dailyrewards.reward.DailyRewardManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod("dailyrewards")
public class DailyRewardsNeoForge {
   public DailyRewardsNeoForge(IEventBus modBus, ModContainer container) {
      DailyRewardAttachments.register(modBus);
      modBus.addListener(NeoForgeNetwork::register);
      NeoForge.EVENT_BUS.addListener(this::onInventoryScreenInit);
      NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
      NeoForge.EVENT_BUS.addListener(this::onClientTick);
      NeoForge.EVENT_BUS.addListener(this::registerCommands);
   }

   private void registerCommands(RegisterCommandsEvent event) {
      DailyRewardsCommand.register(event.getDispatcher());
   }

   private void onInventoryScreenInit(ScreenEvent.Init.Post event) {
      if (event.getScreen() instanceof InventoryScreen inventory) {
         event.addListener(DailyRewardButton.create(inventory));
      }
   }

   private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer serverPlayer) {
         DailyRewardManager.onPlayerLogin(serverPlayer);
      }
   }

   private void onClientTick(ClientTickEvent.Post event) {
      if (DailyRewardClientState.pendingForceOpen()) {
         Minecraft minecraft = Minecraft.getInstance();
         if (minecraft.player != null && minecraft.level != null && minecraft.screen == null) {
            DailyRewardClientState.setPendingForceOpen(false);
            minecraft.setScreen(new DailyRewardScreen());
         }
      }
   }
}
