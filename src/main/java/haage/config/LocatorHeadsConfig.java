package haage.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "locator-heads")
public class LocatorHeadsConfig implements ConfigData {

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean enableMod = true;

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean renderHeads = true;

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean enableTeamBorder = false;

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip(count = 1)
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public BorderThickness teamBorderThickness = BorderThickness.NORMAL;

    @ConfigEntry.Category("general")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 9)
    public int headSizeMultiplier = 5; // 5 = 1.0x, 1 = 0.5x, 9 = 1.5x

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip(count = 1)
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public NameDisplayMode showPlayerNames = NameDisplayMode.OFF;

    @ConfigEntry.Category("playerFiltering")
    @ConfigEntry.Gui.Tooltip(count = 1)
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public PlayerFilterMode playerFilterMode = PlayerFilterMode.ALL;

    @ConfigEntry.Category("playerFiltering")
    @ConfigEntry.Gui.Tooltip(count = 1)
    public String includedPlayers = "";

    @ConfigEntry.Category("playerFiltering")
    @ConfigEntry.Gui.Tooltip(count = 1)
    public String excludedPlayers = "";

    // Helper method to get the actual multiplier value
    public double getHeadSizeMultiplier() {
        // Add null check with fallback to default value
        if (headSizeMultiplier == 0) {
            return 1.0; // Default to normal size if not initialized
        }
        // Convert 1-9 range to 0.5-1.5 range with 5 = 1.0x
        // 1 = 0.5x, 2 = 0.6x, 3 = 0.7x, 4 = 0.8x, 5 = 1.0x, 6 = 1.1x, 7 = 1.2x, 8 = 1.3x, 9 = 1.5x
        switch (headSizeMultiplier) {
            case 1: return 0.5;
            case 2: return 0.6;
            case 3: return 0.7;
            case 4: return 0.8;
            case 5: return 1.0; // Default
            case 6: return 1.1;
            case 7: return 1.2;
            case 8: return 1.3;
            case 9: return 1.5;
            default: return 1.0; // Fallback
        }
    }

    // Method to get display text for the slider
    public String getHeadSizeDisplayText() {
        return String.format("%.1fx", getHeadSizeMultiplier());
    }

    public enum BorderThickness {
        THIN(0.3),
        MEDIUM(0.5),
        NORMAL(1.0),
        THICK(2.0);

        private final double value;

        BorderThickness(double value) {
            this.value = value;
        }

        public double getValue() {
            return value;
        }
    }

    public enum PlayerFilterMode {
        ALL,        // Show all players
        INCLUDE,    // Only show players in the include list
        EXCLUDE     // Show all players except those in the exclude list
    }

    public enum NameDisplayMode {
        OFF,        // Never show names
        ALWAYS,     // Always show names when heads are visible
        LOOKING_AT, // Only show names when looking at the player
        PLAYER_LIST // Only show names when viewing the player list (Tab key)
    }
}
