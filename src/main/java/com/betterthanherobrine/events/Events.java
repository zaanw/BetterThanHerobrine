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
   sound(30,"whisper",-4,2,0,.3f),effect(45,TINT,.4f,18),new Cue(60,SYMBOL,"cross",8,0,1,.6f,20)),
  scene("the_observer",UNEASY,480,7,false,c->!c.underground(),
   actor(0,"observer",25,5,32),sound(14,"rustle",23,6,0,.2f)),
  scene("the_window",WATCHED,720,5,false,c->c.night()&&c.window(),
   sound(0,"knock",6,0,1,.25f),actor(3,"window",9,0,30),sound(19,"rustle",9,0,0,.2f)),
  scene("silent_village",WATCHED,1000,4,false,Context::village,
   effect(0,SILENCE,.7f,40),actor(10,"observer",24,-7,22),sound(28,"door",-20,0,0,.3f)),
  scene("doppelganger",DISTURBED,1200,3,false,c->!c.underground()&&c.nearbyPlayers()==0,
   actor(0,"doppelganger",28,12,45),sound(13,"step",28,12,0,.3f),effect(26,DISTORT,.18f,5)),
  scene("cave_visitor",WATCHED,700,6,false,c->c.underground()&&c.height()<24,
   sound(0,"rustle",18,0,-1,.35f),sound(8,"drone",18,0,0,.2f),actor(13,"approach",18,0,40)),
  scene("the_return",WATCHED,1100,3,false,c->true,
   actor(0,"observer",24,12,14),effect(12,VANISH,0,0),sound(21,"step",-12,-8,0,.3f),actor(27,"observer",-22,-9,25)),
  scene("the_approach",DISTURBED,1000,3,false,c->c.nearbyPlayers()==0,
   actor(0,"approach",30,1,55),sound(16,"breath",12,1,0,.2f)),
  scene("stolen_rhythm",UNEASY,400,8,false,c->c.mining(),
   sound(0,"knock",-6,1,0,.3f),sound(3,"knock",-6,1,0,.25f),sound(9,"knock",-6,1,0,.3f),new Cue(10,PARTICLES,"dust",-6,1,1,.3f,6)),
  scene("held_breath",WATCHED,600,6,false,c->!c.moving()&&c.nearbyPlayers()==0,
   effect(0,SILENCE,.5f,20),sound(6,"breath",-1,0,1,.15f),effect(10,DISTORT,.15f,7)),
  scene("hollow_ground",UNEASY,420,6,false,c->!c.underground(),
   sound(0,"stone",0,0,-5,.35f),sound(7,"drone",1,0,-8,.2f),new Cue(9,PARTICLES,"dust",0,0,0,.3f,8)),
  scene("the_threshold",WATCHED,650,5,false,c->c.nearBase(),
   sound(0,"door",8,0,0,.3f),actor(6,"observer",12,4,22),effect(18,VANISH,0,0)),
  scene("the_haunting",NORMAL,600,1,false,c->true,
   sound(0,"step",-4,1,0,.45f),sound(3,"knock",5,0,0,.35f),
   new Cue(7,JOIN,"",0,0,0,0,0),sound(11,"rustle",-7,-3,0,.3f),actor(17,"observer",21,-7,22))
 );
 public static Optional<HorrorEvent> find(String id){return ALL.stream().filter(e->e.id().equals(id)).findFirst();}
}
