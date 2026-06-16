package haage.mixin;

import haage.LocatorHeads;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudMixin {
    private static final float COMPASS_TOTAL_FOV_DEGREES = 120.0f;

    @Shadow
    @Final
    private Minecraft minecraft;

    /**
     * Renders the compass overlay on the locator bar position.
     * Injects into the main render method to draw once per frame.
     */
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void locatorHeads$renderCompass(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (LocatorHeads.CONFIG == null || !LocatorHeads.CONFIG.enableMod || !LocatorHeads.CONFIG.showCompass) {
            return;
        }

        // Don't render if GUI is hidden (F1) or if a screen is open (ESC menu, etc.), except for chat and inventory
        if (this.minecraft.gui.hud.isHidden() || (this.minecraft.gui.screen() != null &&
                !(this.minecraft.gui.screen() instanceof ChatScreen) &&
                !(this.minecraft.gui.screen() instanceof AbstractContainerScreen))) {
            return;
        }

        // Don't render if in Flashback replay mode
        if (locatorHeads$isInFlashbackReplay()) {
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

        // Draw cardinal directions or coordinate notation based on config
        if (LocatorHeads.CONFIG.useCoordinatesNotation) {
            locatorHeads$drawCardinalDirection(guiGraphics, centerX, compassY, yaw, "+Z", 0);
            locatorHeads$drawCardinalDirection(guiGraphics, centerX, compassY, yaw, "-X", 90);
            locatorHeads$drawCardinalDirection(guiGraphics, centerX, compassY, yaw, "-Z", 180);
            locatorHeads$drawCardinalDirection(guiGraphics, centerX, compassY, yaw, "+X", 270);
        } else {
            locatorHeads$drawCardinalDirection(guiGraphics, centerX, compassY, yaw, "S", 0);
            locatorHeads$drawCardinalDirection(guiGraphics, centerX, compassY, yaw, "W", 90);
            locatorHeads$drawCardinalDirection(guiGraphics, centerX, compassY, yaw, "N", 180);
            locatorHeads$drawCardinalDirection(guiGraphics, centerX, compassY, yaw, "E", 270);
        }
    }

    private void locatorHeads$drawCardinalDirection(GuiGraphicsExtractor guiGraphics, int centerX, int compassY, float playerYaw, String direction, float directionAngle) {
        // Normalize angles
        float normalizedYaw = ((playerYaw % 360) + 360) % 360;
        float angleDiff = directionAngle - normalizedYaw;

        // Normalize angle difference to -180 to 180 range
        while (angleDiff > 180) angleDiff -= 360;
        while (angleDiff < -180) angleDiff += 360;

        float halfFov = COMPASS_TOTAL_FOV_DEGREES / 2.0f;

        // 120 total FOV means +/-60 from center.
        if (Math.abs(angleDiff) > halfFov) {
            return;
        }

        // Map angle directly into XP bar width (182 pixels wide, centered).
        int xpBarHalfWidth = 91; // 182 / 2
        int offset = (int)((angleDiff / halfFov) * xpBarHalfWidth);
        int x = centerX + offset;

        // Calculate opacity based on distance from center (fade at edges)
        float centerDistance = Math.abs(angleDiff) / halfFov;
        int alpha = (int)((1.0f - centerDistance * 0.5f) * 255);

        // Get compass color from config (includes alpha from the color picker)
        int compassColorRGB = LocatorHeads.CONFIG.compassColor & 0xFFFFFF;
        int color = (alpha << 24) | compassColorRGB;

        // Draw the direction letter with optional black outline
        int textX = x - 2;
        if (LocatorHeads.CONFIG.compassShadow) {
            int shadowColor = (alpha << 24); // Black with same alpha as letter
            guiGraphics.text(this.minecraft.font, direction, textX - 1, compassY, shadowColor, false);
            guiGraphics.text(this.minecraft.font, direction, textX + 1, compassY, shadowColor, false);
            guiGraphics.text(this.minecraft.font, direction, textX, compassY - 1, shadowColor, false);
            guiGraphics.text(this.minecraft.font, direction, textX, compassY + 1, shadowColor, false);
        }
        // Draw colored letter on top
        guiGraphics.text(this.minecraft.font, direction, textX, compassY, color, false);
    }

    /**
     * Checks if the player is currently in a Flashback replay.
     * Uses reflection to avoid hard dependency on Flashback mod.
     */
    private boolean locatorHeads$isInFlashbackReplay() {
        try {
            Class<?> flashbackClass = Class.forName("com.moulberry.flashback.Flashback");
            Object isReplaying = flashbackClass.getMethod("isInReplay").invoke(null);
            return (boolean) isReplaying;
        } catch (Exception e) {
            return false;
        }
    }
}
