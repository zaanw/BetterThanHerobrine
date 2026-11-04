package com.betterthanherobrine.rendering;
import com.betterthanherobrine.core.BetterThanHerobrine;
import com.betterthanherobrine.entity.WatcherEntity;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.Identifier;
public final class HerobrineRenderer extends HumanoidMobRenderer<WatcherEntity,HerobrineRenderState,HerobrineModel> {
 public HerobrineRenderer(EntityRendererProvider.Context context){super(context,new HerobrineModel(context.bakeLayer(HerobrineModel.LAYER)),.35f);}
 @Override public HerobrineRenderState createRenderState(){return new HerobrineRenderState();}
 @Override public void extractRenderState(WatcherEntity entity,HerobrineRenderState state,float delta){
  super.extractRenderState(entity,state,delta);state.doppelganger=entity.behavior()==WatcherEntity.Behavior.DOPPELGANGER;
 }
 @Override public Identifier getTextureLocation(HerobrineRenderState state){return BetterThanHerobrine.id(state.doppelganger?"textures/entity/doppelganger.png":"textures/entity/herobrine.png");}
}
