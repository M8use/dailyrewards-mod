package net.execheinz.dailyrewards.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Vanilla draws active status effect icons to the right of the inventory panel (that changed in
 * 1.18 - before that they were on the left). This mod (and others, like the gamble button) add
 * their own buttons in that same spot, so with more than a couple of effects active the vanilla
 * panel covers them. This mixin moves the effect panel back to the left of the inventory instead.
 *
 * <p>An earlier version of this mixin used {@code @ModifyVariable} at {@code @At("STORE")} on the
 * horizontal-offset local. That local is written to <em>twice</em> in {@code renderEffects} — once
 * when first computed, and again right after, when it's reassigned from
 * {@code ClientHooks.onScreenPotionSize(...)}'s result — so a bare {@code @At("STORE")} (which
 * matches every store to that variable, not just the first) fired on both, and the second firing
 * saw the already-shifted value as its input, compounding the offset instead of just applying it
 * once. Redirecting the single call to {@code onScreenPotionSize} instead only fires once, so
 * there's nothing to compound: it shifts the value that feeds the event, and vanilla takes it from
 * there untouched.
 *
 * <p>Vanilla's own x is {@code leftPos + imageWidth + 2}; working backwards from that captured
 * value gets to the same {@code leftPos - 124} the old pre-1.18 left-side layout used, without
 * needing to reference {@code leftPos} directly (which — see the note that used to be here — isn't
 * declared on this class and can't be {@code @Shadow}'d without a refmap). 176 is the standard
 * player-inventory panel width and has been stable for a very long time.
 *
 * <p>This only touches the x position; whether the panel draws in "wide" (icon + description) or
 * "compact" (icon only, tooltip on hover) mode is still vanilla's own decision and is left alone.
 */
@Mixin(EffectRenderingInventoryScreen.class)
public abstract class EffectRenderingInventoryScreenMixin {
   @Redirect(
      method = "renderEffects",
      at = @At(
            value = "INVOKE",
            target = "Lnet/neoforged/neoforge/client/ClientHooks;onScreenPotionSize(Lnet/minecraft/client/gui/screens/Screen;IZI)Lnet/neoforged/neoforge/client/event/ScreenEvent$RenderInventoryMobEffects;"
         )
   )
   private static ScreenEvent.RenderInventoryMobEffects dailyrewards$moveEffectsToLeft(Screen screen, int availableSpace, boolean compact, int horizontalOffset) {
      return ClientHooks.onScreenPotionSize(screen, availableSpace, compact, horizontalOffset - 302);
   }
}
