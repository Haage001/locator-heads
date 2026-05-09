package haage.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import haage.LocatorHeads;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

public class LocatorHeadsModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            LocatorHeadsConfig current = LocatorHeads.CONFIG != null ? LocatorHeads.CONFIG : new LocatorHeadsConfig();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.translatable("title.locator-heads.config"));

            builder.setSavingRunnable(() -> {
                LocatorHeads.CONFIG = current;
                LocatorHeadsConfigManager.save(current, LocatorHeads.LOGGER);
            });

            ConfigEntryBuilder entries = builder.entryBuilder();

            ConfigCategory general = builder.getOrCreateCategory(Component.translatable("category.locator-heads.general"));
            general.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.enable_mod"), current.enableMod)
                    .setTooltip(tooltip("tooltip.locator-heads.enable_mod"))
                    .setDefaultValue(true)
                    .setSaveConsumer(value -> current.enableMod = value)
                    .build());
            general.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.render_heads"), current.renderHeads)
                    .setTooltip(tooltip("tooltip.locator-heads.render_heads"))
                    .setDefaultValue(true)
                    .setSaveConsumer(value -> current.renderHeads = value)
                    .build());
            general.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.always_show_xp"), current.alwaysShowXP)
                    .setTooltip(tooltip("tooltip.locator-heads.always_show_xp"))
                    .setDefaultValue(false)
                    .setSaveConsumer(value -> current.alwaysShowXP = value)
                    .build());
            general.addEntry(entries.startIntSlider(Component.translatable("option.locator-heads.head_size"), current.headSizeMultiplier, 1, 9)
                    .setTooltip(tooltip("tooltip.locator-heads.head_size"))
                    .setDefaultValue(5)
                    .setTextGetter(value -> Component.literal(String.format("%.1fx", convertHeadSize(value))))
                    .setSaveConsumer(value -> current.headSizeMultiplier = value)
                    .build());
            general.addEntry(entries.startEnumSelector(Component.translatable("option.locator-heads.show_player_names"), LocatorHeadsConfig.NameDisplayMode.class, current.showPlayerNames)
                    .setTooltip(tooltip("tooltip.locator-heads.show_player_names"))
                    .setDefaultValue(LocatorHeadsConfig.NameDisplayMode.OFF)
                    .setEnumNameProvider(mode -> Component.translatable("enum.locator-heads.name_display_mode." + mode.name().toLowerCase()))
                    .setSaveConsumer(value -> current.showPlayerNames = value)
                    .build());
            general.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.only_nearby_player_heads"), current.onlyShowNearbyPlayerHeads)
                    .setTooltip(tooltip("tooltip.locator-heads.only_nearby_player_heads"))
                    .setDefaultValue(false)
                    .setSaveConsumer(value -> current.onlyShowNearbyPlayerHeads = value)
                    .build());

            ConfigCategory compass = builder.getOrCreateCategory(Component.translatable("category.locator-heads.compass"));
            compass.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.show_compass"), current.showCompass)
                    .setTooltip(tooltip("tooltip.locator-heads.show_compass"))
                    .setDefaultValue(false)
                    .setSaveConsumer(value -> current.showCompass = value)
                    .build());
            compass.addEntry(entries.startColorField(Component.translatable("option.locator-heads.compass_color"), current.compassColor)
                    .setTooltip(tooltip("tooltip.locator-heads.compass_color"))
                    .setDefaultValue(0xFFFFFF)
                    .setSaveConsumer(value -> current.compassColor = value)
                    .build());
            compass.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.compass_shadow"), current.compassShadow)
                    .setTooltip(tooltip("tooltip.locator-heads.compass_shadow"))
                    .setDefaultValue(true)
                    .setSaveConsumer(value -> current.compassShadow = value)
                    .build());
            compass.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.coordinates_notation"), current.useCoordinatesNotation)
                    .setTooltip(tooltip("tooltip.locator-heads.coordinates_notation"))
                    .setDefaultValue(false)
                    .setSaveConsumer(value -> current.useCoordinatesNotation = value)
                    .build());

            ConfigCategory borders = builder.getOrCreateCategory(Component.translatable("category.locator-heads.borders"));
            borders.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.enable_team_border"), current.enableTeamBorder)
                    .setTooltip(tooltip("tooltip.locator-heads.enable_team_border"))
                    .setDefaultValue(false)
                    .setSaveConsumer(value -> current.enableTeamBorder = value)
                    .build());
            borders.addEntry(entries.startEnumSelector(Component.translatable("option.locator-heads.team_border_thickness"), LocatorHeadsConfig.BorderThickness.class, current.teamBorderThickness)
                    .setTooltip(tooltip("tooltip.locator-heads.team_border_thickness"))
                    .setDefaultValue(LocatorHeadsConfig.BorderThickness.NORMAL)
                    .setEnumNameProvider(thickness -> Component.translatable("enum.locator-heads.border_thickness." + thickness.name().toLowerCase()))
                    .setSaveConsumer(value -> current.teamBorderThickness = value)
                    .build());
            borders.addEntry(entries.startEnumSelector(Component.translatable("option.locator-heads.border_style"), LocatorHeadsConfig.BorderStyle.class, current.borderStyle)
                    .setTooltip(tooltip("tooltip.locator-heads.border_style"))
                    .setDefaultValue(LocatorHeadsConfig.BorderStyle.TEAM_COLOR)
                    .setEnumNameProvider(style -> Component.translatable("enum.locator-heads.border_style." + style.name().toLowerCase()))
                    .setSaveConsumer(value -> current.borderStyle = value)
                    .build());
            borders.addEntry(entries.startColorField(Component.translatable("option.locator-heads.static_border_color"), current.staticBorderColor)
                    .setTooltip(tooltip("tooltip.locator-heads.static_border_color"))
                    .setDefaultValue(0xFFFFFF)
                    .setSaveConsumer(value -> current.staticBorderColor = value)
                    .build());

            ConfigCategory filters = builder.getOrCreateCategory(Component.translatable("category.locator-heads.filters"));
            filters.addEntry(entries.startEnumSelector(Component.translatable("option.locator-heads.player_filter_mode"), LocatorHeadsConfig.PlayerFilterMode.class, current.playerFilterMode)
                    .setTooltip(tooltip("tooltip.locator-heads.player_filter_mode"))
                    .setDefaultValue(LocatorHeadsConfig.PlayerFilterMode.ALL)
                    .setEnumNameProvider(mode -> Component.translatable("enum.locator-heads.player_filter_mode." + mode.name().toLowerCase()))
                    .setSaveConsumer(value -> current.playerFilterMode = value)
                    .build());
            filters.addEntry(entries.startStrField(Component.translatable("option.locator-heads.included_players"), current.includedPlayers)
                    .setTooltip(tooltip("tooltip.locator-heads.included_players"))
                    .setDefaultValue("")
                    .setSaveConsumer(value -> current.includedPlayers = value)
                    .build());
            filters.addEntry(entries.startStrField(Component.translatable("option.locator-heads.excluded_players"), current.excludedPlayers)
                    .setTooltip(tooltip("tooltip.locator-heads.excluded_players"))
                    .setDefaultValue("")
                    .setSaveConsumer(value -> current.excludedPlayers = value)
                    .build());

            return builder.build();
        };
    }

    private static double convertHeadSize(int sliderValue) {
        return switch (sliderValue) {
            case 1 -> 0.5;
            case 2 -> 0.6;
            case 3 -> 0.7;
            case 4 -> 0.8;
            case 5 -> 1.0;
            case 6 -> 1.1;
            case 7 -> 1.2;
            case 8 -> 1.3;
            case 9 -> 1.5;
            default -> 1.0;
        };
    }

        private static Component[] tooltip(String key) {
                return new Component[] { Component.translatable(key) };
        }
}
