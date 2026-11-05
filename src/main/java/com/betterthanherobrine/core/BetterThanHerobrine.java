package com.betterthanherobrine.core;
import com.betterthanherobrine.config.HorrorConfig;
import com.betterthanherobrine.entity.WatcherEntity;
import com.betterthanherobrine.horror.*;
import com.betterthanherobrine.commands.BthCommands;
import com.betterthanherobrine.events.Cue;
import com.betterthanherobrine.networking.*;
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.level.block.Blocks;
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
 public static final ResourceKey<EntityType<?>> HEROBRINE_KEY=ResourceKey.create(Registries.ENTITY_TYPE,id("herobrine"));
 public static final EntityType<WatcherEntity> HEROBRINE=Registry.register(BuiltInRegistries.ENTITY_TYPE,HEROBRINE_KEY,
  FabricEntityType.Builder.createMob(WatcherEntity::new,MobCategory.MISC,b->b.defaultAttributes(Zombie::createAttributes))
   .sized(.6f,1.8f).noSave().clientTrackingRange(12).build(HEROBRINE_KEY));
 private final Map<UUID,Long> adminRequests=new HashMap<>();
 private final Set<UUID> overlays=new HashSet<>();
 private final Map<UUID,BlockPos> windows=new HashMap<>();
 private final Map<UUID,Boolean> villages=new HashMap<>();
 public static BetterThanHerobrine INSTANCE;
 public HorrorDirector director;
 public HorrorConfig config;
 public final WatcherPlacement placement=new WatcherPlacement();
 private long second;

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
  PayloadTypeRegistry.serverboundPlay().register(AdminRequest.TYPE,AdminRequest.CODEC);
  PayloadTypeRegistry.clientboundPlay().register(AdminState.TYPE,AdminState.CODEC);
  ServerPlayNetworking.registerGlobalReceiver(AdminRequest.TYPE,(packet,context)->{
   var player=context.player();
   // Fabric invokes play handlers on the server thread; use only the authenticated player.
   if(second<adminRequests.getOrDefault(player.getUUID(),-1L))return;
   adminRequests.put(player.getUUID(),second+1);
   adminAction(player,packet.action(),packet.event());
  });
  net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.AFTER.register((world,player,pos,block,entity)->{
   if(player instanceof ServerPlayer sp)recordMining(sp);
  });
  BthCommands.register(this);
  ServerLifecycleEvents.SERVER_STARTED.register(server->{
   second=0;ticks=0;director.clear();lastPosition.clear();residence.clear();dimensions.clear();adminRequests.clear();overlays.clear();windows.clear();villages.clear();miningPlayers.clear();
   state=server.getWorldPath(LevelResource.ROOT).resolve("betterthanherobrine/memory.properties");
   try{MemoryStore.load(state,director,second);}catch(IOException e){LOG.error("BTH history unreadable; original retained",e);state=null;}
  });
  ServerTickEvents.END_SERVER_TICK.register(this::tick);
  ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->{
   UUID id=handler.player.getUUID();director.disconnect(id);placement.forget(id);miningPlayers.remove(id);lastPosition.remove(id);residence.remove(id);dimensions.remove(id);adminRequests.remove(id);overlays.remove(id);windows.remove(id);villages.remove(id);save();
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
  return new Context(second,level.getSkyDarken()>=4?18000L:6000L,!level.canSeeSky(p.blockPosition()),forest,level.isRaining(),level.isThundering(),
   residence.getOrDefault(p.getUUID(),0)>120,level.containsAnyLiquid(p.getBoundingBox().inflate(3)),
   level.getMaxLocalRawBrightness(p.blockPosition()),nearby,((((long)(p.blockPosition().getX() >> 4)) << 32) | ((p.blockPosition().getZ() >> 4) & 0xffffffffL)),p.getYRot(),moving,miningPlayers.remove(p.getUUID()),villages.getOrDefault(p.getUUID(),false),windows.containsKey(p.getUUID()),p.blockPosition().getY());
 }
 private void tick(MinecraftServer server){
  if(++ticks%20!=0)return;second++;Map<UUID,Context> contexts=new LinkedHashMap<>();
  for(ServerPlayer p:server.getPlayerList().getPlayers()){
   UUID id=p.getUUID();
   if(second%10==1){probe(p);}
   if(overlays.contains(id)&&second%2==0)sendAdminState(p,false,"",isAdmin(p));
   String dim=p.level().dimension().identifier().toString();
   String previous=dimensions.put(id,dim);
   if(previous!=null&&!previous.equals(dim)){placement.clear(id);director.disconnect(id);lastPosition.remove(id);residence.remove(id);windows.remove(id);villages.remove(id);miningPlayers.remove(id);}
   if(!p.isAlive()||p.isSpectator()||p.isCreative()){director.disconnect(id);placement.clear(id);continue;}
   Context c=context(p);contexts.put(id,c);
   residence.compute(id,(k,v)->c.moving()?0:Math.min(600,(v==null?0:v)+1));lastPosition.put(id,p.position());
  }
  for(var emitted:director.tick(contexts)){
   ServerPlayer player=server.getPlayerList().getPlayer(emitted.player());if(player!=null)emit(player,emitted.cue());
  }
  if(!config.enabled||config.intensity<=0)placement.clearAll();placement.tick();if(second%60==0)save();
 }
 private final Set<UUID> miningPlayers=new HashSet<>();
 public void recordMining(ServerPlayer p){miningPlayers.add(p.getUUID());}
 private void probe(ServerPlayer p){
  ServerLevel level=(ServerLevel)p.level();UUID id=p.getUUID();windows.remove(id);
  villages.put(id,level.isVillage(p.blockPosition()));
  // Search 243 nearby positions, once per ten seconds, without loading terrain.
  BlockPos center=p.blockPosition();
  for(int y=0;y<3;y++)for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++){
   BlockPos pos=center.offset(x,y,z);if(!level.hasChunkAt(pos))continue;
   var block=level.getBlockState(pos);
   if(block.is(Blocks.GLASS)||block.is(Blocks.GLASS_PANE)){windows.put(id,pos.immutable());return;}
  }
 }
 public BlockPos window(UUID player){return windows.get(player);}
 public boolean isAdmin(ServerPlayer player){
  return AdminPolicy.allowed(player.getGameProfile().name(),PermissionPredicates.require(id("command/admin"),PermissionLevel.GAMEMASTERS).test(player.createCommandSourceStack()));
 }
 public boolean adminAction(ServerPlayer p,AdminRequest.Action action,String event){
  if(!isAdmin(p)){sendAdminState(p,false,"",false);return false;}
  boolean ok=true;String details="";
  switch(action){
   case OPEN -> {}
   case SPAWN -> {
    if(!config.enabled||config.entityFrequency==0)ok=false;
    else{
     placement.clear(p.getUUID());
     Vec3 flatLook=Vec3.directionFromRotation(0,p.getYRot());
     ok=placement.spawn(p,p.position().add(flatLook.scale(24)),45,"observer");
     if(!ok)details="No visible, loaded ground found ahead. Try an open field.";
    }
   }
   case REMOVE -> placement.clearAll();
   case RANDOM -> {
    List<String> ids=director.eventIds();String chosen=ids.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(ids.size()));
    ok=director.force(p.getUUID(),chosen,context(p));details=chosen;
   }
   case EVENT -> {ok=director.force(p.getUUID(),event,context(p));details=event;}
   case NIGHTMARE -> {director.stop(p.getUUID());placement.clear(p.getUUID());ok=director.force(p.getUUID(),"distant_figure",context(p));}
   case DEBUG -> {if(!overlays.add(p.getUUID()))overlays.remove(p.getUUID());}
   case ENTITIES -> details=String.join(" | ",placement.describe());
   case TIMERS -> details=String.join(" | ",director.timers(second));
   case RESET -> {stopAll(p);director.clear();save();}
   case STOP -> stopAll(p);
  }
  if(!ok&&details.isEmpty())details="bth.busy";
  sendAdminState(p,action==AdminRequest.Action.OPEN,details,true);return ok;
 }
 public void stopAll(ServerPlayer caller){
  director.stopAll();placement.clearAll();
  var server=caller.level().getServer();
  for(ServerPlayer player:server.getPlayerList().getPlayers()){
   director.memory(player.getUUID()).quietUntil=second+180;
   emit(player,Cue.effect(0,Cue.Kind.CLEAR,0,0));
  }
 }
 private void sendAdminState(ServerPlayer p,boolean open,String details,boolean allowed){
  if(!ServerPlayNetworking.canSend(p,AdminState.TYPE))return;
  if(!allowed){overlays.remove(p.getUUID());}
  String summary=allowed?(details.isEmpty()?String.join(" | ",director.timers(second)):details):"bth.denied";
  if(summary.length()>4096)summary=summary.substring(0,4096);
  ServerPlayNetworking.send(p,new AdminState(allowed,open,allowed&&overlays.contains(p.getUUID()),allowed?placement.count():0,allowed?director.activeCount():0,summary));
 }
 /** Legacy /bth admin chaos now triggers a finite, distant sighting; no nightmare spam. */
 public void adminChaos(ServerPlayer player){adminAction(player,AdminRequest.Action.NIGHTMARE,"");}
 public void emit(ServerPlayer player,Cue cue){
  if(cue.kind()!=Cue.Kind.CLEAR&&!config.enabled)return;
  if(cue.kind()==Cue.Kind.ENTITY&&config.entityFrequency==0)return;
  if((cue.kind()==Cue.Kind.SOUND||cue.kind()==Cue.Kind.SILENCE)&&!config.atmosphericSounds)return;
  if((cue.kind()==Cue.Kind.DISTORT||cue.kind()==Cue.Kind.FOG||cue.kind()==Cue.Kind.TINT||cue.kind()==Cue.Kind.PARTICLES||cue.kind()==Cue.Kind.SYMBOL)&&!config.visualEffects)return;
  if(cue.kind()==Cue.Kind.TINT&&!config.screenEffects)return;
  if(cue.kind()==Cue.Kind.SYMBOL&&!config.worldAnomalies)return;
  Vec3 forward=Vec3.directionFromRotation(0,player.getYRot());Vec3 side=new Vec3(-forward.z,0,forward.x);
  Vec3 pos=player.position().add(forward.scale(cue.forward())).add(side.scale(cue.side())).add(0,cue.up(),0);
  if(cue.kind()==Cue.Kind.JOIN){player.sendSystemMessage(Component.translatable("bth.herobrine_joined"));return;}
  if(cue.kind()==Cue.Kind.VANISH){placement.clear(player.getUUID());return;}
  if(cue.kind()==Cue.Kind.ENTITY){placement.spawn(player,pos,cue.duration(),cue.resource());return;}
  if(cue.kind()==Cue.Kind.CLEAR)placement.clear(player.getUUID());
  if(ServerPlayNetworking.canSend(player,CuePayload.TYPE))
   ServerPlayNetworking.send(player,new CuePayload(player.level().dimension().identifier().toString(),cue.kind(),cue.resource(),pos.x,pos.y,pos.z,cue.strength(),cue.duration()));
 }
 public void save(){if(state!=null)try{MemoryStore.save(state,director,second);}catch(IOException e){LOG.error("Cannot save BTH history",e);}}
 public void saveConfig(){try{config.save(configPath());}catch(IOException e){throw new IllegalStateException("Cannot save BTH configuration",e);}}
}
