package com.betterthanherobrine.rendering;
import com.betterthanherobrine.core.BetterThanHerobrine;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
/** Vanilla cuboid humanoid mesh, original dark texture with pale eyes. */
public final class WatcherRenderer extends ZombieRenderer {
 public WatcherRenderer(EntityRendererProvider.Context context){super(context);}
 @Override public Identifier getTextureLocation(ZombieRenderState state){return BetterThanHerobrine.id("textures/entity/watcher.png");}
}
