package com.betterthanherobrine.horror;
import java.util.*;
/** Bounded per-player memory. UUIDs stay on the local server. */
public final class PlayerMemory {
 public long age, nextEvent=0, quietUntil=0;
 public double tension=0, vigilance=0;
 public int encounters=0;
 public float lastYaw;
 public String lastEvent="";
 public final Map<String,Long> cooldowns=new HashMap<>();
 public final LinkedHashSet<Long> chunks=new LinkedHashSet<>();
 public void observe(Context c){
  age++;
  double turn=Math.abs(((c.yaw()-lastYaw+540)%360)-180);lastYaw=c.yaw();
  vigilance=vigilance*.95+(turn>90?.05:0);
  chunks.remove(c.chunk());chunks.add(c.chunk());
  if(chunks.size()>256)chunks.remove(chunks.iterator().next());
 }
}
