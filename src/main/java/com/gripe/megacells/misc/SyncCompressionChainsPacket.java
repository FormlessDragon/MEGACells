package com.gripe.megacells.misc;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SyncCompressionChainsPacket implements IMessage {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("megacells");

    private List<CompressionChain> chains = Collections.emptyList();

    public SyncCompressionChainsPacket() {
    }

    public SyncCompressionChainsPacket(List<CompressionChain> chains) {
        this.chains = new ArrayList<>(chains);
    }

    public static void init() {
        CHANNEL.registerMessage(Handler.class, SyncCompressionChainsPacket.class, 0, Side.CLIENT);
    }

    public List<CompressionChain> chains() {
        return Collections.unmodifiableList(chains);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        int size = buffer.readVarInt();
        List<CompressionChain> readChains = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            try {
                readChains.add(CompressionChain.read(buffer));
            } catch (IOException e) {
                throw new IllegalStateException("Unable to read compression chain packet", e);
            }
        }

        chains = readChains;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeVarInt(chains.size());

        for (CompressionChain chain : chains) {
            chain.write(buffer);
        }
    }

    public static class Handler implements IMessageHandler<SyncCompressionChainsPacket, IMessage> {
        @Override
        public IMessage onMessage(SyncCompressionChainsPacket message, MessageContext ctx) {
            CompressionService.replaceClientChains(message.chains);
            return null;
        }
    }
}
