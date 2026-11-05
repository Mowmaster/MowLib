package com.mowmaster.mowlib.network;

import com.mowmaster.mowlib.keys.KeyPayloads;
import com.mowmaster.mowlib.reading.ReadingPayloads;
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
    private static final String VERSION = "3"; // 2: added the page payloads; 3: key actions and the reading HUD

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

        // Client -> server: the action keys (keys/, client/KeyActionHandler)
        registrar.playToServer(KeyPayloads.Activate.TYPE, KeyPayloads.Activate.STREAM_CODEC, KeyPayloads.Activate::handle);
        registrar.playToServer(KeyPayloads.Place.TYPE, KeyPayloads.Place.STREAM_CODEC, KeyPayloads.Place::handle);
        registrar.playToServer(KeyPayloads.Pickup.TYPE, KeyPayloads.Pickup.STREAM_CODEC, KeyPayloads.Pickup::handle);
        registrar.playToServer(KeyPayloads.PageKeyTool.TYPE, KeyPayloads.PageKeyTool.STREAM_CODEC, KeyPayloads.PageKeyTool::handle);

        // The reading HUD (reading/): what a looked-at block or mob holds, asked from the server
        registrar.playToServer(ReadingPayloads.BlockRequest.TYPE, ReadingPayloads.BlockRequest.STREAM_CODEC, ReadingPayloads::handleBlockRequest);
        registrar.playToClient(ReadingPayloads.BlockAnswer.TYPE, ReadingPayloads.BlockAnswer.STREAM_CODEC, ReadingPayloads::handleBlockAnswer);
        registrar.playToServer(ReadingPayloads.EntityRequest.TYPE, ReadingPayloads.EntityRequest.STREAM_CODEC, ReadingPayloads::handleEntityRequest);
        registrar.playToClient(ReadingPayloads.EntityAnswer.TYPE, ReadingPayloads.EntityAnswer.STREAM_CODEC, ReadingPayloads::handleEntityAnswer);
    }
}
