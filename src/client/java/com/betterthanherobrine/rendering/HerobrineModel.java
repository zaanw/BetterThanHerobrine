package com.betterthanherobrine.rendering;
import com.betterthanherobrine.core.BetterThanHerobrine;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
/** Own model layer: classic broad-armed humanoid proportions, independent from the zombie renderer. */
public final class HerobrineModel extends HumanoidModel<HerobrineRenderState> {
 public static final ModelLayerLocation LAYER=new ModelLayerLocation(BetterThanHerobrine.id("herobrine"),"main");
 public HerobrineModel(ModelPart root){super(root);}
 public static LayerDefinition layer(){return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE,0),64,64);}
 @Override public void setupAnim(HerobrineRenderState state){
  super.setupAnim(state);
  if(state.doppelganger){
   rightArm.xRot=(float)Math.sin(state.ageInTicks*.19)*.7f;
   leftArm.xRot=rightArm.xRot;head.zRot=(float)Math.sin(state.ageInTicks*.035)*.18f;
  }
 }
}
