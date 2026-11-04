package com.betterthanherobrine.client;
import com.betterthanherobrine.events.Events;
import com.betterthanherobrine.networking.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
/** Opens only after an affirmative server response; all buttons are re-authorized server-side. */
public final class AdminScreen extends Screen {
 public static AdminRequest.Action requestedInfo;
 private final Screen parent;private int page,selected;
 public AdminScreen(Screen parent){super(Component.literal("BetterThanHerobrine"));this.parent=parent;}
 private void action(int row,String key,AdminRequest.Action action){
  addRenderableWidget(new DarkButton(width/2-145,43+row*23,290,20,Component.translatable(key),button->send(action,"")));
 }
 private void send(AdminRequest.Action action,String event){
  if(BetterThanHerobrineClient.adminCooldown==0&&ClientPlayNetworking.canSend(AdminRequest.TYPE)){BetterThanHerobrineClient.adminCooldown=20;if(action==AdminRequest.Action.ENTITIES||action==AdminRequest.Action.TIMERS)requestedInfo=action;ClientPlayNetworking.send(new AdminRequest(action,event));}
 }
 @Override protected void init(){
  if(page==0){
   action(0,"bth.admin.spawn",AdminRequest.Action.SPAWN);action(1,"bth.admin.remove",AdminRequest.Action.REMOVE);
   action(2,"bth.admin.random",AdminRequest.Action.RANDOM);action(3,"bth.admin.nightmare",AdminRequest.Action.NIGHTMARE);
   action(4,"bth.admin.stop",AdminRequest.Action.STOP);
  }else{
   action(0,"bth.admin.debug",AdminRequest.Action.DEBUG);action(1,"bth.admin.entities",AdminRequest.Action.ENTITIES);
   action(2,"bth.admin.timers",AdminRequest.Action.TIMERS);action(3,"bth.admin.reset",AdminRequest.Action.RESET);
   var choices=Events.ALL.stream().filter(e->!e.id().equals("the_haunting")).toList();
   addRenderableWidget(new DarkButton(width/2-145,135,230,20,Component.literal(choices.get(selected).id()),b->{selected=(selected+1)%choices.size();clearWidgets();init();}));
   addRenderableWidget(new DarkButton(width/2+88,135,57,20,Component.translatable("bth.admin.run"),b->send(AdminRequest.Action.EVENT,choices.get(selected).id())));
  }
  addRenderableWidget(new DarkButton(width/2-145,height-49,142,20,Component.literal((page+1)+" / 2"),b->{page=1-page;clearWidgets();init();}));
  addRenderableWidget(new DarkButton(width/2+3,height-49,142,20,Component.translatable("gui.done"),b->onClose()));
 }
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
  g.fill(0,0,width,height,0xed09090d);g.fill(width/2-145,31,width/2+145,33,0xff803535);
  g.centeredText(font,title,width/2,15,0xffded7d7);
  var s=BetterThanHerobrineClient.adminState;
  if(s!=null){
   String status="Entities: "+s.entities()+" | Events: "+s.scenes();g.centeredText(font,status,width/2,height-24,0xffb9b2b2);
   String detail=s.details();if(detail.length()>48)detail=detail.substring(0,48)+"…";
   g.centeredText(font,detail.equals("bth.busy")?Component.translatable(detail):Component.literal(detail),width/2,height-12,0xffb9b2b2);
  }
  super.extractRenderState(g,mx,my,delta);
 }
 @Override public void onClose(){minecraft.gui.setScreen(parent);}
 @Override public boolean isPauseScreen(){return false;}
}
