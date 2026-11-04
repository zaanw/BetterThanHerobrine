package com.betterthanherobrine.entity;
import com.betterthanherobrine.ai.WatcherGoal;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import java.util.UUID;
/** Non-combat observer. No natural spawning, item pickup, attacks, or permanent persistence. */
public final class WatcherEntity extends Zombie {
 private UUID observer;
 private int expiresAt;
 private int watchPatience=100;
 public WatcherEntity(EntityType<? extends Zombie> type,Level level){
  super(type,level);setCanPickUpLoot(false);setInvulnerable(true);setSilent(true);expiresAt=200;
 }
 public int watchPatience(){return watchPatience;}
 public void bind(UUID player,int seconds){watchPatience=seconds>=120?seconds*20:100;observer=player;expiresAt=tickCount+Math.clamp(seconds,1,180)*20;}
 public UUID observer(){return observer;}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new WatcherGoal(this));}
 @Override protected boolean isSunSensitive(){return false;}
 @Override public void tick(){
  super.tick();
  if(!level().isClientSide()&&(observer==null||tickCount>=expiresAt))discard();
 }
}
