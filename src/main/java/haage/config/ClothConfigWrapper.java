package haage.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * Cloth Config wrapper that acts as a bridge for GUI configuration.
 * This class is only loaded when Cloth Config is present.
 * Contains all config fields with Cloth Config annotations.
 */
@Config(name = "locator-heads")
public class ClothConfigWrapper implements ConfigData {

    // Transient reference to the actual config - not serialized, excluded from GUI
    @ConfigEntry.Gui.Excluded
    private transient LocatorHeadsConfig actualConfig;
    
    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean enableMod = true;

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean renderHeads = true;

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean alwaysShowXP = false;

    @ConfigEntry.Category("compass")
    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean showCompass = false;

    @ConfigEntry.Category("compass")
    @ConfigEntry.Gui.Tooltip(count = 1)
    @ConfigEntry.ColorPicker
    public int compassColor = 0xFFFFFF;

    @ConfigEntry.Category("compass")
    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean compassShadow = true;

    @ConfigEntry.Category("borders")
    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean enableTeamBorder = false;

    @ConfigEntry.Category("borders")
    @ConfigEntry.Gui.Tooltip(count = 1)
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public LocatorHeadsConfig.BorderThickness teamBorderThickness = LocatorHeadsConfig.BorderThickness.NORMAL;

    @ConfigEntry.Category("borders")
    @ConfigEntry.Gui.Tooltip(count = 1)
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public LocatorHeadsConfig.BorderStyle borderStyle = LocatorHeadsConfig.BorderStyle.TEAM_COLOR;

    @ConfigEntry.Category("borders")
    @ConfigEntry.Gui.Tooltip(count = 1)
    @ConfigEntry.ColorPicker
    public int staticBorderColor = 0xFFFFFF;

    @ConfigEntry.Category("general")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 9)
    @ConfigEntry.Gui.Excluded
    public int headSizeMultiplier = 5;

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip(count = 1)
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public LocatorHeadsConfig.NameDisplayMode showPlayerNames = LocatorHeadsConfig.NameDisplayMode.OFF;

    @ConfigEntry.Category("playerFiltering")
    @ConfigEntry.Gui.Tooltip(count = 1)
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public LocatorHeadsConfig.PlayerFilterMode playerFilterMode = LocatorHeadsConfig.PlayerFilterMode.ALL;

    @ConfigEntry.Category("playerFiltering")
    @ConfigEntry.Gui.Tooltip(count = 1)
    public String includedPlayers = "";

    @ConfigEntry.Category("playerFiltering")
    @ConfigEntry.Gui.Tooltip(count = 1)
    public String excludedPlayers = "";
    
    /**
     * Links this wrapper to the actual config instance that will be used by the mod.
     * Called after AutoConfig loads this wrapper.
     */
    public void linkToConfig(LocatorHeadsConfig config) {
        this.actualConfig = config;
        // Copy initial values from wrapper to actual config
        syncToActualConfig();
    }
    
    /**
     * Syncs values from this wrapper to the actual config.
     * Called when config is saved via GUI.
     */
    public void syncToActualConfig() {
        if (actualConfig != null) {
            actualConfig.enableMod = this.enableMod;
            actualConfig.renderHeads = this.renderHeads;
            actualConfig.alwaysShowXP = this.alwaysShowXP;
            actualConfig.showCompass = this.showCompass;
            actualConfig.compassColor = this.compassColor;
            actualConfig.compassShadow = this.compassShadow;
            actualConfig.enableTeamBorder = this.enableTeamBorder;
            actualConfig.teamBorderThickness = this.teamBorderThickness;
            actualConfig.borderStyle = this.borderStyle;
            actualConfig.staticBorderColor = this.staticBorderColor;
            actualConfig.headSizeMultiplier = this.headSizeMultiplier;
            actualConfig.showPlayerNames = this.showPlayerNames;
            actualConfig.playerFilterMode = this.playerFilterMode;
            actualConfig.includedPlayers = this.includedPlayers;
            actualConfig.excludedPlayers = this.excludedPlayers;
        }
    }
}
