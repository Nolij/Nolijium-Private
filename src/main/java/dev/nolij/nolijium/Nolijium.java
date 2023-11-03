package dev.nolij.nolijium;

import dev.nolij.nolijium.stress.NolijiumVoxelShapeStressor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Nolijium implements ModInitializer {
	
	public static final String MOD_ID = "nolijium";
	
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	
	@Override
	public void onInitialize() {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			LOGGER.info("Beginning stress test...");
			final long startTime = System.currentTimeMillis();
			for (var i = 0; i < 1000; i++) {
				NolijiumVoxelShapeStressor.stress();
			}
			LOGGER.info("Stress finished in {}s!", (System.currentTimeMillis() - startTime) / 1000D);
			System.exit(0);
		}
	}
	
}
