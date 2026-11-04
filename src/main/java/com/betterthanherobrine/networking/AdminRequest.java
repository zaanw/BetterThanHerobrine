package com.betterthanherobrine.networking;
import com.betterthanherobrine.core.BetterThanHerobrine;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
/** C2S input carries no username or permission claims. */
public record AdminRequest(Action action,String event) implements CustomPacketPayload {
 public enum Action { OPEN, SPAWN, REMOVE, RANDOM, EVENT, NIGHTMARE, DEBUG, ENTITIES, TIMERS, RESET, STOP }
 public static final Type<AdminRequest> TYPE=new Type<>(BetterThanHerobrine.id("admin_request"));
 public static final StreamCodec<RegistryFriendlyByteBuf,AdminRequest> CODEC=StreamCodec.ofMember(AdminRequest::write,AdminRequest::read);
 private void write(RegistryFriendlyByteBuf b){b.writeEnum(action);b.writeUtf(event,64);}
 private static AdminRequest read(RegistryFriendlyByteBuf b){return new AdminRequest(b.readEnum(Action.class),b.readUtf(64));}
 @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
