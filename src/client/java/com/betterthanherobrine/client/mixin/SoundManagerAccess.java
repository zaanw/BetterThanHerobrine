package com.betterthanherobrine.client.mixin;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(SoundManager.class)
public interface SoundManagerAccess {
 @Accessor("soundEngine") SoundEngine bth$engine();
}
