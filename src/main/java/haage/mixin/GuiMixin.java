package haage.mixin;

import haage.LocatorHeads;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Gui.class)
public class GuiMixin {
    
    @Shadow
    @Final
    private Minecraft minecraft;
    
    /**
     * Inject into the method that determines if the experience bar should be prioritized.
     * When alwaysShowXP is enabled, force it to return true so the XP bar is always shown.
     */
    @Inject(method = "*()Z", at = @At("RETURN"), cancellable = true)
    private void locatorHeads$alwaysShowExperienceBar(CallbackInfoReturnable<Boolean> cir) {
        if (LocatorHeads.CONFIG != null && LocatorHeads.CONFIG.alwaysShowXP) {
            cir.setReturnValue(true);
        }
    }
    
    /**
     * Renders the compass overlay on the locator bar position.
     * This is completely independent of the XP bar visibility.
     */
    @Inject(method = "*", at = @At("RETURN"))
    private void locatorHeads$renderCompass(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (LocatorHeads.CONFIG == null || !LocatorHeads.CONFIG.enableMod || !LocatorHeads.CONFIG.showCompass) {
            return;
        }
        
        // Don't render if GUI is hidden (F1) or if a screen is open (ESC menu, etc.)
        if (this.minecraft.options.hideGui || this.minecraft.screen != null) {
            return;
        }
        
        if (this.minecraft.getCameraEntity() == null) {
            return;
        }
        
        // Get player rotation (yaw)
        float yaw = this.minecraft.getCameraEntity().getYRot();
        
        // Calculate screen dimensions and position directly on the XP bar
        int screenWidth = this.minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = this.minecraft.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int compassY = screenHeight - 31; // Position on top of the XP bar itself
        
        // Draw cardinal directions
        locatorHeads$drawCardinalDirection(guiGraphics, centerX, compassY, yaw, "S", 0);
        locatorHeads$drawCardinalDirection(guiGraphics, centerX, compassY, yaw, "W", 90);
        locatorHeads$drawCardinalDirection(guiGraphics, centerX, compassY, yaw, "N", 180);
        locatorHeads$drawCardinalDirection(guiGraphics, centerX, compassY, yaw, "E", 270);
    }
    
    private void locatorHeads$drawCardinalDirection(GuiGraphics guiGraphics, int centerX, int compassY, float playerYaw, String direction, float directionAngle) {
        // Normalize angles
        float normalizedYaw = ((playerYaw % 360) + 360) % 360;
        float angleDiff = directionAngle - normalizedYaw;
        
        // Normalize angle difference to -180 to 180 range
        while (angleDiff > 180) angleDiff -= 360;
        while (angleDiff < -180) angleDiff += 360;
        
        // Only show directions within 100 degrees of view (increased from 90 for better visibility)
        if (Math.abs(angleDiff) > 100) {
            return;
        }
        
        // Calculate X position based on angle difference
        // Reduced multiplier to keep within XP bar bounds (182 pixels / 2 = 91 max offset)
        int offset = (int)(angleDiff * 0.9f); // Reduced from 2.0f to keep within bounds
        int x = centerX + offset;
        
        // Clamp to XP bar width (182 pixels wide, centered)
        int xpBarHalfWidth = 91; // 182 / 2
        int minX = centerX - xpBarHalfWidth;
        int maxX = centerX + xpBarHalfWidth;
        x = Math.max(minX, Math.min(maxX, x));
        
        // Calculate opacity based on distance from center (fade at edges)
        float centerDistance = Math.abs(angleDiff) / 90.0f;
        int alpha = (int)((1.0f - centerDistance * 0.5f) * 255);
        
        // Get compass color from config (includes alpha from the color picker)
        int compassColorRGB = LocatorHeads.CONFIG.compassColor & 0xFFFFFF;
        int color = (alpha << 24) | compassColorRGB;
        
        // Draw the direction letter with optional black outline
        int textX = x - 2;
        // Draw black outline if enabled
        if (LocatorHeads.CONFIG.compassShadow) {
            guiGraphics.drawString(this.minecraft.font, direction, textX - 1, compassY, (alpha << 24), true);
            guiGraphics.drawString(this.minecraft.font, direction, textX + 1, compassY, (alpha << 24), true);
            guiGraphics.drawString(this.minecraft.font, direction, textX, compassY - 1, (alpha << 24), true);
            guiGraphics.drawString(this.minecraft.font, direction, textX, compassY + 1, (alpha << 24), true);
        }
        // Draw colored letter on top
        guiGraphics.drawString(this.minecraft.font, direction, textX, compassY, color, true);
    }
}
