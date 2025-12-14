package haage;

import haage.config.ClothConfigWrapper;
import haage.config.LocatorHeadsConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

/**
 * Helper class to initialize Cloth Config.
 * This class is only loaded when Cloth Config is present, avoiding class loading issues.
 */
public class ClothConfigHelper {
    
    private static ConfigHolder<ClothConfigWrapper> configHolder;
    private static LocatorHeadsConfig bridgeConfig;
    
    public static LocatorHeadsConfig initialize() {
        // Register the config with AutoConfig
        configHolder = AutoConfig.register(ClothConfigWrapper.class, GsonConfigSerializer::new);
        
        // Create the bridge config that will be used by the mod
        bridgeConfig = new LocatorHeadsConfig();
        
        // Get the wrapper and link it to the bridge
        ClothConfigWrapper wrapper = configHolder.getConfig();
        wrapper.linkToConfig(bridgeConfig);
        
        // Add a save listener to sync changes
        configHolder.registerSaveListener((holder, config) -> {
            config.syncToActualConfig();
            return net.minecraft.world.InteractionResult.PASS;
        });
        
        // Return the bridge config
        return bridgeConfig;
    }
}
