package com.betterthanherobrine.client.mixin;
import com.betterthanherobrine.client.BetterThanHerobrineClient;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Attenuates environmental samples when the engine queries their gain; never writes options. */
@Mixin(AbstractSoundInstance.class)
public abstract class AtmosphericVolumeMixin {
 @Inject(method="getVolume",at=@At("RETURN"),cancellable=true)
 private void bth$gain(CallbackInfoReturnable<Float> callback){
  SoundInstance sound=(SoundInstance)(Object)this;
  SoundSource category=sound.getSource();
  if(category==SoundSource.AMBIENT||category==SoundSource.WEATHER||category==SoundSource.MUSIC||category==SoundSource.NEUTRAL){
   if(!sound.getLocation().getNamespace().equals("betterthanherobrine"))
    callback.setReturnValue(callback.getReturnValue()*(float)(1-BetterThanHerobrineClient.ATMOSPHERE.silence));
  }
 }
}
