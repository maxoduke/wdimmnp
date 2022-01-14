package io.github.maxoduke.wdimmnp;

import io.github.maxoduke.wdimmnp.items.LinkedCompassItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Mod implements ModInitializer
{
    public static final String MOD_ID = "wdimmnp";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final LinkedCompassItem LINKED_COMPASS_ITEM = new LinkedCompassItem(new FabricItemSettings().group(ItemGroup.TOOLS));

    @Override
    public void onInitialize()
    {
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "linked_compass"), LINKED_COMPASS_ITEM);

        LOGGER.info("Initialization complete.");
    }
}
