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
    @Unique private boolean locatorHeads$shouldHideWaypoint = false; // Hide entire waypoint
    @Unique private static final java.util.Map<String, Long> locatorHeads$nameAnimationStartTimes = new java.util.HashMap<>();
    @Unique private static final java.util.Map<String, Boolean> locatorHeads$nameAnimationDirection = new java.util.HashMap<>(); // true = fade in
    @Unique private static final int locatorHeads$ANIMATION_DURATION_MS = 150; // ms

    @Inject(method = "method_70870", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIIII)V", shift = At.Shift.BEFORE))
    private void locatorHeads$captureWaypointForSkinRender(Level level, GuiGraphics guiGraphics, int i, TrackedWaypoint trackedWaypoint, CallbackInfo ci) {
        if (LocatorHeads.CONFIG == null || !LocatorHeads.CONFIG.enableMod) {
            this.locatorHeads$shouldHideWaypoint = false;
            return;
        }
        this.locatorHeads$currentWaypoint = trackedWaypoint;
        this.locatorHeads$skinOverride = null;
        this.locatorHeads$teamColor = 0xFFFFFF;
        this.locatorHeads$playerName = null;
        this.locatorHeads$shouldHideWaypoint = false;

        var connection = Minecraft.getInstance().getConnection();
        if (connection != null && trackedWaypoint.id().left().isPresent()) {
            var playerInfo = connection.getPlayerInfo(trackedWaypoint.id().left().get());
            if (playerInfo != null) {
                String playerName = playerInfo.getProfile().getName();
                if (!locatorHeads$shouldShowPlayerHead(playerName)) { // filter hides completely
                    this.locatorHeads$shouldHideWaypoint = true;
                    return;
                }
                this.locatorHeads$playerName = playerName;
                this.locatorHeads$skinOverride = Minecraft.getInstance().getSkinManager().getInsecureSkin(playerInfo.getProfile()).texture();
                if (level != null) {
                    PlayerTeam team = level.getScoreboard().getPlayersTeam(playerInfo.getProfile().getName());
                    if (team != null && team.getColor().getColor() != null) {
                        this.locatorHeads$teamColor = team.getColor().getColor();
                    }
                }
            }
        }
    }

    @Redirect(method = "method_70870", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIIII)V"))
    private void locatorHeads$renderPlayerSkinInsteadOfIcon(GuiGraphics guiGraphics, RenderPipeline pipeline, ResourceLocation originalIcon, int x, int y, int w, int h, int d) {
        if (this.locatorHeads$shouldHideWaypoint) return;
        if (this.locatorHeads$skinOverride == null || LocatorHeads.CONFIG == null || !LocatorHeads.CONFIG.enableMod) {
            guiGraphics.blitSprite(pipeline, originalIcon, x, y, w, h, d);
        } else {
            locatorHeads$renderPlayerHead(guiGraphics, x, y, w, h);
        }
        this.locatorHeads$skinOverride = null;
    }

    @Unique
    private void locatorHeads$renderPlayerHead(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        if (LocatorHeads.CONFIG == null || LocatorHeads.CONFIG.teamBorderThickness == null) return;

        float distance = Mth.sqrt((float)this.locatorHeads$currentWaypoint.distanceSquared(this.minecraft.cameraEntity));
        Waypoint.Icon icon = this.locatorHeads$currentWaypoint.icon();
        WaypointStyle style = this.minecraft.getWaypointStyles().get(icon.style);
        float progress = 1 - Mth.clamp((distance - style.nearDistance()) / (style.farDistance() - style.nearDistance()), 0, 1);
        int scaledWidth = Mth.lerpInt(progress, 4 * 100 + 100, width * 100);
        int scaledHeight = Mth.lerpInt(progress, 4 * 100 + 100, height * 100);
        double sizeMultiplier = LocatorHeads.CONFIG.getHeadSizeMultiplier();
        scaledWidth = (int)(scaledWidth * sizeMultiplier);
        scaledHeight = (int)(scaledHeight * sizeMultiplier);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(0.01f);
        int centerX = (x * 100 + 450) - (scaledWidth / 2);
        int centerY = (y * 100 + 450) - (scaledHeight / 2);

        boolean shouldRenderBorder = false;
        int borderColor = 0xFFFFFF;
        boolean hasTeam = this.locatorHeads$teamColor != 0xFFFFFF; // heuristic: non-white set earlier only for team color
        if (LocatorHeads.CONFIG.renderHeads && LocatorHeads.CONFIG.enableTeamBorder) {
            if (LocatorHeads.CONFIG.borderStyle == haage.config.LocatorHeadsConfig.BorderStyle.TEAM_COLOR) {
                if (hasTeam) { // only if player actually has a team
                    shouldRenderBorder = true;
                    borderColor = this.locatorHeads$teamColor;
                }
            } else if (LocatorHeads.CONFIG.borderStyle == haage.config.LocatorHeadsConfig.BorderStyle.STATIC_COLOR) {
                shouldRenderBorder = true;
                borderColor = LocatorHeads.CONFIG.staticBorderColor.getColor();
            }
        }

        if (LocatorHeads.CONFIG.renderHeads) {
            if (shouldRenderBorder && LocatorHeads.CONFIG.teamBorderThickness.getValue() > 0) {
                int bt = (int)(LocatorHeads.CONFIG.teamBorderThickness.getValue() * 100);
                int drawColor = borderColor | 0xFF000000;
                guiGraphics.fill(centerX - bt, centerY - bt, centerX + scaledWidth + bt, centerY, drawColor);
                guiGraphics.fill(centerX - bt, centerY + scaledHeight, centerX + scaledWidth + bt, centerY + scaledHeight + bt, drawColor);
                guiGraphics.fill(centerX - bt, centerY, centerX, centerY + scaledHeight, drawColor);
                guiGraphics.fill(centerX + scaledWidth, centerY, centerX + scaledWidth + bt, centerY + scaledHeight, drawColor);
            }
            guiGraphics.blit(this.locatorHeads$skinOverride, centerX, centerY, centerX + scaledWidth, centerY + scaledHeight, 1f/8, 2f/8, 1f/8, 2f/8);
            guiGraphics.blit(this.locatorHeads$skinOverride, centerX, centerY, centerX + scaledWidth, centerY + scaledHeight, 5f/8, 6f/8, 1f/8, 2f/8);
        }

        // Name rendering with animation
        boolean shouldShowName = locatorHeads$shouldShowPlayerName();
        boolean isAnimating = locatorHeads$nameAnimationStartTimes.containsKey(this.locatorHeads$playerName);
        if (shouldShowName && !isAnimating && this.locatorHeads$playerName != null) {
            long now = System.currentTimeMillis();
            locatorHeads$nameAnimationStartTimes.put(this.locatorHeads$playerName, now);
            locatorHeads$nameAnimationDirection.put(this.locatorHeads$playerName, true);
        } else if (!shouldShowName && isAnimating) {
            Boolean dir = locatorHeads$nameAnimationDirection.get(this.locatorHeads$playerName);
            if (dir != null && dir) { // was fading in -> start fade out
                long now = System.currentTimeMillis();
                Long start = locatorHeads$nameAnimationStartTimes.get(this.locatorHeads$playerName);
                if (start != null) {
                    long elapsed = now - start;
                    float inProg = Math.min(1f, (float)elapsed / locatorHeads$ANIMATION_DURATION_MS);
                    long newStart = now - (long)((1f - inProg) * locatorHeads$ANIMATION_DURATION_MS);
                    locatorHeads$nameAnimationStartTimes.put(this.locatorHeads$playerName, newStart);
                    locatorHeads$nameAnimationDirection.put(this.locatorHeads$playerName, false);
                }
            }
        }

        if ((shouldShowName || isAnimating) && this.locatorHeads$playerName != null) {
            guiGraphics.pose().popMatrix(); // exit scale
            long now = System.currentTimeMillis();
            long start = locatorHeads$nameAnimationStartTimes.getOrDefault(this.locatorHeads$playerName, now);
            long elapsed = now - start;
            float animProg = Math.min(1f, (float)elapsed / locatorHeads$ANIMATION_DURATION_MS);
            Boolean dir = locatorHeads$nameAnimationDirection.get(this.locatorHeads$playerName);
            if (dir == null) dir = true;
            float eff = dir ? animProg : 1f - animProg;
            float eased = 1f - (float)Math.pow(1f - eff, 3f);

            int textX = x + (width / 2);
            int baseTextY = LocatorHeads.CONFIG.renderHeads ? y - (int)(12 * sizeMultiplier) : y + (height - this.minecraft.font.lineHeight) / 2;
            int textY = baseTextY + (int)(20 * (1f - eased));
            int textWidth = this.minecraft.font.width(this.locatorHeads$playerName);
            textX -= textWidth / 2;

            int alpha = (int)(0xBF * eased);
            int textColor;
            if (LocatorHeads.CONFIG.enableTeamBorder) {
                if (LocatorHeads.CONFIG.borderStyle == haage.config.LocatorHeadsConfig.BorderStyle.TEAM_COLOR) {
                    if (hasTeam) {
                        textColor = (alpha << 24) | (this.locatorHeads$teamColor & 0xFFFFFF);
                    } else {
                        textColor = (alpha << 24) | 0xFFFFFF; // no team -> default white, no tint
                    }
                } else if (LocatorHeads.CONFIG.borderStyle == haage.config.LocatorHeadsConfig.BorderStyle.STATIC_COLOR) {
                    textColor = (alpha << 24) | (LocatorHeads.CONFIG.staticBorderColor.getColor() & 0xFFFFFF);
                } else {
                    textColor = (alpha << 24) | 0xFFFFFF;
                }
            } else {
                textColor = (alpha << 24) | 0xFFFFFF;
            }

            if (alpha > 0) {
                guiGraphics.drawString(this.minecraft.font, this.locatorHeads$playerName, textX, textY, textColor, true);
            }
            if (!dir && animProg >= 1f) { // fade-out finished
                locatorHeads$nameAnimationStartTimes.remove(this.locatorHeads$playerName);
                locatorHeads$nameAnimationDirection.remove(this.locatorHeads$playerName);
            }
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().scale(0.01f); // restore for consistency
        }

        guiGraphics.pose().popMatrix();
    }

    @Unique
    private boolean locatorHeads$shouldShowPlayerHead(String playerName) {
        if (LocatorHeads.CONFIG == null || LocatorHeads.CONFIG.playerFilterMode == null) return true;
        switch (LocatorHeads.CONFIG.playerFilterMode) {
            case ALL: return true;
            case INCLUDE: return locatorHeads$isPlayerInList(playerName, LocatorHeads.CONFIG.includedPlayers);
            case EXCLUDE: return !locatorHeads$isPlayerInList(playerName, LocatorHeads.CONFIG.excludedPlayers);
            default: return true;
        }
    }

    @Unique
    private boolean locatorHeads$isPlayerInList(String playerName, String list) {
        if (list == null || list.trim().isEmpty()) return false;
        String[] players = list.split("[,;:.]");
        for (String p : players) if (p.trim().equalsIgnoreCase(playerName)) return true;
        return false;
    }

    @Unique
    private boolean locatorHeads$shouldShowPlayerName() {
        if (LocatorHeads.CONFIG == null || LocatorHeads.CONFIG.showPlayerNames == null) return false;
        switch (LocatorHeads.CONFIG.showPlayerNames) {
            case OFF: return false;
            case ALWAYS: return true;
            case LOOKING_AT: return locatorHeads$isLookingAtPlayer();
            case PLAYER_LIST: return locatorHeads$isPlayerListOpen();
            default: return false;
        }
    }

    @Unique
    private boolean locatorHeads$isLookingAtPlayer() {
        if (this.locatorHeads$currentWaypoint == null || this.minecraft.cameraEntity == null) return false;
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null || this.locatorHeads$currentWaypoint.id().left().isEmpty()) return false;
        var playerInfo = connection.getPlayerInfo(this.locatorHeads$currentWaypoint.id().left().get());
        if (playerInfo == null) return false;
        var level = this.minecraft.level; if (level == null) return false;
        var target = level.getPlayerByUUID(playerInfo.getProfile().getId()); if (target == null) return false;
        var cameraPos = this.minecraft.cameraEntity.position();
        var look = this.minecraft.cameraEntity.getViewVector(1.0f);
        var dir = target.position().subtract(cameraPos).normalize();
        double dot = look.dot(dir);
        double threshold = Math.cos(Math.toRadians(20));
        return dot >= threshold;
    }

    @Unique
    private boolean locatorHeads$isPlayerListOpen() {
        return this.minecraft.options.keyPlayerList.isDown();
    }
}
