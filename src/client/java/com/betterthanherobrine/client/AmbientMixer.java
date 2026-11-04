package com.betterthanherobrine.client;
import com.betterthanherobrine.client.mixin.*;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
/** Reapplies gains of already playing channels; never alters option values or system volume. */
public final class AmbientMixer {
 private boolean restoring;
 public void tick(double silence){
  if(silence<=.001&&!restoring)return;
  var mc=Minecraft.getInstance();
  var engine=((SoundManagerAccess)mc.getSoundManager()).bth$engine();int visited=0;
  for(var entry:((SoundEngineAccess)engine).bth$channels().entrySet()){
   if(++visited>512)break;
   var sound=entry.getKey();SoundSource source=sound.getSource();
   if(source!=SoundSource.AMBIENT&&source!=SoundSource.MUSIC&&source!=SoundSource.WEATHER&&source!=SoundSource.NEUTRAL)continue;
   float user=mc.options.getSoundSourceOptionInstance(source).get().floatValue();
   // getVolume is attenuated by AtmosphericVolumeMixin, including while new sounds begin.
   float gain=Math.clamp(sound.getVolume()*user,0,1);
   entry.getValue().execute(channel->channel.setVolume(gain));
  }
  restoring=silence>.001;
 }
}
