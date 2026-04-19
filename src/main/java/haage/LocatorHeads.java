package haage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import haage.config.LocatorHeadsConfig;
import haage.config.LocatorHeadsConfigManager;
import net.fabricmc.api.ModInitializer;

public class LocatorHeads implements ModInitializer {
	public static final String MOD_ID = "locator-heads";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static LocatorHeadsConfig CONFIG;

	@Override
	public void onInitialize() {
		CONFIG = LocatorHeadsConfigManager.load(LOGGER);
		LOGGER.info("Locator Heads mod initialized with config support.");
	}
}