package net.execheinz.dailyrewards.neoforge;

import net.execheinz.dailyrewards.client.ClientPacketHandler;
import net.execheinz.dailyrewards.network.ClientboundDailyRewardResultPacket;
import net.execheinz.dailyrewards.network.ClientboundDailyRewardSyncPacket;
import net.execheinz.dailyrewards.network.DailyRewardNetwork;
import net.execheinz.dailyrewards.network.ServerboundClaimDailyRewardPacket;
import net.execheinz.dailyrewards.network.ServerboundRequestDailySyncPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NeoForgeNetwork {
   static ResourceLocation id(String path) {
      return ResourceLocation.fromNamespaceAndPath("dailyrewards", path);
   }

   public static void register(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar("1");
      registrar.playToServer(
         NeoForgeNetwork.RequestSync.TYPE,
         NeoForgeNetwork.RequestSync.CODEC,
         (payload, context) -> context.enqueueWork(() -> payload.inner().handle((ServerPlayer)context.player()))
      );
      registrar.playToServer(
         NeoForgeNetwork.ClaimReward.TYPE,
         NeoForgeNetwork.ClaimReward.CODEC,
         (payload, context) -> context.enqueueWork(() -> payload.inner().handle((ServerPlayer)context.player()))
      );
      registrar.playToClient(
         NeoForgeNetwork.Sync.TYPE, NeoForgeNetwork.Sync.CODEC, (payload, context) -> context.enqueueWork(() -> ClientPacketHandler.handleDailySync(payload.inner()))
      );
      registrar.playToClient(
         NeoForgeNetwork.Result.TYPE,
         NeoForgeNetwork.Result.CODEC,
         (payload, context) -> context.enqueueWork(() -> ClientPacketHandler.handleDailyResult(payload.inner()))
      );
      DailyRewardNetwork.bind(new DailyRewardNetwork.Sender() {
         @Override
         public void toClient(ServerPlayer player, Object message) {
            if (message instanceof ClientboundDailyRewardSyncPacket packet) {
               PacketDistributor.sendToPlayer(player, new NeoForgeNetwork.Sync(packet), new CustomPacketPayload[0]);
            } else if (message instanceof ClientboundDailyRewardResultPacket packet) {
               PacketDistributor.sendToPlayer(player, new NeoForgeNetwork.Result(packet), new CustomPacketPayload[0]);
            }
         }

         @Override
         public void toServer(Object message) {
            if (message instanceof ServerboundRequestDailySyncPacket packet) {
               PacketDistributor.sendToServer(new NeoForgeNetwork.RequestSync(packet), new CustomPacketPayload[0]);
            } else if (message instanceof ServerboundClaimDailyRewardPacket packet) {
               PacketDistributor.sendToServer(new NeoForgeNetwork.ClaimReward(packet), new CustomPacketPayload[0]);
            }
         }
      });
   }

   private NeoForgeNetwork() {
   }

   public static record RequestSync(ServerboundRequestDailySyncPacket inner) implements CustomPacketPayload {
      public static final Type<NeoForgeNetwork.RequestSync> TYPE = new Type(NeoForgeNetwork.id("request_sync"));
      public static final StreamCodec<RegistryFriendlyByteBuf, NeoForgeNetwork.RequestSync> CODEC = StreamCodec.of(
         (buf, msg) -> msg.inner().encode(buf), buf -> new NeoForgeNetwork.RequestSync(ServerboundRequestDailySyncPacket.decode(buf))
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record ClaimReward(ServerboundClaimDailyRewardPacket inner) implements CustomPacketPayload {
      public static final Type<NeoForgeNetwork.ClaimReward> TYPE = new Type(NeoForgeNetwork.id("claim_reward"));
      public static final StreamCodec<RegistryFriendlyByteBuf, NeoForgeNetwork.ClaimReward> CODEC = StreamCodec.of(
         (buf, msg) -> msg.inner().encode(buf), buf -> new NeoForgeNetwork.ClaimReward(ServerboundClaimDailyRewardPacket.decode(buf))
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record Sync(ClientboundDailyRewardSyncPacket inner) implements CustomPacketPayload {
      public static final Type<NeoForgeNetwork.Sync> TYPE = new Type(NeoForgeNetwork.id("sync"));
      public static final StreamCodec<RegistryFriendlyByteBuf, NeoForgeNetwork.Sync> CODEC = StreamCodec.of(
         (buf, msg) -> msg.inner().encode(buf), buf -> new NeoForgeNetwork.Sync(ClientboundDailyRewardSyncPacket.decode(buf))
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record Result(ClientboundDailyRewardResultPacket inner) implements CustomPacketPayload {
      public static final Type<NeoForgeNetwork.Result> TYPE = new Type(NeoForgeNetwork.id("result"));
      public static final StreamCodec<RegistryFriendlyByteBuf, NeoForgeNetwork.Result> CODEC = StreamCodec.of(
         (buf, msg) -> msg.inner().encode(buf), buf -> new NeoForgeNetwork.Result(ClientboundDailyRewardResultPacket.decode(buf))
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }
}
