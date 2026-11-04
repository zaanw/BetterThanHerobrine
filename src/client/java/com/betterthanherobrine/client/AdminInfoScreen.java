package com.betterthanherobrine.client;
import com.betterthanherobrine.networking.AdminRequest;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;
/** Paged server snapshots retain complete entity IDs and timers, without truncating to one status line. */
public final class AdminInfoScreen extends Screen {
 private final Screen parent;private int page;
 public AdminInfoScreen(Screen parent,AdminRequest.Action action){super(Component.translatable(action==AdminRequest.Action.ENTITIES?"bth.admin.entities":"bth.admin.timers"));this.parent=parent;}
 private List<String> lines(){
  var state=BetterThanHerobrineClient.adminState;var out=new ArrayList<String>();
  if(state==null||!state.allowed())return out;
  for(String entry:state.details().split(" \\| ")){
   if(entry.isBlank())continue;
   for(int i=0;i<entry.length();i+=42)out.add(entry.substring(i,Math.min(i+42,entry.length())));
  }
  return out;
 }
 @Override protected void init(){
  addRenderableWidget(new DarkButton(width/2-145,height-34,142,20,Component.literal("< / >"),b->{int pages=Math.max(1,(lines().size()+7)/8);page=(page+1)%pages;}));
  addRenderableWidget(new DarkButton(width/2+3,height-34,142,20,Component.translatable("gui.done"),b->onClose()));
 }
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
  g.fill(0,0,width,height,0xed09090d);g.fill(width/2-145,32,width/2+145,33,0xff803535);g.centeredText(font,title,width/2,17,0xffded7d7);
  var data=lines();int pages=Math.max(1,(data.size()+7)/8);page=Math.min(page,pages-1);
  for(int row=0;row<8&&page*8+row<data.size();row++)g.text(font,data.get(page*8+row),width/2-140,46+row*14,0xffc2baba);
  g.centeredText(font,(page+1)+" / "+pages,width/2,height-50,0xffc2baba);
  super.extractRenderState(g,mx,my,delta);
 }
 @Override public void onClose(){minecraft.gui.setScreen(parent);}
 @Override public boolean isPauseScreen(){return false;}
}
