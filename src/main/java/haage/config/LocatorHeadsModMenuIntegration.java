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
                    .setDefaultValue(true)
                    .setTooltip(tooltip("option.locator-heads.enable_mod"))
                    .setSaveConsumer(value -> current.enableMod = value)
                    .build());
            general.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.render_heads"), current.renderHeads)
                    .setDefaultValue(true)
                    .setTooltip(tooltip("option.locator-heads.render_heads"))
                    .setSaveConsumer(value -> current.renderHeads = value)
                    .build());
            general.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.always_show_xp"), current.alwaysShowXP)
                    .setDefaultValue(false)
                    .setTooltip(tooltip("option.locator-heads.always_show_xp"))
                    .setSaveConsumer(value -> current.alwaysShowXP = value)
                    .build());
            general.addEntry(entries.startIntSlider(Component.translatable("option.locator-heads.head_size"), current.headSizeMultiplier, 1, 9)
                    .setDefaultValue(5)
                    .setTextGetter(value -> Component.literal(String.format("%.1fx", convertHeadSize(value))))
                    .setSaveConsumer(value -> current.headSizeMultiplier = value)
                    .build());
            general.addEntry(entries.startEnumSelector(Component.translatable("option.locator-heads.show_player_names"), LocatorHeadsConfig.NameDisplayMode.class, current.showPlayerNames)
                    .setDefaultValue(LocatorHeadsConfig.NameDisplayMode.OFF)
                    .setEnumNameProvider(mode -> Component.translatable("enum.locator-heads.name_display_mode." + mode.name().toLowerCase()))
                    .setSaveConsumer(value -> current.showPlayerNames = value)
                    .build());
            general.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.only_nearby_player_heads"), current.onlyShowNearbyPlayerHeads)
                    .setDefaultValue(false)
                    .setTooltip(tooltip("option.locator-heads.only_nearby_player_heads"))
                    .setSaveConsumer(value -> current.onlyShowNearbyPlayerHeads = value)
                    .build());

            ConfigCategory compass = builder.getOrCreateCategory(Component.translatable("category.locator-heads.compass"));
            compass.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.show_compass"), current.showCompass)
                    .setDefaultValue(false)
                    .setSaveConsumer(value -> current.showCompass = value)
                    .build());
            compass.addEntry(entries.startColorField(Component.translatable("option.locator-heads.compass_color"), current.compassColor)
                    .setDefaultValue(0xFFFFFF)
                    .setSaveConsumer(value -> current.compassColor = value)
                    .build());
            compass.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.compass_shadow"), current.compassShadow)
                    .setDefaultValue(true)
                    .setSaveConsumer(value -> current.compassShadow = value)
                    .build());
            compass.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.coordinates_notation"), current.useCoordinatesNotation)
                    .setDefaultValue(false)
                    .setTooltip(tooltip("option.locator-heads.coordinates_notation"))
                    .setSaveConsumer(value -> current.useCoordinatesNotation = value)
                    .build());

            ConfigCategory borders = builder.getOrCreateCategory(Component.translatable("category.locator-heads.borders"));
            borders.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.enable_team_border"), current.enableTeamBorder)
                    .setDefaultValue(false)
                    .setTooltip(tooltip("option.locator-heads.enable_team_border"))
                    .setSaveConsumer(value -> current.enableTeamBorder = value)
                    .build());
            borders.addEntry(entries.startEnumSelector(Component.translatable("option.locator-heads.border_style"), LocatorHeadsConfig.BorderStyle.class, current.borderStyle)
                    .setDefaultValue(LocatorHeadsConfig.BorderStyle.TEAM_COLOR)
                    .setEnumNameProvider(style -> Component.translatable("enum.locator-heads.border_style." + style.name().toLowerCase()))
                    .setSaveConsumer(value -> current.borderStyle = value)
                    .build());
            borders.addEntry(entries.startEnumSelector(Component.translatable("option.locator-heads.team_border_thickness"), LocatorHeadsConfig.BorderThickness.class, current.teamBorderThickness)
                    .setDefaultValue(LocatorHeadsConfig.BorderThickness.NORMAL)
                    .setEnumNameProvider(thickness -> Component.translatable("enum.locator-heads.border_thickness." + thickness.name().toLowerCase()))
                    .setSaveConsumer(value -> current.teamBorderThickness = value)
                    .build());
            borders.addEntry(entries.startColorField(Component.translatable("option.locator-heads.static_border_color"), current.staticBorderColor)
                    .setDefaultValue(0xFFFFFF)
                    .setSaveConsumer(value -> current.staticBorderColor = value)
                    .build());

            ConfigCategory filters = builder.getOrCreateCategory(Component.translatable("category.locator-heads.filters"));
            filters.addEntry(entries.startEnumSelector(Component.translatable("option.locator-heads.player_filter_mode"), LocatorHeadsConfig.PlayerFilterMode.class, current.playerFilterMode)
                    .setDefaultValue(LocatorHeadsConfig.PlayerFilterMode.ALL)
                    .setTooltip(tooltip("option.locator-heads.player_filter_mode"))
                    .setEnumNameProvider(mode -> Component.translatable("enum.locator-heads.player_filter_mode." + mode.name().toLowerCase()))
                    .setSaveConsumer(value -> current.playerFilterMode = value)
                    .build());
            filters.addEntry(entries.startStrField(Component.translatable("option.locator-heads.included_players"), current.includedPlayers)
                    .setDefaultValue("")
                    .setTooltip(tooltip("option.locator-heads.included_players"))
                    .setSaveConsumer(value -> current.includedPlayers = value)
                    .build());
            filters.addEntry(entries.startStrField(Component.translatable("option.locator-heads.excluded_players"), current.excludedPlayers)
                    .setDefaultValue("")
                    .setTooltip(tooltip("option.locator-heads.excluded_players"))
                    .setSaveConsumer(value -> current.excludedPlayers = value)
                    .build());

            return builder.build();
        };
    }

    private static Component tooltip(String baseKey) {
        return Component.translatable(baseKey + ".@Tooltip");
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
}
