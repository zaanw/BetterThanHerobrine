package com.betterthanherobrine.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Local convenience UI. Every operation is re-authorized by the server-side command. */
public final class AdminScreen extends Screen {
 private final Screen parent;
 public AdminScreen(Screen parent){
  super(Component.literal("BetterThanHerobrine | ZAANW DEBUG"));
  this.parent=parent;
 }
 private void action(int row,String title,String command){
  int x=width/2-135;
  addRenderableWidget(new DarkButton(x,52+row*29,270,24,Component.literal(title),
    button->{
     if(minecraft.player!=null && "zaanw".equalsIgnoreCase(minecraft.player.getGameProfile().name())){
      minecraft.player.connection.sendCommand("bth admin "+command);
     }
    }));
 }
 @Override protected void init(){
  action(0,"Spawn Watcher","watcher");
  action(1,"Trigger Chaos","chaos");
  action(2,"Clear Effects / Watcher","clear");
  action(3,"Show Debug Status","status");
  addRenderableWidget(new DarkButton(width/2-135,height-36,270,23,Component.literal("Close"),button->onClose()));
 }
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
  g.fill(0,0,width,height,0xe609090d);
  g.fill(width/2-140,23,width/2+140,25,0xff993939);
  g.centeredText(font,title,width/2,13,0xffe3d9d9);
  super.extractRenderState(g,mx,my,delta);
 }
 @Override public void onClose(){minecraft.gui.setScreen(parent);}
}
