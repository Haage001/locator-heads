package haage.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import haage.LocatorHeads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import net.minecraft.client.resources.WaypointStyle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.Waypoint;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocatorBarRenderer.class)
public class LocatorBarRendererMixin {
    @Shadow @Final private Minecraft minecraft;
    @Unique private ResourceLocation locatorHeads$skinOverride;
    @Unique private TrackedWaypoint locatorHeads$currentWaypoint;
    @Unique private int locatorHeads$teamColor = 0xFFFFFF; // Default white
    @Unique private String locatorHeads$playerName = null;
    @Unique private boolean locatorHeads$shouldHideWaypoint = false; // New flag to track if waypoint should be completely hidden
    @Unique private static final java.util.Map<String, Long> locatorHeads$nameAnimationStartTimes = new java.util.HashMap<>();
    @Unique private static final java.util.Map<String, Boolean> locatorHeads$nameAnimationDirection = new java.util.HashMap<>(); // true = fade in, false = fade out
    @Unique private static final int locatorHeads$ANIMATION_DURATION_MS = 150; // Fast animation - 150ms

    @Inject(method = "method_70870", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIIII)V", shift = At.Shift.BEFORE))
    private void locatorHeads$captureWaypointForSkinRender(Level level, GuiGraphics guiGraphics, int i, TrackedWaypoint trackedWaypoint, CallbackInfo ci) {
        // Check if config is loaded and mod is enabled
        if (LocatorHeads.CONFIG == null || !LocatorHeads.CONFIG.enableMod) {
            this.locatorHeads$shouldHideWaypoint = false;
            return;
        }

        this.locatorHeads$currentWaypoint = trackedWaypoint;
        this.locatorHeads$skinOverride = null;
        this.locatorHeads$teamColor = 0xFFFFFF; // Default white
        this.locatorHeads$playerName = null;
        this.locatorHeads$shouldHideWaypoint = false; // Reset flag

        // Check if this waypoint represents a player
        var connection = Minecraft.getInstance().getConnection();
        if (connection != null && trackedWaypoint.id().left().isPresent()) {
            var playerInfo = connection.getPlayerInfo(trackedWaypoint.id().left().get());
            if (playerInfo != null) {
                String playerName = playerInfo.getProfile().getName();

                // Check player filtering rules - if player should be filtered out, hide the entire waypoint
                if (!locatorHeads$shouldShowPlayerHead(playerName)) {
                    this.locatorHeads$shouldHideWaypoint = true;
                    return; // Don't show anything for this player
                }

                // Store player name for rendering
                this.locatorHeads$playerName = playerName;

                // Get the player's skin texture
                this.locatorHeads$skinOverride = Minecraft.getInstance().getSkinManager().getInsecureSkin(playerInfo.getProfile()).texture();

                // Get player's team color
                if (level != null && level.getScoreboard() != null) {
                    PlayerTeam team = level.getScoreboard().getPlayersTeam(playerInfo.getProfile().getName());
                    if (team != null) {
                        this.locatorHeads$teamColor = team.getColor().getColor() != null ? team.getColor().getColor() : 0xFFFFFF;
                    }
                }
            }
        }
    }

    @Redirect(method = "method_70870", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIIII)V"))
    private void locatorHeads$renderPlayerSkinInsteadOfIcon(GuiGraphics guiGraphics, RenderPipeline renderPipeline, ResourceLocation originalIcon, int x, int y, int width, int height, int depth) {
        // If player should be hidden, don't render anything at all
        if (this.locatorHeads$shouldHideWaypoint) {
            return; // Skip rendering entirely - no head, no vanilla icon
        }

        if (this.locatorHeads$skinOverride == null || LocatorHeads.CONFIG == null || !LocatorHeads.CONFIG.enableMod) {
            // No skin override, config not loaded, or mod disabled - render original icon
            guiGraphics.blitSprite(renderPipeline, originalIcon, x, y, width, height, depth);
        } else {
            // Render player head with configurable team-colored border
            this.locatorHeads$renderPlayerHead(guiGraphics, x, y, width, height);
        }
        // Clear the override after use
        this.locatorHeads$skinOverride = null;
    }

