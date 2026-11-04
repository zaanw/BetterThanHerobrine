package com.betterthanherobrine.horror;
import java.io.*;
import java.nio.file.*;
import java.util.*;
/** Versioned local state; use only inside the current Minecraft world directory. */
public final class MemoryStore {
 private MemoryStore(){}
 public static void save(Path file,HorrorDirector director,long now)throws IOException{
  Properties p=new Properties();p.setProperty("format","1");
  for(var e:director.memories().entrySet()){
   String k=e.getKey()+".";PlayerMemory m=e.getValue();
   p.setProperty(k+"introduced",""+m.introduced);
   p.setProperty(k+"recent",String.join(",",m.recentEvents));
   p.setProperty(k+"age",""+m.age);p.setProperty(k+"tension",""+m.tension);
   p.setProperty(k+"encounters",""+m.encounters);p.setProperty(k+"last",m.lastEvent);
   p.setProperty(k+"quiet",""+Math.max(0,m.quietUntil-now));
   p.setProperty(k+"next",""+Math.max(0,m.nextEvent-now));
   p.setProperty(k+"chunks",m.chunks.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")));
   m.cooldowns.forEach((event,until)->p.setProperty(k+"cooldown."+event,""+Math.max(0,until-now)));
  }
  Files.createDirectories(file.getParent());Path temp=Files.createTempFile(file.getParent(),"bth-",".tmp");
  try{try(var out=Files.newOutputStream(temp)){p.store(out,"BetterThanHerobrine local director memory");}
   try{Files.move(temp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
   catch(AtomicMoveNotSupportedException e){Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING);}
  }finally{Files.deleteIfExists(temp);}
 }
 public static void load(Path file,HorrorDirector director,long now)throws IOException{
  if(!Files.exists(file))return;Properties p=new Properties();try(var in=Files.newInputStream(file)){p.load(in);}
  if(!p.getProperty("format","").equals("1"))throw new IOException("Unknown BTH memory format");
  Map<UUID,PlayerMemory> loaded=new HashMap<>();
  try{for(String key:p.stringPropertyNames())if(key.endsWith(".age")){
   UUID id=UUID.fromString(key.substring(0,key.length()-4));String k=id+".";var m=new PlayerMemory();
   m.introduced=Boolean.parseBoolean(p.getProperty(k+"introduced","false"));
   for(String event:p.getProperty(k+"recent","").split(","))if(!event.isBlank()&&m.recentEvents.size()<4)m.recentEvents.addLast(event);
   m.age=Math.max(0,Long.parseLong(p.getProperty(k+"age")));
   m.tension=Math.clamp(Double.parseDouble(p.getProperty(k+"tension","0")),0,99);
   if(!Double.isFinite(m.tension))throw new NumberFormatException("non-finite tension");
   m.encounters=Math.max(0,Integer.parseInt(p.getProperty(k+"encounters","0")));m.lastEvent=p.getProperty(k+"last","");
   m.nextEvent=now+Math.clamp(Long.parseLong(p.getProperty(k+"next","0")),0,3600);
   m.quietUntil=now+Math.clamp(Long.parseLong(p.getProperty(k+"quiet","0")),0,3600);
   for(String s:p.getProperty(k+"chunks","").split(","))if(!s.isBlank()&&m.chunks.size()<256)m.chunks.add(Long.parseLong(s));
   for(String field:p.stringPropertyNames())if(field.startsWith(k+"cooldown."))
    m.cooldowns.put(field.substring((k+"cooldown.").length()),now+Math.clamp(Long.parseLong(p.getProperty(field)),0,7200));
   loaded.put(id,m);
  }}catch(IllegalArgumentException e){throw new IOException("Invalid BTH memory",e);}
  loaded.forEach(director::restore);
 }
}
