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
  check(Events.ALL.size()>=10,"At least ten complete, curated scenes");
  for(var event:Events.ALL)for(var cue:event.cues())
   check(cue.kind()!=Cue.Kind.JOIN&&cue.kind()!=Cue.Kind.TINT&&cue.kind()!=Cue.Kind.FOG&&cue.kind()!=Cue.Kind.DISTORT&&cue.kind()!=Cue.Kind.SILENCE,
    "No forced intro, intrusive screen effects or fake ambience suppression");
  for(String name:List.of("zaanw","ZAANW","za anw","another",""))for(boolean permission:List.of(true,false))
   check(AdminPolicy.allowed(name,permission)==(permission&&name.equalsIgnoreCase("zaanw")),"Server permission + exact name");
  for(int seed=0;seed<64;seed++){
   var opening=new HorrorDirector(new HorrorConfig(),new Random(seed));UUID newcomer=new UUID(4,seed);
   int emitted=0;
   for(int sec=0;sec<900;sec++){
    var flat=new Context(sec,6000,false,false,false,false,false,false,15,0,0,0,false,false,false,false,-60);
    for(var emission:opening.tick(Map.of(newcomer,flat))){
     check(sec>=100,"No scene during the first 100 seconds");
     check(emission.cue().kind()!=Cue.Kind.JOIN,"No fake Herobrine joined message");
     emitted++;
    }
   }
   check(emitted>0,"A quiet single-player world can eventually produce a clue");
  }
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
  check(small.force(id,"distant_figure",context(0)),"First slot");
  check(!small.force(new UUID(0,2),"footsteps",context(0)),"Global cap on commands");
  check(!small.force(id,"unknown",context(0)),"Unknown event rejected");
  small.disconnect(id);check(small.force(id,"footsteps",context(0)),"Slot reusable");
  cfg.entityFrequency=0;small.disconnect(id);check(!small.force(id,"distant_figure",context(0)),"No entity with frequency zero");
  var safety=new HorrorDirector(cfg,new Random(7));
  var safeMemory=safety.memory(id);safeMemory.introduced=true;safeMemory.quietUntil=420;
  for(int sec=0;sec<420;sec++)
   check(safety.tick(Map.of(id,context(sec))).isEmpty(),"Quiet period blocks natural events");
  check(safety.memory(id).encounters==0,"No encounters during quiet period");
  var adaptive=new HorrorDirector(cfg,new Random(7));cfg.entityFrequency=1;
  adaptive.memory(id).vigilance=1;
  check(adaptive.force(id,"distant_figure",context(0)),"Forced distant sighting");
  check(adaptive.tick(Map.of(id,context(0))).getFirst().cue().forward()>0,
   "Turning does not turn sightings into blind-side jumpscares");
  cfg.worldAnomalies=false;
  var protectedWorld=new HorrorDirector(cfg,new Random(1));
  check(!protectedWorld.force(id,"unknown_symbol",context(0)),"World anomaly setting enforced");
  cfg.worldAnomalies=true;
  var stopped=new HorrorDirector(cfg,new Random(3));
  check(stopped.force(id,"distant_figure",context(0)),"Admin-owned finite sequence starts");
  check(stopped.timers(5).size()==1,"Live timer reports active sequence");
  stopped.stopAll();check(stopped.activeCount()==0&&stopped.timers(5).isEmpty(),"Stop removes every scheduled stage");
  check(stopped.tick(Map.of(id,context(6))).isEmpty(),"Stopped sequence cannot emit delayed cues");
  stopped.reset(id);check(stopped.memory(id).age==0,"Reset clears player memory");
  var off=new HorrorConfig();off.intensity=0;
  var disabled=new HorrorDirector(off,new Random(4));
  for(int sec=0;sec<70;sec++)check(disabled.tick(Map.of(id,context(sec))).isEmpty(),"Zero intensity suppresses opening and random scenes");
  check(!disabled.force(id,"distant_figure",context(0)),"Zero intensity suppresses forced scenes");
  var dir=Files.createTempDirectory("bth-tests-");
  try{
   Path file=dir.resolve("memory.properties");MemoryStore.save(file,d,30001);
   var restored=new HorrorDirector(cfg,new Random(42));MemoryStore.load(file,restored,0);
   check(restored.memory(id).introduced==d.memory(id).introduced,"Persist introduction phase");
   check(restored.memory(id).age==d.memory(id).age,"Persist age");
   check(restored.memory(id).encounters==d.memory(id).encounters,"Persist encounters");
   check(restored.memory(id).lastEvent.equals(d.memory(id).lastEvent),"Persist repeat prevention");
   check(restored.memory(id).chunks.equals(d.memory(id).chunks),"Persist exploration");
   check(restored.memory(id).nextEvent>=0&&restored.memory(id).nextEvent<=3600,"Rebase cooldown across restart");
   Path settings=dir.resolve("config.properties");cfg.preset("Classic");cfg.save(settings);
   var loaded=HorrorConfig.load(settings);check(loaded.intensity==1&&loaded.graceSeconds==cfg.graceSeconds,"Classic preset persistence");
   loaded.enabled=false;loaded.volume=.23;loaded.reducedVisuals=true;loaded.save(settings);
   var preferences=HorrorConfig.load(settings);
   check(!preferences.enabled&&preferences.volume==.23&&preferences.reducedVisuals,"Loading never overwrites user preferences");
   double[] intensities={.55,1,1.25};int preset=0;
   for(String name:List.of("Subtle","Classic","Frequent")){preferences.preset(name);check(preferences.intensity==intensities[preset++],"Distinct classic presets");}
   loaded.intensity=Double.NaN;loaded.volume=2;loaded.validate();check(loaded.intensity==0&&loaded.volume==1,"Config clamps");
   Files.writeString(file,"format=1\n"+id+".age=oops\n");
   try{MemoryStore.load(file,restored,0);throw new AssertionError("Corrupt state accepted");}catch(java.io.IOException expected){checks++;}
  }finally{try(var walk=Files.walk(dir)){for(Path p:walk.sorted(Comparator.reverseOrder()).toList())Files.delete(p);}}
  var grace=new HorrorConfig();var quiet=new HorrorDirector(grace,new Random(42));
  for(long s=0;s<20;s++)check(quiet.tick(Map.of(id,context(s))).isEmpty(),"Grace period");
  System.out.println("PASS: "+checks+" checks; "+Events.ALL.size()+" scenes; 30,000-second simulation; persistence/config round trips.");
 }
}
