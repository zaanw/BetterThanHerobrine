package com.betterthanherobrine.networking;
import com.betterthanherobrine.events.Cue;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
/** Small S2C-only packet. No player identity, chat, file names, or external destinations. */
public record CuePayload(String dimension,Cue.Kind kind,String resource,double x,double y,double z,float strength,int duration) implements CustomPacketPayload {
 public static final Type<CuePayload> TYPE=new Type<>(Identifier.fromNamespaceAndPath("betterthanherobrine","cue"));
 public static final StreamCodec<RegistryFriendlyByteBuf,CuePayload> CODEC=StreamCodec.ofMember(CuePayload::write,CuePayload::read);
 private void write(RegistryFriendlyByteBuf b){b.writeUtf(dimension,128);b.writeEnum(kind);b.writeUtf(resource,64);b.writeDouble(x);b.writeDouble(y);b.writeDouble(z);b.writeFloat(strength);b.writeVarInt(duration);}
 private static CuePayload read(RegistryFriendlyByteBuf b){
  String dim=b.readUtf(128);Cue.Kind k=b.readEnum(Cue.Kind.class);String r=b.readUtf(64);
  double x=b.readDouble(),y=b.readDouble(),z=b.readDouble();float s=b.readFloat();int d=b.readVarInt();
  if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z)||!Float.isFinite(s)||s<0||s>1||d<0||d>600)
   throw new IllegalArgumentException("Invalid horror payload");
  return new CuePayload(dim,k,r,x,y,z,s,d);
 }
 @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
