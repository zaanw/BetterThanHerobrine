package com.betterthanherobrine.events;
import com.betterthanherobrine.horror.*;
import java.util.*;
import java.util.function.Predicate;
public record HorrorEvent(String id, Intensity minimum, int cooldown, int weight,
 boolean special, Predicate<Context> condition, List<Cue> cues) {
 public HorrorEvent { cues=List.copyOf(cues);if(cues.isEmpty()||cooldown<1||weight<1)throw new IllegalArgumentException(id); }
 public int duration(){return cues.stream().mapToInt(c->c.delay()+Math.max(c.duration(),1)).max().orElse(1);}
 public boolean hasEntity(){return cues.stream().anyMatch(c->c.kind()==Cue.Kind.ENTITY);}
 public boolean hasWorldEffect(){return cues.stream().anyMatch(c->c.kind()==Cue.Kind.SYMBOL);}
}
