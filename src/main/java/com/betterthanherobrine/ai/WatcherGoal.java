package com.betterthanherobrine.ai;
import com.betterthanherobrine.entity.WatcherEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;
/** Watches, retreats on approach and walks to cover when watched. Never teleports or attacks. */
public final class WatcherGoal extends Goal {
 private final WatcherEntity entity;
 private int noticed,steps;
 public WatcherGoal(WatcherEntity entity){this.entity=entity;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
 @Override public boolean canUse(){return entity.observer()!=null;}
 @Override public void tick(){
  if(++steps%5!=0)return;
  Player p=entity.level().getPlayerByUUID(entity.observer());
  if(p==null||!p.isAlive()||p.isSpectator()){entity.discard();return;}
  entity.getLookControl().setLookAt(p,12,12);
  double distance=entity.distanceToSqr(p);
  Vec3 toward=entity.position().subtract(p.getEyePosition()).normalize();
  boolean watched=p.getLookAngle().dot(toward)>.96&&p.hasLineOfSight(entity);
  if(watched)noticed+=5;else noticed=Math.max(0,noticed-2);
  if(distance<64||noticed>entity.watchPatience()){entity.discard();return;}
  if(watched&&noticed>=20&&steps%20==0){
   Vec3 away=entity.position().subtract(p.position()).normalize();
   // A short path request uses vanilla collision/navigation and stays in already loaded terrain.
   Vec3 cover=entity.position().add(away.scale(5)).add(-away.z*3,0,away.x*3);
   if(entity.level().hasChunkAt(net.minecraft.core.BlockPos.containing(cover)))
    entity.getNavigation().moveTo(cover.x,cover.y,cover.z,.7);
  }
 }
 @Override public boolean requiresUpdateEveryTick(){return true;}
}
