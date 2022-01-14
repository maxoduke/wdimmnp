package io.github.maxoduke.wdimmnp;

import net.minecraft.util.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;

import io.github.maxoduke.wdimmnp.items.LinkedCompassModelPredicateProvider;

public class ClientMod implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        FabricModelPredicateProviderRegistry.register(
                Mod.LINKED_COMPASS_ITEM,
                new Identifier("angle"),
                new LinkedCompassModelPredicateProvider()
        );
    }
}
