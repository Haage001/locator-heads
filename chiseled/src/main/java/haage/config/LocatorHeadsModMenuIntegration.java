package haage.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import haage.LocatorHeads;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
//? if >=26.1
import net.minecraft.network.chat.Component;
//? if <=1.21.11
/*import net.minecraft.text.Text;*/

public class LocatorHeadsModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            LocatorHeadsConfig current = LocatorHeads.CONFIG != null ? LocatorHeads.CONFIG : new LocatorHeadsConfig();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
            //? if >=26.1
                    .setTitle(Component.translatable("title.locator-heads.config"));
            //? if <=1.21.11
            /*        .setTitle(Text.translatable("title.locator-heads.config"));*/

            builder.setSavingRunnable(() -> {
                LocatorHeads.CONFIG = current;
                LocatorHeadsConfigManager.save(current, LocatorHeads.LOGGER);
            });

            ConfigEntryBuilder entries = builder.entryBuilder();

            //? if >=26.1
            ConfigCategory general = builder.getOrCreateCategory(Component.translatable("category.locator-heads.general"));
            general.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.enable_mod"), current.enableMod)
            //? if <=1.21.11 {
            /*ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.locator-heads.general"));
            general.addEntry(entries.startBooleanToggle(Text.translatable("option.locator-heads.enable_mod"), current.enableMod)
            *///?}
                    .setDefaultValue(true)
                    .setSaveConsumer(value -> current.enableMod = value)
                    .build());
            //? if >=26.1
            general.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.render_heads"), current.renderHeads)
            //? if <=1.21.11
            /*general.addEntry(entries.startBooleanToggle(Text.translatable("option.locator-heads.render_heads"), current.renderHeads)*/
                    .setDefaultValue(true)
                    .setSaveConsumer(value -> current.renderHeads = value)
                    .build());
            //? if >=26.1
            general.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.always_show_xp"), current.alwaysShowXP)
            //? if <=1.21.11
            /*general.addEntry(entries.startBooleanToggle(Text.translatable("option.locator-heads.always_show_xp"), current.alwaysShowXP)*/
                    .setDefaultValue(false)
                    .setSaveConsumer(value -> current.alwaysShowXP = value)
                    .build());
            //? if >=26.1 {
            general.addEntry(entries.startIntSlider(Component.translatable("option.locator-heads.head_size"), current.headSizeMultiplier, 1, 9)
                    .setDefaultValue(5)
                    .setTextGetter(value -> Component.literal(String.format("%.1fx", convertHeadSize(value))))
            //?} else {
            /*general.addEntry(entries.startIntSlider(Text.translatable("option.locator-heads.head_size"), current.headSizeMultiplier, 1, 9)
                    .setDefaultValue(5)
                    .setTextGetter(value -> Text.literal(String.format("%.1fx", convertHeadSize(value))))
            *///?}
                    .setSaveConsumer(value -> current.headSizeMultiplier = value)
                    .build());
            //? if >=26.1 {
            general.addEntry(entries.startEnumSelector(Component.translatable("option.locator-heads.show_player_names"), LocatorHeadsConfig.NameDisplayMode.class, current.showPlayerNames)
                    .setDefaultValue(LocatorHeadsConfig.NameDisplayMode.OFF)
                    .setEnumNameProvider(mode -> Component.translatable("enum.locator-heads.name_display_mode." + mode.name().toLowerCase()))
            //?} else {
            /*general.addEntry(entries.startEnumSelector(Text.translatable("option.locator-heads.show_player_names"), LocatorHeadsConfig.NameDisplayMode.class, current.showPlayerNames)
                    .setDefaultValue(LocatorHeadsConfig.NameDisplayMode.OFF)
                    .setEnumNameProvider(mode -> Text.translatable("enum.locator-heads.name_display_mode." + mode.name().toLowerCase()))
            *///?}
                    .setSaveConsumer(value -> current.showPlayerNames = value)
                    .build());
            //? if >=26.1
            general.addEntry(entries.startStrField(Component.translatable("option.locator-heads.max_player_marker_distance"), current.getMaxPlayerMarkerDistanceText())
            //? if <=1.21.11
            /*general.addEntry(entries.startStrField(Text.translatable("option.locator-heads.max_player_marker_distance"), current.getMaxPlayerMarkerDistanceText())*/
                    .setDefaultValue("")
                    .setSaveConsumer(current::setMaxPlayerMarkerDistanceFromText)
                    .build());

            //? if >=26.1 {
            ConfigCategory compass = builder.getOrCreateCategory(Component.translatable("category.locator-heads.compass"));
            compass.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.show_compass"), current.showCompass)
            //?} else {
            /*ConfigCategory compass = builder.getOrCreateCategory(Text.translatable("category.locator-heads.compass"));
            compass.addEntry(entries.startBooleanToggle(Text.translatable("option.locator-heads.show_compass"), current.showCompass)
            *///?}
                    .setDefaultValue(false)
                    .setSaveConsumer(value -> current.showCompass = value)
                    .build());
            //? if >=26.1
            compass.addEntry(entries.startColorField(Component.translatable("option.locator-heads.compass_color"), current.compassColor)
            //? if <=1.21.11
            /*compass.addEntry(entries.startColorField(Text.translatable("option.locator-heads.compass_color"), current.compassColor)*/
                    .setDefaultValue(0xFFFFFF)
                    .setSaveConsumer(value -> current.compassColor = value)
                    .build());
            //? if >=26.1
            compass.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.compass_shadow"), current.compassShadow)
            //? if <=1.21.11
            /*compass.addEntry(entries.startBooleanToggle(Text.translatable("option.locator-heads.compass_shadow"), current.compassShadow)*/
                    .setDefaultValue(true)
                    .setSaveConsumer(value -> current.compassShadow = value)
                    .build());
            //? if >=26.1
            compass.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.coordinates_notation"), current.useCoordinatesNotation)
            //? if <=1.21.11
            /*compass.addEntry(entries.startBooleanToggle(Text.translatable("option.locator-heads.coordinates_notation"), current.useCoordinatesNotation)*/
                    .setDefaultValue(false)
                    .setSaveConsumer(value -> current.useCoordinatesNotation = value)
                    .build());

            //? if >=26.1 {
            ConfigCategory borders = builder.getOrCreateCategory(Component.translatable("category.locator-heads.borders"));
            borders.addEntry(entries.startBooleanToggle(Component.translatable("option.locator-heads.enable_team_border"), current.enableTeamBorder)
            //?} else {
            /*ConfigCategory borders = builder.getOrCreateCategory(Text.translatable("category.locator-heads.borders"));
            borders.addEntry(entries.startBooleanToggle(Text.translatable("option.locator-heads.enable_team_border"), current.enableTeamBorder)
            *///?}
                    .setDefaultValue(false)
                    .setSaveConsumer(value -> current.enableTeamBorder = value)
                    .build());
            //? if >=26.1 {
            borders.addEntry(entries.startEnumSelector(Component.translatable("option.locator-heads.team_border_thickness"), LocatorHeadsConfig.BorderThickness.class, current.teamBorderThickness)
                    .setDefaultValue(LocatorHeadsConfig.BorderThickness.NORMAL)
                    .setEnumNameProvider(thickness -> Component.translatable("enum.locator-heads.border_thickness." + thickness.name().toLowerCase()))
            //?} else {
            /*borders.addEntry(entries.startEnumSelector(Text.translatable("option.locator-heads.team_border_thickness"), LocatorHeadsConfig.BorderThickness.class, current.teamBorderThickness)
                    .setDefaultValue(LocatorHeadsConfig.BorderThickness.NORMAL)
                    .setEnumNameProvider(thickness -> Text.translatable("enum.locator-heads.border_thickness." + thickness.name().toLowerCase()))
            *///?}
                    .setSaveConsumer(value -> current.teamBorderThickness = value)
                    .build());
            //? if >=26.1 {
            borders.addEntry(entries.startEnumSelector(Component.translatable("option.locator-heads.border_style"), LocatorHeadsConfig.BorderStyle.class, current.borderStyle)
                    .setDefaultValue(LocatorHeadsConfig.BorderStyle.TEAM_COLOR)
                    .setEnumNameProvider(style -> Component.translatable("enum.locator-heads.border_style." + style.name().toLowerCase()))
            //?} else {
            /*borders.addEntry(entries.startEnumSelector(Text.translatable("option.locator-heads.border_style"), LocatorHeadsConfig.BorderStyle.class, current.borderStyle)
                    .setDefaultValue(LocatorHeadsConfig.BorderStyle.TEAM_COLOR)
                    .setEnumNameProvider(style -> Text.translatable("enum.locator-heads.border_style." + style.name().toLowerCase()))
            *///?}
                    .setSaveConsumer(value -> current.borderStyle = value)
                    .build());
            //? if >=26.1
            borders.addEntry(entries.startColorField(Component.translatable("option.locator-heads.static_border_color"), current.staticBorderColor)
            //? if <=1.21.11
            /*borders.addEntry(entries.startColorField(Text.translatable("option.locator-heads.static_border_color"), current.staticBorderColor)*/
                    .setDefaultValue(0xFFFFFF)
                    .setSaveConsumer(value -> current.staticBorderColor = value)
                    .build());

            //? if >=26.1 {
            ConfigCategory filters = builder.getOrCreateCategory(Component.translatable("category.locator-heads.filters"));
            filters.addEntry(entries.startEnumSelector(Component.translatable("option.locator-heads.player_filter_mode"), LocatorHeadsConfig.PlayerFilterMode.class, current.playerFilterMode)
                    .setDefaultValue(LocatorHeadsConfig.PlayerFilterMode.ALL)
                    .setEnumNameProvider(mode -> Component.translatable("enum.locator-heads.player_filter_mode." + mode.name().toLowerCase()))
            //?} else {
            /*ConfigCategory filters = builder.getOrCreateCategory(Text.translatable("category.locator-heads.filters"));
            filters.addEntry(entries.startEnumSelector(Text.translatable("option.locator-heads.player_filter_mode"), LocatorHeadsConfig.PlayerFilterMode.class, current.playerFilterMode)
                    .setDefaultValue(LocatorHeadsConfig.PlayerFilterMode.ALL)
                    .setEnumNameProvider(mode -> Text.translatable("enum.locator-heads.player_filter_mode." + mode.name().toLowerCase()))
            *///?}
                    .setSaveConsumer(value -> current.playerFilterMode = value)
                    .build());
            //? if >=26.1
            filters.addEntry(entries.startStrField(Component.translatable("option.locator-heads.included_players"), current.includedPlayers)
            //? if <=1.21.11
            /*filters.addEntry(entries.startStrField(Text.translatable("option.locator-heads.included_players"), current.includedPlayers)*/
                    .setDefaultValue("")
                    .setSaveConsumer(value -> current.includedPlayers = value)
                    .build());
            //? if >=26.1
            filters.addEntry(entries.startStrField(Component.translatable("option.locator-heads.excluded_players"), current.excludedPlayers)
            //? if <=1.21.11
            /*filters.addEntry(entries.startStrField(Text.translatable("option.locator-heads.excluded_players"), current.excludedPlayers)*/
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
}