    @Unique
    private void locatorHeads$renderPlayerHead(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Safety check - ensure config is loaded
        if (LocatorHeads.CONFIG == null || LocatorHeads.CONFIG.teamBorderThickness == null) {
            return;
        }

        // Calculate distance-based scaling like the original waypoint system
        float distance = Mth.sqrt((float)this.locatorHeads$currentWaypoint.distanceSquared(this.minecraft.cameraEntity));
        Waypoint.Icon icon = this.locatorHeads$currentWaypoint.icon();
        WaypointStyle style = this.minecraft.getWaypointStyles().get(icon.style);
        float progress = 1 - Mth.clamp((distance - style.nearDistance()) / (style.farDistance() - style.nearDistance()), 0, 1);

        // Apply distance-based scaling
        int scaledWidth = (int)(Mth.lerpInt(progress, 4 * 100 + 100, width * 100));
        int scaledHeight = (int)(Mth.lerpInt(progress, 4 * 100 + 100, height * 100));

        // Apply user-configured size multiplier (maintaining the same ratio between close and far players)
        double sizeMultiplier = LocatorHeads.CONFIG.getHeadSizeMultiplier();
        scaledWidth = (int)(scaledWidth * sizeMultiplier);
        scaledHeight = (int)(scaledHeight * sizeMultiplier);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(0.01f);

        // Calculate centered position
        int centerX = (x * 100 + 450) - (scaledWidth / 2);
        int centerY = (y * 100 + 450) - (scaledHeight / 2);

        // ===== CONFIGURABLE TEAM COLORED BORDER =====
        // Check config settings for border rendering
        boolean shouldRenderBorder = false;
        int borderColor = 0xFFFFFF;

        if (this.locatorHeads$teamColor != 0xFFFFFF && LocatorHeads.CONFIG.enableTeamBorder) {
            // Player has a team and team borders are enabled
            shouldRenderBorder = true;
            borderColor = this.locatorHeads$teamColor;
        }

        if (shouldRenderBorder && LocatorHeads.CONFIG.teamBorderThickness != null && LocatorHeads.CONFIG.teamBorderThickness.getValue() > 0) {
            // Get border thickness from config (convert to scaled pixels)
            int borderThickness = (int)(LocatorHeads.CONFIG.teamBorderThickness.getValue() * 100); // Convert to 0.01 scale
            borderColor = borderColor | 0xFF000000; // Add full alpha

            // Render configurable team-colored border around the head
            // Top border
            guiGraphics.fill(centerX - borderThickness, centerY - borderThickness,
                            centerX + scaledWidth + borderThickness, centerY, borderColor);
            // Bottom border
            guiGraphics.fill(centerX - borderThickness, centerY + scaledHeight,
                            centerX + scaledWidth + borderThickness, centerY + scaledHeight + borderThickness, borderColor);
            // Left border
            guiGraphics.fill(centerX - borderThickness, centerY,
                            centerX, centerY + scaledHeight, borderColor);
            // Right border
            guiGraphics.fill(centerX + scaledWidth, centerY,
                            centerX + scaledWidth + borderThickness, centerY + scaledHeight, borderColor);
        }

        // Render the base skin layer (face)
        guiGraphics.blit(this.locatorHeads$skinOverride,
            centerX, centerY,
            centerX + scaledWidth, centerY + scaledHeight,
            1f/8, 2f/8, 1f/8, 2f/8);

        // Render the hat/overlay layer
        guiGraphics.blit(this.locatorHeads$skinOverride,
            centerX, centerY,
            centerX + scaledWidth, centerY + scaledHeight,
            5f/8, 6f/8, 1f/8, 2f/8);

        // ===== PLAYER NAME RENDERING WITH BIDIRECTIONAL ANIMATION =====
        boolean shouldShowName = locatorHeads$shouldShowPlayerName();
        boolean isAnimating = locatorHeads$nameAnimationStartTimes.containsKey(this.locatorHeads$playerName);

        // Handle animation state changes
        if (shouldShowName && !isAnimating) {
            // Start fade-in animation
            long currentTime = System.currentTimeMillis();
            locatorHeads$nameAnimationStartTimes.put(this.locatorHeads$playerName, currentTime);
            locatorHeads$nameAnimationDirection.put(this.locatorHeads$playerName, true); // fade in
        } else if (!shouldShowName && isAnimating) {
            // Check if we're currently fading in - if so, start fade-out from current position
            Boolean currentDirection = locatorHeads$nameAnimationDirection.get(this.locatorHeads$playerName);
            if (currentDirection != null && currentDirection) {
                // We were fading in, now start fading out
                long currentTime = System.currentTimeMillis();
                Long startTime = locatorHeads$nameAnimationStartTimes.get(this.locatorHeads$playerName);
                if (startTime != null) {
                    long elapsedTime = currentTime - startTime;
                    float fadeInProgress = Math.min(1.0f, (float)elapsedTime / locatorHeads$ANIMATION_DURATION_MS);

                    // Start fade-out from the current position
                    long newStartTime = currentTime - (long)((1.0f - fadeInProgress) * locatorHeads$ANIMATION_DURATION_MS);
                    locatorHeads$nameAnimationStartTimes.put(this.locatorHeads$playerName, newStartTime);
                    locatorHeads$nameAnimationDirection.put(this.locatorHeads$playerName, false); // fade out
                }
            }
        }

        // Render name with animation (fade in, fade out, or static)
        if ((shouldShowName || isAnimating) && this.locatorHeads$playerName != null) {
            guiGraphics.pose().popMatrix(); // Exit the 0.01 scale for text rendering

            long currentTime = System.currentTimeMillis();
            long animationStartTime = locatorHeads$nameAnimationStartTimes.get(this.locatorHeads$playerName);
            long elapsedTime = currentTime - animationStartTime;
            float animationProgress = Math.min(1.0f, (float)elapsedTime / locatorHeads$ANIMATION_DURATION_MS);

            Boolean isFadingIn = locatorHeads$nameAnimationDirection.get(this.locatorHeads$playerName);
            if (isFadingIn == null) isFadingIn = true; // Default to fade in

            // Calculate progress based on direction
            float effectiveProgress;
            if (isFadingIn) {
                effectiveProgress = animationProgress; // 0 to 1 (fade in)
            } else {
                effectiveProgress = 1.0f - animationProgress; // 1 to 0 (fade out)
            }

            // Smooth easing function (ease-out cubic)
            float easedProgress = 1.0f - (float)Math.pow(1.0f - effectiveProgress, 3.0f);

            // Calculate text position (above the head) - adjust for scaled head size
            int textX = x + (width / 2);
            int baseTextY = y - (int)(12 * sizeMultiplier); // Base position above head

            // Animation: slide up/down based on progress
            int animationOffset = (int)(20 * (1.0f - easedProgress));
            int textY = baseTextY + animationOffset;

            // Get text width for centering
            int textWidth = this.minecraft.font.width(this.locatorHeads$playerName);
            textX -= textWidth / 2; // Center the text

            // Fade in/out effect
            int baseAlpha = 0xBF; // 75% opacity when fully visible
            int animatedAlpha = (int)(baseAlpha * easedProgress);

            // Use team color for text if team borders are enabled and player has a team
            int textColor;
            if (LocatorHeads.CONFIG.enableTeamBorder && this.locatorHeads$teamColor != 0xFFFFFF) {
                // Use team color with animated alpha
                int teamColorRGB = this.locatorHeads$teamColor & 0xFFFFFF; // Remove any existing alpha
                textColor = (animatedAlpha << 24) | teamColorRGB;
            } else {
                // Use default white color with animated alpha
                textColor = (animatedAlpha << 24) | 0xFFFFFF;
            }

            // Only render if there's some opacity
            if (animatedAlpha > 0) {
                guiGraphics.drawString(this.minecraft.font, this.locatorHeads$playerName, textX, textY, textColor, true);
            }

            // Clean up completed fade-out animations
            if (!isFadingIn && animationProgress >= 1.0f) {
                locatorHeads$nameAnimationStartTimes.remove(this.locatorHeads$playerName);
                locatorHeads$nameAnimationDirection.remove(this.locatorHeads$playerName);
            }

            guiGraphics.pose().pushMatrix(); // Re-enter the scale for cleanup
            guiGraphics.pose().scale(0.01f);
        }

        guiGraphics.pose().popMatrix();
    }

