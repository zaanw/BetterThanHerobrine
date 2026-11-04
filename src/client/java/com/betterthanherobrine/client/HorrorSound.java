package com.betterthanherobrine.client;
import com.betterthanherobrine.core.BetterThanHerobrine;
import com.betterthanherobrine.networking.CuePayload;
import net.minecraft.client.resources.sounds.*;
import net.minecraft.sounds.SoundSource;
/** Finite positional sound with a short attack and smooth release. */
public final class HorrorSound extends AbstractSoundInstance implements TickableSoundInstance {
 private int age;
 private final int lifetime;
 private final float gain;
 private boolean stopped;
 public HorrorSound(CuePayload packet,float volume){
  super(BetterThanHerobrine.id(packet.resource()),SoundSource.AMBIENT,SoundInstance.createUnseededRandom());
  x=packet.x();y=packet.y();z=packet.z();pitch=.9f+random.nextFloat()*.2f;
  gain=packet.strength()*volume;lifetime=Math.max(12,packet.duration()*20);this.volume=gain*.08f;
  attenuation=SoundInstance.Attenuation.LINEAR;relative=false;looping=false;
 }
 @Override public void tick(){
  age++;volume=gain*Math.min(1,age/4f)*Math.clamp((lifetime-age)/16f,0,1);
  if(age>=lifetime)stopped=true;
 }
 @Override public boolean isStopped(){return stopped;}
 public void stop(){stopped=true;}
}
