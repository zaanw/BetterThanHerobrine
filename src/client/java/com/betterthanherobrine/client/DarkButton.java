package com.betterthanherobrine.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import java.util.function.Supplier;
/** Quiet palette, fixed contrast, no flashes; vanilla keyboard navigation and narration. */
public final class DarkButton extends Button.Plain {
 private float hover;
 public DarkButton(int x,int y,int width,int height,Component label,OnPress press){
  super(x,y,width,height,label,press,Supplier::get);
 }
 @Override protected void extractWidgetRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float delta){
  hover+=(isHoveredOrFocused()?1-hover:-hover)*.18f;
  int shade=22+(int)(hover*10);
  graphics.fill(getX(),getY(),getX()+getWidth(),getY()+getHeight(),0xff000000|shade<<16|shade<<8|shade);
  int red=64+(int)(hover*50);
  graphics.fill(getX(),getY()+getHeight()-1,getX()+getWidth(),getY()+getHeight(),0xff000000|red<<16|0x2424);
  graphics.centeredText(Minecraft.getInstance().font,getMessage(),getX()+getWidth()/2,getY()+(getHeight()-8)/2,active?0xffd0cdcc:0xff686868);
 }
}
