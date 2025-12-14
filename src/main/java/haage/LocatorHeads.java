package haage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import haage.config.LocatorHeadsConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class LocatorHeads implements ModInitializer {
	public static final String MOD_ID = "locator-heads";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static LocatorHeadsConfig CONFIG;

	@Override
	public void onInitialize() {
		// Check if Cloth Config is available before trying to use it
		if (FabricLoader.getInstance().isModLoaded("cloth-config")) {
			try {
				CONFIG = initializeWithClothConfig();
				LOGGER.info("Locator Heads mod initialized with Cloth Config support!");
			} catch (Exception e) {
				LOGGER.warn("Failed to initialize Cloth Config, using default config", e);
				CONFIG = new LocatorHeadsConfig();
			}
		} else {
			LOGGER.info("Cloth Config not found, using default config. Install Cloth Config for in-game configuration.");
			CONFIG = new LocatorHeadsConfig();
		}
	}

	/**
	 * Initialize config using Cloth Config. This method is in a separate method
	 * to avoid loading Cloth Config classes when they're not available.
	 */
	private LocatorHeadsConfig initializeWithClothConfig() {
		try {
			LOGGER.info("Attempting to initialize Cloth Config...");
			
			// Use the helper class that has direct access to Cloth Config classes
			return ClothConfigHelper.initialize();
		} catch (Exception e) {
			LOGGER.error("Detailed error initializing Cloth Config:", e);
			throw new RuntimeException("Failed to initialize Cloth Config", e);
		}
	}
}