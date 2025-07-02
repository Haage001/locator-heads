package haage.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "locator-heads")
public class LocatorHeadsConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean enableMod = true;

    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean enableTeamBorder = true;

    @ConfigEntry.Gui.Tooltip(count = 1)
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public BorderThickness teamBorderThickness = BorderThickness.NORMAL;

    @ConfigEntry.Gui.Tooltip(count = 1)
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public NameDisplayMode showPlayerNames = NameDisplayMode.OFF;

    @ConfigEntry.Gui.Tooltip(count = 1)
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public PlayerFilterMode playerFilterMode = PlayerFilterMode.ALL;

    @ConfigEntry.Gui.Tooltip(count = 1)
    public String includedPlayers = "";

    @ConfigEntry.Gui.Tooltip(count = 1)
    public String excludedPlayers = "";

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
        LOOKING_AT  // Only show names when looking at the player
    }
}
