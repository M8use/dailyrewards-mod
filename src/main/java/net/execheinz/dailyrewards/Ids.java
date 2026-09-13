package net.execheinz.dailyrewards;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public final class Ids {
   public static String of(Item item) {
      ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
      return key == null ? "" : key.toString();
   }

   @Nullable
   public static Item item(String id) {
      if (id != null && !id.isEmpty()) {
         ResourceLocation key = ResourceLocation.tryParse(id);
         return key == null ? null : (Item)BuiltInRegistries.ITEM.get(key);
      } else {
         return null;
      }
   }

   private Ids() {
   }
}
