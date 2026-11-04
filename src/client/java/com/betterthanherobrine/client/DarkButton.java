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
}
