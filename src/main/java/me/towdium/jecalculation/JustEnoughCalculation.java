package me.towdium.jecalculation;

import me.towdium.jecalculation.data.Controller;
import me.towdium.jecalculation.data.label.ILabel;
import me.towdium.jecalculation.events.GuiScreenEventHandler;
import me.towdium.jecalculation.gui.JecaGui;
import me.towdium.jecalculation.network.packets.PCalculator;
import me.towdium.jecalculation.network.packets.PEdit;
import me.towdium.jecalculation.network.packets.PRecord;
import me.towdium.jecalculation.utils.Utilities;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static me.towdium.jecalculation.gui.JecaGui.keyOpenGuiCraft;
import static me.towdium.jecalculation.gui.JecaGui.keyOpenGuiMath;

public class JustEnoughCalculation {
    public static final String MODID = "jecalculation";
    public static final String MODNAME = "Just Enough Calculation";
    public static final Identifier PACKET_CHANNEL = Identifier.fromNamespaceAndPath(MODID, "main");
    public static Logger logger = LogManager.getLogger(MODID);

    @Environment(EnvType.CLIENT)
    public static class Client {
        @Environment(EnvType.CLIENT)
        public static GuiScreenEventHandler GUI_HANDLER = null;
    }

    public JustEnoughCalculation() {
        registerNetworkPackets();

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            registerClientEvents();
        }

        if (!Utilities.config().mkdirs() && !Utilities.config().exists()) {
            logger.warn("Failed to create config directory: {}", Utilities.config());
        }
    }

    private void registerNetworkPackets() {
        PayloadTypeRegistry.clientboundPlay().register(PRecord.TYPE, PRecord.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PCalculator.TYPE, PCalculator.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PEdit.TYPE, PEdit.CODEC);

        // Server-side handlers
        ServerLifecycleEvents.SERVER_STARTED.register(server -> ILabel.initServer());
    }

    @Environment(EnvType.CLIENT)
    private void registerClientEvents() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            ILabel.initClient();
            Controller.loadFromLocal();
            Client.GUI_HANDLER = new GuiScreenEventHandler();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (keyOpenGuiCraft.isDown()) JecaGui.openGuiCraft(null, 0);
            if (keyOpenGuiMath.isDown()) JecaGui.openGuiMath(null, 0);
        });

        KeyMappingHelper.registerKeyMapping(keyOpenGuiCraft);
        KeyMappingHelper.registerKeyMapping(keyOpenGuiMath);
    }
}
