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

    @Inject(method = "method_70870", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIIII)V", shift = At.Shift.BEFORE))
    private void locatorHeads$captureWaypointForSkinRender(Level level, GuiGraphics guiGraphics, int i, TrackedWaypoint trackedWaypoint, CallbackInfo ci) {
        // Check if config is loaded and mod is enabled
        if (LocatorHeads.CONFIG == null || !LocatorHeads.CONFIG.enableMod) {
            return;
        }

        this.locatorHeads$currentWaypoint = trackedWaypoint;
        this.locatorHeads$skinOverride = null;
        this.locatorHeads$teamColor = 0xFFFFFF; // Default white
        this.locatorHeads$playerName = null;

        // Check if this waypoint represents a player
        var connection = Minecraft.getInstance().getConnection();
        if (connection != null && trackedWaypoint.id().left().isPresent()) {
            var playerInfo = connection.getPlayerInfo(trackedWaypoint.id().left().get());
            if (playerInfo != null) {
                String playerName = playerInfo.getProfile().getName();

                // Check player filtering rules
                if (!locatorHeads$shouldShowPlayerHead(playerName)) {
                    return; // Don't show head for this player
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

        // ===== PLAYER NAME RENDERING =====
        // Render player name if enabled and we have a name
        if (locatorHeads$shouldShowPlayerName() && this.locatorHeads$playerName != null) {
            guiGraphics.pose().popMatrix(); // Exit the 0.01 scale for text rendering

            // Calculate text position (above the head)
            int textX = x + (width / 2);
            int textY = y - 12; // 12 pixels above the head

            // Get text width for centering
            int textWidth = this.minecraft.font.width(this.locatorHeads$playerName);
            textX -= textWidth / 2; // Center the text

            // Render text with 75% opacity (0xBF alpha) and shadow for better visibility
            int textColor = 0xBFFFFFFF; // 75% opacity white
            guiGraphics.drawString(this.minecraft.font, this.locatorHeads$playerName, textX, textY, textColor, true);

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
}
