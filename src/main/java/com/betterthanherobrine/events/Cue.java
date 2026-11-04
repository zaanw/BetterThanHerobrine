package com.betterthanherobrine.events;
/** Executable, bounded stage. Position is relative to the player location when the stage fires. */
public record Cue(int delay, Kind kind, String resource, double forward, double side, double up,
 float strength, int duration) {
 public enum Kind { SOUND, ENTITY, FOG, TINT, SILENCE, PARTICLES, SYMBOL, CLEAR, JOIN, VANISH, DISTORT }
 public Cue {
  if(delay<0||delay>600||duration<0||duration>600||!Float.isFinite(strength)||strength<0||strength>1)
   throw new IllegalArgumentException("Invalid cue");
 }
 public static Cue sound(int at,String id,double f,double s,double y,float volume){
  return new Cue(at,Kind.SOUND,id,f,s,y,volume,4);
 }
 public static Cue effect(int at,Kind kind,float strength,int duration){return new Cue(at,kind,"",0,0,0,strength,duration);}
 public static Cue actor(int at,String mode,double f,double side,int seconds){return new Cue(at,Kind.ENTITY,mode,f,side,0,1,seconds);}
 public static Cue watcher(int at,double f,double side,int seconds){return new Cue(at,Kind.ENTITY,"watcher",f,side,0,1,seconds);}
}
