package com.betterthanherobrine;
import com.betterthanherobrine.config.*;
import com.betterthanherobrine.horror.*;
import com.betterthanherobrine.events.*;
import java.nio.file.*;
import java.util.*;
public final class DirectorTest {
 static int checks;
 static void check(boolean ok,String label){checks++;if(!ok)throw new AssertionError(label);}
 static Context context(long n){return new Context(n,18000,true,true,true,true,true,true,0,0,n%1024,(float)(n*137%360),true,true);}
 public static void main(String[] args)throws Exception{
  var cfg=new HorrorConfig();cfg.graceSeconds=0;cfg.intensity=2;
  var d=new HorrorDirector(cfg,new Random(42));UUID id=new UUID(0,1);
  Set<String> ids=new HashSet<>();for(var e:Events.ALL){check(ids.add(e.id()),"Unique ID "+e.id());check(e.duration()<=600,"Bounded scene");}
  // Test every stage, not just every event name. Force scenes and check exact cue delivery once.
  for(var e:Events.ALL){
   var one=new HorrorDirector(cfg,new Random(10));check(one.force(id,e.id(),context(0)),"Force "+e.id());
   List<Cue> got=new ArrayList<>();
   for(int second=0;second<=e.duration();second++) {
    var c=context(second);var emissions=one.tick(Map.of(id,c));for(var emission:emissions)got.add(emission.cue());
    one.memory(id).nextEvent=99999;
   }
   check(got.size()==e.cues().size(),"Every cue exactly once: "+e.id());
   check(one.activeCount()==0,"Scene releases capacity "+e.id());
  }
  for(long n=0;n<30000;n++){
   d.tick(Map.of(id,context(n)));check(d.activeCount()<=cfg.maxActiveEvents,"Scene cap");
   check(Double.isFinite(d.memory(id).tension),"Finite tension");
  }
  check(d.memory(id).encounters>20,"Natural survival scheduling");
  check(d.memory(id).chunks.size()==256,"Bounded exploration history");
  cfg.enabled=false;check(d.tick(Map.of(id,context(30001))).isEmpty()&&d.activeCount()==0,"Disable clears scenes");
  cfg.enabled=true;d.disconnect(id);check(d.activeCount()==0,"Disconnect cleanup");
  var small=new HorrorDirector(cfg,new Random(1));cfg.maxActiveEvents=1;
  check(small.force(id,"the_watcher",context(0)),"First slot");
  check(!small.force(new UUID(0,2),"footsteps",context(0)),"Global cap on commands");
  check(!small.force(id,"unknown",context(0)),"Unknown event rejected");
  small.disconnect(id);check(small.force(id,"footsteps",context(0)),"Slot reusable");
  cfg.entityFrequency=0;small.disconnect(id);check(!small.force(id,"the_watcher",context(0)),"No entity with frequency zero");
  var safety=new HorrorDirector(cfg,new Random(7));
  check(safety.force(id,"false_safety",context(0)),"False safety starts");
  for(int s=0;s<420;s++){
   var outputs=safety.tick(Map.of(id,context(s)));
   if(s>0)check(outputs.isEmpty(),"False safety suppresses events");
  }
  check(safety.memory(id).encounters==1,"No natural events in false safety");
  var adaptive=new HorrorDirector(cfg,new Random(7));cfg.entityFrequency=1;
  adaptive.memory(id).vigilance=1;
  check(adaptive.force(id,"the_watcher",context(0)),"Adaptive watcher starts");
  check(adaptive.tick(Map.of(id,context(0))).getFirst().cue().forward()<0,"Frequent turns move watcher to blind side");
  cfg.worldAnomalies=false;
  var protectedWorld=new HorrorDirector(cfg,new Random(1));
  check(!protectedWorld.force(id,"unknown_symbol",context(0)),"World anomaly setting enforced");
  cfg.worldAnomalies=true;
  var dir=Files.createTempDirectory("bth-tests-");
  try{
   Path file=dir.resolve("memory.properties");MemoryStore.save(file,d,30001);
   var restored=new HorrorDirector(cfg,new Random(42));MemoryStore.load(file,restored,0);
   check(restored.memory(id).age==d.memory(id).age,"Persist age");
   check(restored.memory(id).encounters==d.memory(id).encounters,"Persist encounters");
   check(restored.memory(id).lastEvent.equals(d.memory(id).lastEvent),"Persist repeat prevention");
   check(restored.memory(id).chunks.equals(d.memory(id).chunks),"Persist exploration");
   check(restored.memory(id).nextEvent>=0&&restored.memory(id).nextEvent<=3600,"Rebase cooldown across restart");
   Path settings=dir.resolve("config.properties");cfg.preset("Nightmare");cfg.save(settings);
   var loaded=HorrorConfig.load(settings);check(loaded.intensity==1.75,"Preset persistence");
   loaded.intensity=Double.NaN;loaded.volume=2;loaded.validate();check(loaded.intensity==0&&loaded.volume==1,"Config clamps");
   Files.writeString(file,"format=1\n"+id+".age=oops\n");
   try{MemoryStore.load(file,restored,0);throw new AssertionError("Corrupt state accepted");}catch(java.io.IOException expected){checks++;}
  }finally{try(var walk=Files.walk(dir)){for(Path p:walk.sorted(Comparator.reverseOrder()).toList())Files.delete(p);}}
  var grace=new HorrorConfig();var quiet=new HorrorDirector(grace,new Random(42));
  for(long s=0;s<grace.graceSeconds;s++)check(quiet.tick(Map.of(id,context(s))).isEmpty(),"Grace period");
  System.out.println("PASS: "+checks+" checks; "+Events.ALL.size()+" scenes; 30,000-second simulation; persistence/config round trips.");
 }
}
