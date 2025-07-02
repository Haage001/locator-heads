package haage;

import haage.config.LocatorHeadsConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocatorHeads implements ModInitializer {
	public static final String MOD_ID = "locator-heads";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static LocatorHeadsConfig CONFIG;

	@Override
	public void onInitialize() {
		// Initialize Cloth Config
		AutoConfig.register(LocatorHeadsConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(LocatorHeadsConfig.class).getConfig();

		LOGGER.info("Locator Heads mod initialized with ModMenu + Cloth Config support!");
	}
}