package com.betterthanherobrine.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import java.util.function.Supplier;
/** Custom dark controls retain vanilla focus, keyboard activation and narration. */
public final class DarkButton extends Button {
 private float hover;
 public DarkButton(int x,int y,int width,int height,Component label,OnPress press){super(x,y,width,height,label,press,Supplier::get);}
 @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float delta){
  boolean over=mx>=getX()&&mx<getX()+getWidth()&&my>=getY()&&my<getY()+getHeight();
  hover+=((over||isFocused()?1:0)-hover)*.15f;
  int shade=22+(int)(hover*10),red=65+(int)(hover*35);
  g.fill(getX(),getY(),getX()+getWidth(),getY()+getHeight(),0xff000000|shade<<16|shade<<8|shade);
  g.fill(getX(),getY()+getHeight()-1,getX()+getWidth(),getY()+getHeight(),0xff000000|red<<16|0x2929);
  g.centeredText(Minecraft.getInstance().font,getMessage(),getX()+getWidth()/2,getY()+(getHeight()-8)/2,active?0xffd2cccc:0xff6b6464);
 }
}
