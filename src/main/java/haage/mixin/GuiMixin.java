package haage.mixin;

import haage.LocatorHeads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
        if (LocatorHeads.CONFIG != null && LocatorHeads.CONFIG.alwaysShowXP
                && this.minecraft.player != null
                && !this.minecraft.player.isPassenger()) {
            cir.setReturnValue(true);
        }
    }
}
