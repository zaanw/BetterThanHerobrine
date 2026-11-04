package com.betterthanherobrine.networking;
import com.betterthanherobrine.core.BetterThanHerobrine;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public record AdminState(boolean allowed,boolean open,boolean debug,int entities,int scenes,String details) implements CustomPacketPayload {
 public static final Type<AdminState> TYPE=new Type<>(BetterThanHerobrine.id("admin_state"));
 public static final StreamCodec<RegistryFriendlyByteBuf,AdminState> CODEC=StreamCodec.ofMember(AdminState::write,AdminState::read);
 private void write(RegistryFriendlyByteBuf b){b.writeBoolean(allowed);b.writeBoolean(open);b.writeBoolean(debug);b.writeVarInt(entities);b.writeVarInt(scenes);b.writeUtf(details,4096);}
 private static AdminState read(RegistryFriendlyByteBuf b){return new AdminState(b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readVarInt(),b.readVarInt(),b.readUtf(4096));}
 @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