    @Unique
    private boolean locatorHeads$shouldShowPlayerHead(String playerName) {
        if (LocatorHeads.CONFIG == null || LocatorHeads.CONFIG.playerFilterMode == null) {
            return true; // Default to showing all players if config not loaded
        }

        switch (LocatorHeads.CONFIG.playerFilterMode) {
            case ALL:
                return true; // Show all players
            case INCLUDE:
                // Only show players in the include list
                return LocatorHeads.CONFIG.includedPlayers != null &&
                       locatorHeads$isPlayerInList(playerName, LocatorHeads.CONFIG.includedPlayers);
            case EXCLUDE:
                // Show all players except those in the exclude list
                return LocatorHeads.CONFIG.excludedPlayers == null ||
                       !locatorHeads$isPlayerInList(playerName, LocatorHeads.CONFIG.excludedPlayers);
            default:
                return true;
        }
    }

    @Unique
    private boolean locatorHeads$isPlayerInList(String playerName, String playerList) {
        if (playerList == null || playerList.trim().isEmpty()) {
            return false;
        }

        // Split by comma, semicolon, colon, or period and check each name
        String[] players = playerList.split("[,;:.]");
        for (String player : players) {
            if (player.trim().equalsIgnoreCase(playerName)) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private boolean locatorHeads$shouldShowPlayerName() {
        if (LocatorHeads.CONFIG == null || LocatorHeads.CONFIG.showPlayerNames == null) {
            return false;
        }

        switch (LocatorHeads.CONFIG.showPlayerNames) {
            case OFF:
                return false;
            case ALWAYS:
                return true;
            case LOOKING_AT:
                return locatorHeads$isLookingAtPlayer();
            case PLAYER_LIST:
                return locatorHeads$isPlayerListOpen();
            default:
                return false;
        }
    }

    @Unique
    private boolean locatorHeads$isLookingAtPlayer() {
        if (this.locatorHeads$currentWaypoint == null || this.minecraft.cameraEntity == null) {
            return false;
        }

        // Get the player entity from the world using the waypoint's player ID
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null || !this.locatorHeads$currentWaypoint.id().left().isPresent()) {
            return false;
        }

        var playerInfo = connection.getPlayerInfo(this.locatorHeads$currentWaypoint.id().left().get());
        if (playerInfo == null) {
            return false;
        }

        // Try to find the actual player entity in the world
        var level = this.minecraft.level;
        if (level == null) {
            return false;
        }

        var targetPlayer = level.getPlayerByUUID(playerInfo.getProfile().getId());
        if (targetPlayer == null) {
            return false;
        }

        // Get our camera position and look direction
        var cameraPos = this.minecraft.cameraEntity.position();
        var lookVec = this.minecraft.cameraEntity.getViewVector(1.0f);

        // Get target player position
        var targetPos = targetPlayer.position();

        // Calculate direction vector from camera to target player
        var directionToPlayer = targetPos.subtract(cameraPos);

        // Normalize the direction vector
        var normalizedDirection = directionToPlayer.normalize();

        // Calculate the dot product between look direction and direction to player
        double dotProduct = lookVec.dot(normalizedDirection);

        // Convert 20 degrees to radians: cos(20°)
        // This means if the angle between look direction and player direction is within 30 degrees
        double angleThreshold = Math.cos(Math.toRadians(20));

        return dotProduct >= angleThreshold;
    }

    @Unique
    private boolean locatorHeads$isPlayerListOpen() {
        // Check if the player list (Tab overlay) is currently being displayed
        return this.minecraft.options.keyPlayerList.isDown();
    }
}
