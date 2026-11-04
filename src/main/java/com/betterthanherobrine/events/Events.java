package com.betterthanherobrine.events;
import com.betterthanherobrine.horror.*;
import java.util.*;
import java.util.function.Predicate;
import static com.betterthanherobrine.events.Cue.*;
import static com.betterthanherobrine.events.Cue.Kind.*;
import static com.betterthanherobrine.horror.Intensity.*;
/** Each entry is a complete bounded scene. No permanent block writes. */
public final class Events {
 private Events(){}
 private static HorrorEvent scene(String id,Intensity level,int cooldown,int weight,boolean rare,Predicate<Context> when,Cue... cues){
  return new HorrorEvent(id,level,cooldown,weight,rare,when,List.of(cues));
 }
 public static final List<HorrorEvent> ALL=List.of(
  scene("footsteps",UNEASY,180,12,false,Context::moving,
   sound(0,"step",-5,1,0,.35f),sound(2,"step",-4,-1,0,.3f),sound(5,"step",-3,0,0,.22f)),
  scene("empty_mine",UNEASY,240,9,false,Context::underground,
   sound(0,"stone",10,0,-4,.5f),sound(6,"stone",16,4,-6,.25f)),
  scene("window_knock",WATCHED,420,5,false,Context::nearBase,
   sound(0,"knock",0,5,1,.45f),sound(3,"knock",0,5,1,.22f)),
  scene("distant_door",UNEASY,300,8,false,c->!c.underground(),sound(0,"door",28,-12,0,.65f)),
  scene("breathing_wall",WATCHED,360,6,false,c->c.underground()&&c.light()<8,
   sound(0,"breath",0,-3,0,.25f),sound(7,"breath",0,-3,0,.18f),effect(1,TINT,.2f,12)),
  scene("forest_glimmer",WATCHED,420,5,false,Context::forest,
   new Cue(0,PARTICLES,"glimmer",18,6,1,.5f,16),watcher(8,26,9,18)),
  scene("rain_watcher",WATCHED,600,4,false,Context::rain,watcher(0,38,-12,35),sound(12,"whisper",-8,3,0,.2f)),
  scene("eyes_in_dark",WATCHED,360,7,false,c->c.light()<5,
   watcher(0,19,5,12),effect(3,TINT,.18f,7)),
  scene("cave_dust",UNEASY,240,8,false,c->c.underground()&&c.mining(),
   new Cue(0,PARTICLES,"dust",3,0,3,.45f,8),sound(2,"stone",0,3,4,.4f)),
  scene("shore_observer",WATCHED,600,4,false,Context::acrossWater,watcher(0,32,0,40)),
  scene("unknown_symbol",DISTURBED,900,3,false,c->!c.underground(),
   new Cue(0,SYMBOL,"cross",7,0,1,.5f,20),sound(10,"whisper",7,0,1,.2f)),
  scene("shadow_passage",DISTURBED,600,4,false,c->c.light()<10,
   effect(0,TINT,.45f,6),sound(2,"step",-2,0,0,.3f),new Cue(3,PARTICLES,"shadow",-2,0,1,.7f,5)),
  scene("the_watcher",WATCHED,3600,1,true,c->!c.underground(),watcher(0,42,10,180)),
  scene("false_safety",DISTURBED,2400,2,true,c->true,effect(0,CLEAR,0,240)),
  scene("the_forest",DISTURBED,2400,1,true,Context::forest,
   effect(0,FOG,.65f,70),sound(8,"breath",-12,4,0,.3f),watcher(20,32,-10,40),
   new Cue(35,SYMBOL,"cross",12,3,1,.3f,20),sound(52,"step",-5,0,0,.4f)),
  scene("the_visitor",DISTURBED,3600,1,true,c->c.nearBase()&&c.night(),
   sound(0,"knock",8,0,1,.5f),watcher(9,22,0,60),sound(45,"door",14,0,0,.3f)),
  scene("the_silence",WATCHED,1800,2,true,c->c.nearbyPlayers()==0,
   effect(0,SILENCE,.85f,35),sound(24,"breath",-2,0,0,.2f)),
  scene("the_encounter",NIGHTMARE,7200,1,true,c->c.night()&&c.thunder()&&c.nearbyPlayers()==0,
   effect(0,FOG,.75f,90),effect(0,SILENCE,.7f,80),watcher(12,16,0,60),
   sound(30,"whisper",-4,2,0,.3f),effect(45,TINT,.4f,18),new Cue(60,SYMBOL,"cross",8,0,1,.6f,20))
 );
 public static Optional<HorrorEvent> find(String id){return ALL.stream().filter(e->e.id().equals(id)).findFirst();}
}
