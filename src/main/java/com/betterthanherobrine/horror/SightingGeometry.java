package com.betterthanherobrine.horror;
import java.util.*;
import java.util.random.RandomGenerator;

/** Minecraft-independent sightline planning math, exercised by the core test suite. */
public final class SightingGeometry {
 private SightingGeometry(){}
 public record Offset(double x,double z){}
 /** Search the intended sighting spot first, then nearby ground, without scanning chunks. */
 public static List<Offset> offsets(RandomGenerator random){
  List<Offset> result=new ArrayList<>();
  result.add(new Offset(0,0));
  for(int ring=1;ring<=4;ring++){
   double radius=ring*2.8;
   double phase=random.nextDouble()*2*Math.PI;
   for(int point=0;point<12;point++){
    double angle=phase+point*Math.PI/6;
    result.add(new Offset(Math.cos(angle)*radius,Math.sin(angle)*radius));
   }
  }
  return List.copyOf(result);
 }
 /** Minecraft yaw convention: 0 = south (+Z), 90 = west (-X). */
 public static boolean inFront(double yaw,double dx,double dz){
  double length=Math.hypot(dx,dz);
  if(length<1e-6)return false;
  double radians=Math.toRadians(yaw);
  double front=-Math.sin(radians)*dx+Math.cos(radians)*dz;
  // 78-degree half-cone. The silhouette is not spawned arbitrarily behind the player.
  return front>=.20*length;
 }
}
