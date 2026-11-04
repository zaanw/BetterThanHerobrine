package com.betterthanherobrine.horror;
/** Snapshot collected only on the server thread. Time is monotonic seconds, not wall clock. */
public record Context(long second, long dayTime, boolean underground, boolean forest, boolean rain,
 boolean thunder, boolean nearBase, boolean acrossWater, int light, int nearbyPlayers,
 long chunk, float yaw, boolean moving, boolean mining, boolean village, boolean window, int height) {
 public Context(long second,long dayTime,boolean underground,boolean forest,boolean rain,boolean thunder,
 boolean nearBase,boolean acrossWater,int light,int nearbyPlayers,long chunk,float yaw,boolean moving,boolean mining){
  this(second,dayTime,underground,forest,rain,thunder,nearBase,acrossWater,light,nearbyPlayers,chunk,yaw,moving,mining,false,false,64);
 }
 public boolean night(){ long t=Math.floorMod(dayTime,24000);return t>13000&&t<23000; }
}
