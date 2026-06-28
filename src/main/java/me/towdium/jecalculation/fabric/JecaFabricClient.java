package me.towdium.jecalculation.fabric;

import me.towdium.jecalculation.JustEnoughCalculation;
import net.fabricmc.api.ClientModInitializer;

public final class JecaFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        new JustEnoughCalculation();
    }
}
