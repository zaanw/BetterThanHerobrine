package com.betterthanherobrine.config;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/** Local preferences and server policy use different files. No network credentials or personal data. */
public final class HorrorConfig {
 public boolean enabled=true, worldAnomalies=true, atmosphericSounds=true, visualEffects=true,
  screenEffects=true, reducedVisuals=false, performanceMode=false, debug=false;
 public double intensity=1, entityFrequency=1, fogIntensity=.2, volume=.55;
 public int maxActiveEvents=4, graceSeconds=60;
 public void preset(String name) {
  switch(name){
   case "Subtle" -> {intensity=.55;entityFrequency=.7;fogIntensity=.1;volume=.4;}
   case "Frequent" -> {intensity=1.25;entityFrequency=1.35;fogIntensity=.3;volume=.7;}
   default -> {intensity=1;entityFrequency=1;fogIntensity=.2;volume=.55;}
  }
 }
 public void validate() {
  intensity=bounded(intensity,0,2); entityFrequency=bounded(entityFrequency,0,2);
  fogIntensity=bounded(fogIntensity,0,1); volume=bounded(volume,0,1);
  maxActiveEvents=Math.clamp(maxActiveEvents,1,16); graceSeconds=Math.clamp(graceSeconds,0,7200);
 }
 private static double bounded(double n,double lo,double hi) { return Double.isFinite(n)?Math.clamp(n,lo,hi):lo; }
 public static HorrorConfig load(Path file) throws IOException {
  var c=new HorrorConfig(); if (!Files.exists(file)) { c.save(file); return c; }
  var p=new Properties(); try(var in=Files.newInputStream(file)){p.load(in);}
  for(var f:HorrorConfig.class.getFields()) try {
   String s=p.getProperty(f.getName()); if(s==null)continue;
   if(f.getType()==boolean.class) f.setBoolean(c,Boolean.parseBoolean(s));
   if(f.getType()==double.class) f.setDouble(c,Double.parseDouble(s));
   if(f.getType()==int.class) f.setInt(c,Integer.parseInt(s));
  } catch(ReflectiveOperationException|NumberFormatException e) { throw new IOException("Invalid config field "+f.getName(),e); }
  c.validate();return c;
 }
 public void save(Path file) throws IOException {
  validate();var p=new Properties();
  for(var f:HorrorConfig.class.getFields()) try { p.setProperty(f.getName(),String.valueOf(f.get(this))); }
  catch(IllegalAccessException e){throw new IOException(e);}
  Files.createDirectories(file.toAbsolutePath().getParent());
  Path temp=Files.createTempFile(file.toAbsolutePath().getParent(),"bth-",".tmp");
  try {try(var out=Files.newOutputStream(temp)){p.store(out,"BetterThanHerobrine");}
   try {Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
   catch(AtomicMoveNotSupportedException e){Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING);}
  }finally{Files.deleteIfExists(temp);}
 }
}
