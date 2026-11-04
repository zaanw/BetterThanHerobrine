package com.betterthanherobrine.core;
import com.betterthanherobrine.config.HorrorConfig;
import com.betterthanherobrine.entity.WatcherEntity;
import com.betterthanherobrine.horror.*;
import com.betterthanherobrine.commands.BthCommands;
import com.betterthanherobrine.events.Cue;
import com.betterthanherobrine.networking.CuePayload;
import com.betterthanherobrine.world.WatcherPlacement;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import org.slf4j.*;
import java.nio.file.Path;
import java.io.IOException;
import java.util.*;

public final class BetterThanHerobrine implements ModInitializer {
 public static final String ID="betterthanherobrine";
 public static final Logger LOG=LoggerFactory.getLogger(ID);
 public static Identifier id(String path){return Identifier.fromNamespaceAndPath(ID,path);}
 private static final ResourceKey<EntityType<?>> KEY=ResourceKey.create(Registries.ENTITY_TYPE,id("watcher"));
 public static final EntityType<WatcherEntity> WATCHER=Registry.register(BuiltInRegistries.ENTITY_TYPE,KEY,
  FabricEntityType.Builder.createMob(WatcherEntity::new,MobCategory.MISC,b->b.defaultAttributes(Zombie::createAttributes))
   .sized(.6f,1.95f).noSave().clientTrackingRange(12).build(KEY));
 public static BetterThanHerobrine INSTANCE;
 public HorrorDirector director;
 public HorrorConfig config;
 public final WatcherPlacement placement=new WatcherPlacement();
 private long second;
 private final Map<UUID,Long> enteredAt=new HashMap<>();
 private int ticks;
 private Path state;
 private final Map<UUID,Vec3> lastPosition=new HashMap<>();
 private final Map<UUID,Integer> residence=new HashMap<>();
 private final Map<UUID,String> dimensions=new HashMap<>();
 public Path configPath(){return FabricLoader.getInstance().getConfigDir().resolve(ID+"-server.properties");}
 @Override public void onInitialize(){
  INSTANCE=this;
  try{config=HorrorConfig.load(configPath());}catch(IOException e){throw new IllegalStateException("Cannot load BTH server configuration",e);}
  director=new HorrorDirector(config,new Random());
  PayloadTypeRegistry.clientboundPlay().register(CuePayload.TYPE,CuePayload.CODEC);
  BthCommands.register(this);
  ServerLifecycleEvents.SERVER_STARTED.register(server->{
   second=0;ticks=0;director.clear();lastPosition.clear();residence.clear();dimensions.clear();enteredAt.clear();
   state=server.getWorldPath(LevelResource.ROOT).resolve("betterthanherobrine/memory.properties");
   try{MemoryStore.load(state,director,second);}catch(IOException e){LOG.error("BTH history unreadable; original retained",e);state=null;}
  });
  ServerTickEvents.END_SERVER_TICK.register(this::tick);
  ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->{
   UUID id=handler.player.getUUID();director.disconnect(id);placement.clear(id);lastPosition.remove(id);residence.remove(id);dimensions.remove(id);enteredAt.remove(id);save();
  });
  ServerLifecycleEvents.SERVER_STOPPING.register(server->{save();placement.clearAll();director.clear();});
 }
 public long second(){return second;}
 public Context context(ServerPlayer p){
  ServerLevel level=(ServerLevel)p.level();Vec3 old=lastPosition.get(p.getUUID());
  boolean moving=old!=null&&old.distanceToSqr(p.position())>.5;
  int nearby=0;for(ServerPlayer other:level.players())if(other!=p&&other.distanceToSqr(p)<48*48&&!other.isSpectator()&&++nearby>=8)break;
  var biome=level.getBiome(p.blockPosition());
  boolean forest=biome.is(Biomes.FOREST)||biome.is(Biomes.DARK_FOREST)||biome.is(Biomes.TAIGA)||biome.is(Biomes.OLD_GROWTH_PINE_TAIGA);
  return new Context(second,(second*20L)%24000L,!level.canSeeSky(p.blockPosition()),forest,level.isRaining(),level.isThundering(),
   residence.getOrDefault(p.getUUID(),0)>120,level.containsAnyLiquid(p.getBoundingBox().inflate(3)),
   level.getMaxLocalRawBrightness(p.blockPosition()),nearby,((((long)(p.blockPosition().getX() >> 4)) << 32) | ((p.blockPosition().getZ() >> 4) & 0xffffffffL)),p.getYRot(),moving,false);
 }
 private void tick(MinecraftServer server){
  if(++ticks%20!=0)return;second++;Map<UUID,Context> contexts=new LinkedHashMap<>();
  for(ServerPlayer p:server.getPlayerList().getPlayers()){
   UUID id=p.getUUID();
   long age=second-enteredAt.computeIfAbsent(id,key->second);
   if(config.enabled&&!p.isSpectator())nightmareSequence(p,age);
   String dim=p.level().dimension().identifier().toString();
   String previous=dimensions.put(id,dim);
   if(previous!=null&&!previous.equals(dim)){placement.clear(id);director.disconnect(id);lastPosition.remove(id);residence.remove(id);}
   if(!p.isAlive()||p.isSpectator()||p.isCreative()){director.disconnect(id);placement.clear(id);continue;}
   Context c=context(p);contexts.put(id,c);
   residence.compute(id,(k,v)->c.moving()?0:Math.min(600,(v==null?0:v)+1));lastPosition.put(id,p.position());
  }
  for(var emitted:director.tick(contexts)){
   ServerPlayer player=server.getPlayerList().getPlayer(emitted.player());if(player!=null)emit(player,emitted.cue());
  }
  if(!config.enabled)placement.clearAll();placement.tick();if(second%60==0)save();
 }
 /** Deterministic server-side opening scene, counted in seconds spent inside a world. */
 private void nightmareSequence(ServerPlayer player,long age){
  // The first few seconds are intentionally unsettling; effects stay bounded.
  if(age==2){
   emit(player,Cue.sound(0,"whisper",-2,0,1,.95f));
   emit(player,Cue.effect(0,Cue.Kind.TINT,.8f,9));
   emit(player,Cue.effect(0,Cue.Kind.FOG,.9f,22));
  }
  if(age==5){
   lightning(player,3,2);
   emit(player,Cue.sound(0,"stone",-4,1,0,.9f));
  }
  if(age==9){
   Vec3 behind=player.getLookAngle().scale(-13);
   placement.spawn(player,player.position().add(behind),35);
   emit(player,new Cue(0,Cue.Kind.PARTICLES,"shadow",-2,0,1,.9f,15));
  }
  if(age>=12&&age<60&&age%11==0){
   lightning(player,age%2==0?4:-4,3);
   emit(player,Cue.effect(0,Cue.Kind.TINT,.5f,5));
   emit(player,Cue.sound(0,"step",-3,1,0,.7f));
  }
  if(age==60){
   player.sendSystemMessage(Component.translatable("multiplayer.player.joined",Component.literal("Herobrine")));
   lightning(player,0,0);
   emit(player,Cue.effect(0,Cue.Kind.FOG,.95f,110));
   emit(player,Cue.effect(0,Cue.Kind.TINT,.85f,25));
   emit(player,Cue.sound(0,"whisper",-2,0,1,.9f));
  }
  // One storm chapter only. Lightning is visual-only: no fire, terrain loss or PvP damage.
  if(age==62||age==65||age==69||age==75||age==83){
   lightning(player,age%2==0?2:-3,age%3==0?3:-2);
   emit(player,Cue.effect(0,Cue.Kind.TINT,.75f,7));
   emit(player,Cue.sound(0,"stone",4,1,0,.8f));
  }
  if(age==64||age==78||age==91){
   Vec3 behind=player.getLookAngle().scale(-12);
   Vec3 pos=player.position().add(behind);
   placement.spawn(player,pos,35);
   emit(player,new Cue(0,Cue.Kind.PARTICLES,"shadow",-4,0,1,.9f,15));
   emit(player,Cue.sound(0,"breath",-2,0,1,.75f));
  }
  if(age==72||age==88){
   emit(player,Cue.effect(0,Cue.Kind.SYMBOL,.85f,20));
   emit(player,Cue.effect(0,Cue.Kind.SILENCE,.8f,16));
  }
  // APOCALYPSE: relentless but bounded chaos after the intro.
  // All effects expire, no blocks are modified, no uncontrolled mob swarms.
  if(age>=96){
   long phase=age-96;
   if(phase%12==0){
    lightning(player,phase%24==0?4:-4,phase%36==0?2:-2);
    emit(player,Cue.effect(0,Cue.Kind.FOG,.9f,25));
    emit(player,Cue.effect(0,Cue.Kind.TINT,.65f,8));
    emit(player,Cue.sound(0,"whisper",-2,1,0,.8f));
   }
   if(phase%17==0){
    emit(player,new Cue(0,Cue.Kind.PARTICLES,"shadow",3,0,1,.95f,15));
    emit(player,Cue.sound(0,"step",-4,-1,0,.95f));
    emit(player,Cue.effect(0,Cue.Kind.SYMBOL,.75f,12));
   }
   if(phase%28==0){
    placement.clear(player.getUUID());
    Vec3 forward=player.getLookAngle().scale(15);
    placement.spawn(player,player.position().add(forward),23);
    emit(player,Cue.sound(0,"breath",-3,0,1,.95f));
   }
   if(phase%43==0){
    emit(player,Cue.effect(0,Cue.Kind.SILENCE,.95f,16));
    emit(player,Cue.effect(0,Cue.Kind.FOG,1f,18));
   }
  }
 }
 public void adminChaos(ServerPlayer player){
  if(!config.enabled)return;
  lightning(player,4,3);
  lightning(player,-5,-2);
  emit(player,Cue.effect(0,Cue.Kind.FOG,.95f,35));
  emit(player,Cue.effect(0,Cue.Kind.TINT,.85f,12));
  emit(player,new Cue(0,Cue.Kind.PARTICLES,"shadow",3,0,1,.9f,20));
  emit(player,Cue.sound(0,"whisper",-2,0,0,.9f));
  placement.clear(player.getUUID());
  placement.spawn(player,player.position().add(player.getLookAngle().scale(-14)),35);
 }
 private void lightning(ServerPlayer player,double offsetX,double offsetZ){
  ServerLevel level=(ServerLevel)player.level();
  var lightningType=BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.fromNamespaceAndPath("minecraft","lightning_bolt"));
  if(lightningType==null)return;
  var entity=lightningType.create(level,EntitySpawnReason.TRIGGERED);
  if(!(entity instanceof LightningBolt bolt))return;
  bolt.setVisualOnly(true);
  bolt.snapTo(player.getX()+offsetX,player.getY(),player.getZ()+offsetZ);
  level.addFreshEntity(bolt);
 }
 public void emit(ServerPlayer player,Cue cue){
  if(cue.kind()!=Cue.Kind.CLEAR&&!config.enabled)return;
  if(cue.kind()==Cue.Kind.ENTITY&&config.entityFrequency==0)return;
  if((cue.kind()==Cue.Kind.SOUND||cue.kind()==Cue.Kind.SILENCE)&&!config.atmosphericSounds)return;
  if((cue.kind()==Cue.Kind.FOG||cue.kind()==Cue.Kind.TINT||cue.kind()==Cue.Kind.PARTICLES||cue.kind()==Cue.Kind.SYMBOL)&&!config.visualEffects)return;
  if(cue.kind()==Cue.Kind.TINT&&!config.screenEffects)return;
  if(cue.kind()==Cue.Kind.SYMBOL&&!config.worldAnomalies)return;
  Vec3 forward=Vec3.directionFromRotation(0,player.getYRot());Vec3 side=new Vec3(-forward.z,0,forward.x);
  Vec3 pos=player.position().add(forward.scale(cue.forward())).add(side.scale(cue.side())).add(0,cue.up(),0);
  if(cue.kind()==Cue.Kind.ENTITY){placement.spawn(player,pos,cue.duration());return;}
  if(cue.kind()==Cue.Kind.CLEAR)placement.clear(player.getUUID());
  if(ServerPlayNetworking.canSend(player,CuePayload.TYPE))
   ServerPlayNetworking.send(player,new CuePayload(player.level().dimension().identifier().toString(),cue.kind(),cue.resource(),pos.x,pos.y,pos.z,cue.strength(),cue.duration()));
 }
 public void save(){if(state!=null)try{MemoryStore.save(state,director,second);}catch(IOException e){LOG.error("Cannot save BTH history",e);}}
 public void saveConfig(){try{config.save(configPath());}catch(IOException e){throw new IllegalStateException("Cannot save BTH configuration",e);}}
}
