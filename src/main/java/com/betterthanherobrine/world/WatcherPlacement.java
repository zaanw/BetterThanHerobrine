package com.betterthanherobrine.world;
import com.betterthanherobrine.entity.WatcherEntity;
import com.betterthanherobrine.core.BetterThanHerobrine;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.*;
/** Bounded placement. Never loads chunks or edits terrain; one observer per player. */
public final class WatcherPlacement {
 private final Map<UUID,BlockPos> lastSpots=new HashMap<>();
 private final Map<UUID,WatcherEntity> watchers=new HashMap<>();
 public boolean spawn(ServerPlayer player,Vec3 requested,int duration){return spawn(player,requested,duration,"watcher");}
 public boolean spawn(ServerPlayer player,Vec3 requested,int duration,String mode){
  if(mode.equals("window")){
   BlockPos glass=BetterThanHerobrine.INSTANCE.window(player.getUUID());
   if(glass==null||Vec3.atCenterOf(glass).distanceToSqr(player.position())>100)return false;
   Vec3 away=Vec3.atCenterOf(glass).subtract(player.position()).multiply(1,0,1).normalize();
   requested=Vec3.atCenterOf(glass).add(away.scale(1.5)).add(0,-1,0);
  }
  tick();
  if(watchers.size()>=BetterThanHerobrine.INSTANCE.config.maxActiveEvents)return false;
  WatcherEntity previous=watchers.get(player.getUUID());
  if(previous!=null&&!previous.isRemoved())return false;
  ServerLevel level=(ServerLevel)player.level();
  for(int attempt=0;attempt<24;attempt++){
   double a=attempt*2.399963, radius=attempt/4.0;
   BlockPos base=BlockPos.containing(requested.add(Math.cos(a)*radius,0,Math.sin(a)*radius));
   if(!level.hasChunkAt(base))continue;
   int y=!mode.equals("window")&&level.canSeeSky(player.blockPosition())?level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,base.getX(),base.getZ()):base.getY();
   for(int dy=0;dy<7;dy++){
    BlockPos pos=new BlockPos(base.getX(),y+(dy%2==0?dy/2:-dy/2-1),base.getZ());
    if(pos.equals(lastSpots.get(player.getUUID())))continue;
    if(!level.getWorldBorder().isWithinBounds(pos)||!level.hasChunkAt(pos)||!level.isEmptyBlock(pos)||!level.isEmptyBlock(pos.above()))continue;
    if(!level.getBlockState(pos.below()).isSolidRender())continue;
    if(mode.equals("window")){
     var hit=level.clip(new net.minecraft.world.level.ClipContext(player.getEyePosition(),Vec3.atCenterOf(pos.above()),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,player));
     if(!hit.getBlockPos().equals(BetterThanHerobrine.INSTANCE.window(player.getUUID())))continue;
    }
    var watcher=new WatcherEntity(BetterThanHerobrine.HEROBRINE,level);
    watcher.snapTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5);watcher.bind(player.getUUID(),duration);watcher.behavior(mode);
    if(!level.noCollision(watcher)){watcher.discard();continue;}
    if(level.addFreshEntity(watcher)){watchers.put(player.getUUID(),watcher);lastSpots.put(player.getUUID(),pos);return true;}
   }
  }
  return false;
 }
 public void forget(UUID player){clear(player);lastSpots.remove(player);}
 public void clear(UUID player){var entity=watchers.remove(player);if(entity!=null)entity.discard();}
 public int count(){tick();return watchers.size();}
 public List<String> describe(){return watchers.values().stream().filter(w->!w.isRemoved()).map(w->w.getUUID()+" "+w.blockPosition()+" "+w.behavior()).toList();}
 public void clearAll(){new ArrayList<>(watchers.keySet()).forEach(this::clear);}
 public void tick(){watchers.values().removeIf(WatcherEntity::isRemoved);}
}
