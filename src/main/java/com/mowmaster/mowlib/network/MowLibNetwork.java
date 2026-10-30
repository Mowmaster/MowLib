package com.mowmaster.mowlib.network;

import com.mowmaster.mowlib.MowLibReferences;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

// Registers MowLib's network payloads. The registrar's version must match between client and server, so bump it
// whenever a payload is added or its format changes (mismatched versions then refuse to connect instead of failing
// on an unknown payload). Handlers run on the main thread by default.
@EventBusSubscriber(modid = MowLibReferences.MODID)
public class MowLibNetwork
{
    private static final String VERSION = "2"; // 2: added the page payloads

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event)
    {
        PayloadRegistrar registrar = event.registrar(VERSION);

        // Server -> client
        registrar.playToClient(TransferParticlesPayload.TYPE, TransferParticlesPayload.STREAM_CODEC, TransferParticlesPayload::handle);

        // Client -> server: page selection and turning (paging/, client/PageInputHandler)
        registrar.playToServer(SelectPagePayload.TYPE, SelectPagePayload.STREAM_CODEC, SelectPagePayload::handle);
        registrar.playToServer(TurnHeldPagePayload.TYPE, TurnHeldPagePayload.STREAM_CODEC, TurnHeldPagePayload::handle);
        registrar.playToServer(TurnBlockPagePayload.TYPE, TurnBlockPagePayload.STREAM_CODEC, TurnBlockPagePayload::handle);
    }
}
