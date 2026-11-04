package com.betterthanherobrine.horror;
import com.betterthanherobrine.config.HorrorConfig;
import com.betterthanherobrine.events.*;
import java.util.*;
import java.util.random.RandomGenerator;
/** Server authoritative scheduler. Call exactly once per second on the game thread. */
public final class HorrorDirector {
 public record Emission(UUID player,Cue cue){}
 private record Scene(UUID player,HorrorEvent event,long start,Set<Integer> fired){}
 private final Map<UUID,PlayerMemory> memories=new HashMap<>();
 private final List<Scene> active=new ArrayList<>();
 private final RandomGenerator random;
 public final HorrorConfig config;
 public HorrorDirector(HorrorConfig config,RandomGenerator random){this.config=config;this.random=random;}
 public PlayerMemory memory(UUID id){return memories.computeIfAbsent(id,k->new PlayerMemory());}
 public Map<UUID,PlayerMemory> memories(){return Collections.unmodifiableMap(memories);}
 public int activeCount(){return active.size();}
 public boolean isActive(UUID id){return active.stream().anyMatch(s->s.player.equals(id));}
 public void disconnect(UUID id){active.removeIf(s->s.player.equals(id));}
 public void reset(UUID id){disconnect(id);memories.remove(id);}
 public void clear(){active.clear();memories.clear();}
 public void restore(UUID id,PlayerMemory memory){memories.put(id,memory);}
 public List<Emission> tick(Map<UUID,Context> players){
  List<Emission> out=new ArrayList<>();
  if(!config.enabled){active.clear();return out;}
  active.removeIf(s->!players.containsKey(s.player));
  for(var entry:players.entrySet()) {
   UUID id=entry.getKey();Context c=entry.getValue();PlayerMemory m=memory(id);m.observe(c);
   double target=(Math.min(35,m.age/180.0)+(c.night()?13:0)+(c.underground()?12:0)+
    (c.light()<7?9:0)+(c.forest()?5:0)+Math.min(15,m.encounters*1.2)+Math.min(8,m.chunks.size()*.08)
    -(c.nearbyPlayers()>0?15:0))*config.intensity;
   if(c.second()<m.quietUntil)target=0;
   m.tension=Math.clamp(m.tension+Math.clamp(target-m.tension,-.35,.12),0,99);
   if(m.age<=config.graceSeconds||c.second()<m.nextEvent||c.second()<m.quietUntil||isActive(id))continue;
   if(active.size()>=config.maxActiveEvents||random.nextDouble()>.025*config.intensity)continue;
   List<HorrorEvent> eligible=Events.ALL.stream().filter(e->eligible(e,c,m)).toList();
   int total=eligible.stream().mapToInt(HorrorEvent::weight).sum();if(total==0)continue;
   int chosen=random.nextInt(total);
   for(var e:eligible){chosen-=e.weight();if(chosen<0){start(id,e,c,m);break;}}
  }
  for(var it=active.iterator();it.hasNext();) {
   Scene s=it.next();Context c=players.get(s.player);long elapsed=c.second()-s.start;
   for(int i=0;i<s.event.cues().size();i++)if(!s.fired.contains(i)&&elapsed>=s.event.cues().get(i).delay()){
    Cue cue=s.event.cues().get(i);s.fired.add(i);
    // Turning around repeatedly makes watchers favor the player's blind side.
    if(cue.kind()==Cue.Kind.ENTITY&&memory(s.player).vigilance>.25)
     cue=new Cue(cue.delay(),cue.kind(),cue.resource(),-Math.abs(cue.forward()),cue.side(),cue.up(),cue.strength(),cue.duration());
    out.add(new Emission(s.player,cue));
   }
   if(elapsed>=s.event.duration())it.remove();
  }
  return List.copyOf(out);
 }
 private boolean eligible(HorrorEvent e,Context c,PlayerMemory m){
  return e.minimum().ordinal()<=Intensity.of(m.tension).ordinal()&&e.condition().test(c)
   &&!e.id().equals(m.lastEvent)&&c.second()>=m.cooldowns.getOrDefault(e.id(),0L)
   &&(!e.hasEntity()||config.entityFrequency>0&&random.nextDouble()<config.entityFrequency/2)
   &&(!e.hasWorldEffect()||config.worldAnomalies)
   &&(!e.special()||m.encounters>=5);
 }
 private void start(UUID id,HorrorEvent event,Context c,PlayerMemory m){
  active.add(new Scene(id,event,c.second(),new HashSet<>()));m.lastEvent=event.id();m.encounters++;
  m.cooldowns.put(event.id(),c.second()+event.cooldown());
  m.nextEvent=c.second()+event.duration()+60+random.nextInt(120);
  if(event.id().equals("false_safety"))m.quietUntil=c.second()+event.duration()+180;
 }
 /** Operator-triggered scenes bypass environmental eligibility, but never limits or the global off switch. */
 public boolean force(UUID id,String eventId,Context c){
  Optional<HorrorEvent> event=Events.find(eventId);
  if(!config.enabled||event.isEmpty()||isActive(id)||active.size()>=config.maxActiveEvents)return false;
  if(event.get().hasWorldEffect()&&!config.worldAnomalies||event.get().hasEntity()&&config.entityFrequency==0)return false;
  start(id,event.get(),c,memory(id));return true;
 }
}
