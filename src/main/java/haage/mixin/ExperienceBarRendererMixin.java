package haage.mixin;

import haage.LocatorHeads;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ExperienceBarRenderer;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for ExperienceBarRenderer to add support for simultaneous display
 * of both XP bar and locator heads when the "Always Show XP" feature is enabled.
 */
@Mixin(ExperienceBarRenderer.class)
public abstract class ExperienceBarRendererMixin {
    
    @Shadow
    @Final
    private Minecraft minecraft;
    
    /**
     * Injects after the experience bar has finished rendering.
     * If the "Always Show XP" config option is enabled, this will additionally
     * render the locator bar overlay, allowing both UI elements to be visible simultaneously.
     * Also renders the compass if enabled.
    
     @param guiGraphics The graphics context for rendering
     @param deltaTracker The delta tracker for animation timing
     @param ci Callback info for the injection
     */
    @Inject(method = "*", at = @At("RETURN"))
    private void locatorHeads$addLocatorOverlayToExperienceBar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        // Only proceed if config is loaded and feature is enabled
        if (LocatorHeads.CONFIG == null || !LocatorHeads.CONFIG.alwaysShowXP) {
            return;
        }
        
        // Render the locator bar on top of the experience bar
        LocatorBarRenderer renderer = new LocatorBarRenderer(this.minecraft);
        renderer.render(guiGraphics, deltaTracker);
    }
}
