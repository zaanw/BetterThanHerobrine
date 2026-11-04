package com.betterthanherobrine.client;
import com.betterthanherobrine.events.Cue;
import com.betterthanherobrine.networking.CuePayload;
import com.betterthanherobrine.config.HorrorConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import java.util.*;
/** Personal effects only. Finite lifetimes and hard caps; no client block writes. */
public final class ClientAtmosphere {
 private record Effect(CuePayload packet,long start,long end){}
 private final List<Effect> effects=new ArrayList<>();
 private final List<HorrorSound> sounds=new ArrayList<>();
 private long tick;
 private String dimension="";

 public double tint,haze,distortion;
 public volatile double silence;
 private final AmbientMixer mixer=new AmbientMixer();
 public void accept(CuePayload p,HorrorConfig c){
  var mc=Minecraft.getInstance();
  if(mc.level==null||!mc.level.dimension().identifier().toString().equals(p.dimension()))return;
  if(!dimension.equals(p.dimension())){clear();dimension=p.dimension();}
  if(p.kind()==Cue.Kind.CLEAR){clear();return;}
  if(!c.enabled)return;
  if(p.kind()==Cue.Kind.ENTITY)throw new IllegalArgumentException("Entity cue requires server spawning");
  if(p.kind()==Cue.Kind.SYMBOL&&!c.worldAnomalies)return;
  if(p.kind()==Cue.Kind.SOUND){
   if(c.atmosphericSounds&&sounds.size()<12){var sound=new HorrorSound(p,(float)c.volume);sounds.add(sound);mc.getSoundManager().play(sound);}
   return;
  }
  if(effects.size()>=12)effects.removeFirst();
  effects.add(new Effect(p,tick,tick+Math.max(1,p.duration())*20L));
 }
 public void tick(HorrorConfig c){
  var mc=Minecraft.getInstance();tick++;
  String now=mc.level==null?"":mc.level.dimension().identifier().toString();
  if(!now.equals(dimension)||!c.enabled||mc.player==null||!mc.player.isAlive()){clear();dimension=now;return;}
  effects.removeIf(e->tick>=e.end);sounds.removeIf(HorrorSound::isStopped);
  double targetTint=0,targetHaze=0,targetSilence=0,targetDistortion=0;int particles=0;
  for(var e:effects){
   var p=e.packet;double fade=Math.min(1,(tick-e.start)/30.0)*Math.min(1,(e.end-tick)/40.0)*p.strength();
   double visual=fade*(c.reducedVisuals?.3:1);
   switch(p.kind()){
    case TINT -> {if(c.visualEffects&&c.screenEffects)targetTint=Math.max(targetTint,visual);}
    case FOG -> {if(c.visualEffects)targetHaze=Math.max(targetHaze,visual*c.fogIntensity);}
    case DISTORT -> {if(c.visualEffects&&c.screenEffects)targetDistortion=Math.max(targetDistortion,visual);}
    case SILENCE -> {if(c.atmosphericSounds)targetSilence=Math.max(targetSilence,fade);}
    case PARTICLES, SYMBOL -> {
     if(!c.visualEffects||mc.level==null||mc.player==null||particles>=(c.performanceMode||c.reducedVisuals?2:8)||tick%4!=0)break;
     Vec3 origin=new Vec3(p.x(),p.y(),p.z());if(mc.player.position().distanceToSqr(origin)>96*96)break;
     if(p.kind()==Cue.Kind.SYMBOL){
      // Particle cross: no fake block, no structure replacement, no saved-world changes.
      double t=(tick-e.start)%32/8.0-2;
      mc.level.addParticle(ParticleTypes.ASH,origin.x,origin.y+t*.5,origin.z,0,0,0);
      if(!c.performanceMode)mc.level.addParticle(ParticleTypes.ASH,origin.x+t*.4,origin.y+.3,origin.z,0,0,0);
      particles+=2;
     }else{
      var random=java.util.concurrent.ThreadLocalRandom.current();var type=p.resource().equals("glimmer")?ParticleTypes.END_ROD:ParticleTypes.ASH;
      mc.level.addParticle(type,origin.x+(random.nextDouble()-.5)*3,origin.y+random.nextDouble()*2,origin.z+(random.nextDouble()-.5)*3,0,.005,0);particles++;
     }
    }
    case SOUND, ENTITY, CLEAR, JOIN, VANISH -> throw new IllegalStateException("Nonvisual cue in effect queue");
   }
  }
  tint+=(targetTint-tint)*.1;haze+=(targetHaze-haze)*.08;silence+=(targetSilence-silence)*.1;
  distortion+=(targetDistortion-distortion)*.08;
  mixer.tick(silence);
 }
 public void clear(){
  effects.clear();sounds.forEach(HorrorSound::stop);sounds.clear();tint=0;haze=0;silence=0;distortion=0;mixer.tick(0);
 }
 public void draw(GuiGraphicsExtractor g){
  int w=g.guiWidth(),h=g.guiHeight();
  // A low-contrast camera veil. This is not a replacement for depth-dependent terrain fog.
  int alpha=(int)Math.clamp(haze*55+tint*42,0,80);
  if(alpha>0)g.fill(0,0,w,h,(alpha<<24)|0x15151b);
  if(distortion>.005){
   for(int row=0;row<8;row++){
    int offset=(int)(Math.sin(tick*.025+row*.7)*distortion*9);
    int alphaBand=(int)(distortion*22);
    g.fill(Math.max(0,offset),row*h/8,w-Math.max(0,-offset),row*h/8+Math.max(1,h/100),(alphaBand<<24)|0x16141a);
   }
  }
  int edge=(int)Math.clamp(tint*60,0,65);
  for(int i=0;i<12&&edge>0;i++){
   int a=(int)(edge*(1-i/12.0));int x=i*w/100,y=i*h/80;
   g.fill(x,y,w-x,y+Math.max(1,h/80),(a<<24)|0x100509);
   g.fill(x,h-y-Math.max(1,h/80),w-x,h-y,(a<<24)|0x100509);
  }
 }
}
