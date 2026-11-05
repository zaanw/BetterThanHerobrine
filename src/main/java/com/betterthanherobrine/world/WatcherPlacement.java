package com.betterthanherobrine.world;
import com.betterthanherobrine.entity.WatcherEntity;
import com.betterthanherobrine.core.BetterThanHerobrine;
import com.betterthanherobrine.horror.SightingGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Spawn one short-lived observer per player on already loaded, safe ground.
 * Prefer a clear, forward sightline so successful spawns are actually visible.
 * Nothing touches player blocks, saved chunks, inventories, or terrain.
 */
public final class WatcherPlacement {
 private final Map<UUID,BlockPos> lastSpots=new HashMap<>();
 private final Map<UUID,WatcherEntity> watchers=new HashMap<>();
 public boolean spawn(ServerPlayer player,Vec3 requested,int duration){return spawn(player,requested,duration,"watcher");}
 public boolean spawn(ServerPlayer player,Vec3 requested,int duration,String mode){
  final boolean window=mode.equals("window");
  if(window){
   BlockPos glass=BetterThanHerobrine.INSTANCE.window(player.getUUID());
   if(glass==null||Vec3.atCenterOf(glass).distanceToSqr(player.position())>100)return false;
   Vec3 outward=Vec3.atCenterOf(glass).subtract(player.position()).multiply(1,0,1);
   if(outward.lengthSqr()<1e-4)return false;
   requested=Vec3.atCenterOf(glass).add(outward.normalize().scale(1.5)).add(0,-1,0);
  }
  tick();
  if(watchers.size()>=BetterThanHerobrine.INSTANCE.config.maxActiveEvents)return false;
  WatcherEntity previous=watchers.get(player.getUUID());
  if(previous!=null&&!previous.isRemoved())return false;
  ServerLevel level=(ServerLevel)player.level();
  // Window encounters require precision. Other encounters search loaded ground
  // within ~11 blocks of the intended position, prioritising player-visible spots.
  List<SightingGeometry.Offset> offsets=window?List.of(new SightingGeometry.Offset(0,0)):
   SightingGeometry.offsets(ThreadLocalRandom.current());
  Vec3 eye=player.getEyePosition();
  for(var offset:offsets){
   BlockPos base=BlockPos.containing(requested.add(offset.x(),0,offset.z()));
   if(!level.hasChunkAt(base))continue;
   int groundY=!window&&level.canSeeSky(player.blockPosition())?
    level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,base.getX(),base.getZ()):base.getY();
   for(int dy=0;dy<(window?4:7);dy++){
    int lift=dy==0?0:(dy%2==1?(dy+1)/2:-dy/2);
    BlockPos pos=new BlockPos(base.getX(),groundY+lift,base.getZ());
    if(pos.equals(lastSpots.get(player.getUUID())))continue;
    if(!level.getWorldBorder().isWithinBounds(pos)||!level.hasChunkAt(pos)
       ||!level.isEmptyBlock(pos)||!level.isEmptyBlock(pos.above()))continue;
    if(!level.getBlockState(pos.below()).isSolidRender())continue;
    Vec3 target=new Vec3(pos.getX()+.5,pos.getY()+1.4,pos.getZ()+.5);
    if(window){
     BlockPos glass=BetterThanHerobrine.INSTANCE.window(player.getUUID());
     if(glass==null)continue;
     var hit=level.clip(new ClipContext(eye,target,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
     if(!hit.getBlockPos().equals(glass))continue;
    }else{
     Vec3 diff=target.subtract(eye);
     if(Math.abs(pos.getY()-player.getY())>12||diff.lengthSqr()<100||diff.lengthSqr()>4096)continue;
     if(!SightingGeometry.inFront(player.getYRot(),diff.x,diff.z))continue;
     // Without this test the director can claim a successful Herobrine
     // encounter even though he was hidden behind a hill or house.
     var hit=level.clip(new ClipContext(eye,target,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
     if(hit.getType()!=HitResult.Type.MISS)continue;
    }
    var watcher=new WatcherEntity(BetterThanHerobrine.HEROBRINE,level);
    watcher.snapTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5);
    watcher.bind(player.getUUID(),duration);watcher.behavior(mode);
    if(!level.noCollision(watcher)){watcher.discard();continue;}
    if(level.addFreshEntity(watcher)){
     watchers.put(player.getUUID(),watcher);
     lastSpots.put(player.getUUID(),pos);
     return true;
    }
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
