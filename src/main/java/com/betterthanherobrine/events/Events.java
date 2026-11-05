package com.betterthanherobrine.events;
import com.betterthanherobrine.horror.*;
import java.util.*;
import java.util.function.Predicate;
import static com.betterthanherobrine.events.Cue.*;
import static com.betterthanherobrine.events.Cue.Kind.*;
import static com.betterthanherobrine.horror.Intensity.*;
/**
 * Classic encounters. Nothing here changes blocks, flashes the screen or sends chat messages.
 * Every scene is optional, bounded, and deliberately small.
 */
public final class Events {
 private Events(){}
 private static HorrorEvent scene(String id,Intensity level,int cooldown,int weight,boolean rare,Predicate<Context> when,Cue... cues){
  return new HorrorEvent(id,level,cooldown,weight,rare,when,List.of(cues));
 }
 public static final List<HorrorEvent> ALL=List.of(
  scene("footsteps",NORMAL,420,12,false,c->c.moving()&&c.nearbyPlayers()==0,
   sound(0,"step",-7,2,0,.26f),sound(4,"step",-5,-2,0,.19f)),
  scene("cave_echo",NORMAL,520,10,false,Context::underground,
   sound(0,"stone",12,1,-3,.35f),sound(8,"step",9,-3,0,.15f)),
  scene("forest_rustle",NORMAL,480,10,false,Context::forest,
   sound(0,"rustle",13,-6,1,.27f),sound(7,"rustle",20,4,1,.16f)),
  scene("distant_door",NORMAL,780,6,false,c->c.village()&&!c.underground(),
   sound(0,"door",23,-11,0,.34f)),
  scene("stolen_rhythm",NORMAL,540,7,false,Context::mining,
   sound(0,"knock",-5,2,0,.27f),sound(5,"knock",-5,2,0,.20f)),
  scene("distant_figure",NORMAL,850,5,false,c->!c.underground()&&c.nearbyPlayers()==0,
   actor(0,"observer",34,6,42)),
  scene("tree_line",UNEASY,1050,4,false,Context::forest,
   sound(0,"rustle",20,9,1,.18f),actor(9,"observer",31,-10,40)),
  scene("cave_visitor",UNEASY,1100,3,false,c->c.underground()&&c.light()<9,
   sound(0,"stone",11,-4,0,.22f),actor(8,"observer",19,2,32)),
  scene("the_window",UNEASY,1500,3,false,c->c.night()&&c.window(),
   sound(0,"knock",7,1,1,.27f),actor(6,"window",9,0,25)),
  scene("the_visitor",WATCHED,1900,2,true,c->c.nearBase()&&c.night(),
   sound(0,"knock",12,2,1,.23f),actor(14,"observer",27,6,47)),
  scene("the_return",WATCHED,2000,2,true,c->!c.underground()&&c.nearbyPlayers()==0,
   actor(0,"observer",30,-7,16),effect(17,VANISH,0,0),
   sound(25,"step",-9,-5,0,.18f),actor(33,"observer",33,8,27)),
  scene("unknown_symbol",DISTURBED,2400,1,true,c->c.forest()&&c.nearbyPlayers()==0,
   new Cue(0,SYMBOL,"cross",16,5,1,.24f,16))
 );
 public static Optional<HorrorEvent> find(String id){return ALL.stream().filter(e->e.id().equals(id)).findFirst();}
}
