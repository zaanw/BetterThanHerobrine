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
 private final Map<UUID,WatcherEntity> watchers=new HashMap<>();
 public boolean spawn(ServerPlayer player,Vec3 requested,int duration){
  tick();
  if(watchers.size()>=BetterThanHerobrine.INSTANCE.config.maxActiveEvents)return false;
  WatcherEntity previous=watchers.get(player.getUUID());
  if(previous!=null&&!previous.isRemoved())return false;
  ServerLevel level=(ServerLevel)player.level();
  for(int attempt=0;attempt<24;attempt++){
   double a=attempt*2.399963, radius=attempt/4.0;
   BlockPos base=BlockPos.containing(requested.add(Math.cos(a)*radius,0,Math.sin(a)*radius));
   if(!level.hasChunkAt(base))continue;
   int y=level.canSeeSky(player.blockPosition())?level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,base.getX(),base.getZ()):base.getY();
   for(int dy=0;dy<7;dy++){
    BlockPos pos=new BlockPos(base.getX(),y+(dy%2==0?dy/2:-dy/2-1),base.getZ());
    if(!level.getWorldBorder().isWithinBounds(pos)||!level.hasChunkAt(pos)||!level.isEmptyBlock(pos)||!level.isEmptyBlock(pos.above()))continue;
    if(!level.getBlockState(pos.below()).isSolidRender())continue;
    var watcher=new WatcherEntity(BetterThanHerobrine.WATCHER,level);
    watcher.snapTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5);watcher.bind(player.getUUID(),duration);
    if(!level.noCollision(watcher)){watcher.discard();continue;}
    if(level.addFreshEntity(watcher)){watchers.put(player.getUUID(),watcher);return true;}
   }
  }
  return false;
 }
 public void clear(UUID player){var entity=watchers.remove(player);if(entity!=null)entity.discard();}
 public void clearAll(){new ArrayList<>(watchers.keySet()).forEach(this::clear);}
 public void tick(){watchers.values().removeIf(WatcherEntity::isRemoved);}
}
