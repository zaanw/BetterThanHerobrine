package com.betterthanherobrine.client;
import com.betterthanherobrine.core.BetterThanHerobrine;
import com.betterthanherobrine.config.HorrorConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.function.*;
/** Local audiovisual preferences; integrated server policy can also be edited by its owner. */
public final class SettingsScreen extends Screen {
 private final Screen parent;
 private long opened;
 private int page;
 public SettingsScreen(Screen parent){super(Component.literal("BetterThanHerobrine"));this.parent=parent;}
 private void button(int x,int y,int w,String label,Runnable action){addRenderableWidget(new DarkButton(x,y,w,20,Component.literal(label),b->action.run()));}
 private void toggle(int row,String key,BooleanSupplier get,Consumer<Boolean> set){
  int x=width/2-148,y=48+row*23;
  addRenderableWidget(new DarkButton(x,y,296,20,Component.translatable(key).append(": "+(get.getAsBoolean()?"ON":"OFF")),b->{set.accept(!get.getAsBoolean());saveAndRefresh();}));
 }
 private void value(int row,String key,DoubleSupplier get,DoubleConsumer set){
  int x=width/2-148,y=48+row*23;
  addRenderableWidget(new DarkButton(x,y,296,20,Component.translatable(key).append(": "+Math.round(get.getAsDouble()*100)+"%"),b->{double v=get.getAsDouble()+.25;set.accept(v>1.001?0:v);saveAndRefresh();}));
 }
 private void saveAndRefresh(){
  BetterThanHerobrineClient.save();
  if(minecraft.hasSingleplayerServer()){
   var c=BetterThanHerobrineClient.config;
   boolean enabled=c.enabled,anomalies=c.worldAnomalies;
   double intensity=c.intensity,frequency=c.entityFrequency;
   minecraft.getSingleplayerServer().execute(()->{
    var mod=BetterThanHerobrine.INSTANCE;
    mod.config.enabled=enabled;mod.config.worldAnomalies=anomalies;
    mod.config.intensity=intensity;mod.config.entityFrequency=frequency;
    mod.config.validate();mod.saveConfig();
   });
  }
  clearWidgets();init();
 }
 @Override protected void init(){
  if(opened==0)opened=System.nanoTime();var c=BetterThanHerobrineClient.config;
  if(page==0){
   toggle(0,"bth.enable",()->c.enabled,v->c.enabled=v);
   toggle(1,"bth.sounds",()->c.atmosphericSounds,v->c.atmosphericSounds=v);
   value(2,"bth.volume",()->c.volume,v->c.volume=v);
   toggle(3,"bth.visuals",()->c.visualEffects,v->c.visualEffects=v);
   value(4,"bth.fog",()->c.fogIntensity,v->c.fogIntensity=v);
  }else if(page==1){
   toggle(0,"bth.screen",()->c.screenEffects,v->c.screenEffects=v);
   toggle(1,"bth.performance",()->c.performanceMode,v->c.performanceMode=v);
   toggle(2,"bth.anomalies",()->c.worldAnomalies,v->c.worldAnomalies=v);
   toggle(3,"bth.reduced",()->c.reducedVisuals,v->c.reducedVisuals=v);
   if(minecraft.hasSingleplayerServer()){
    value(4,"bth.intensity",()->c.intensity/2,v->c.intensity=v*2);

   }
  }
  if(page==2&&minecraft.hasSingleplayerServer())value(0,"bth.frequency",()->c.entityFrequency/2,v->c.entityFrequency=v*2);
  button(width/2-148,height-73,296,(page+1)+" / 3",()->{page=(page+1)%3;clearWidgets();init();});
  int y=height-49;int x=width/2-148;
  String[] presets={"Low","Normal","High","Nightmare"};
  for(int i=0;i<presets.length;i++){
   String name=presets[i];
   DarkButton option=new DarkButton(x+i*75,y,71,20,Component.literal(name),
    b->{c.preset(name);saveAndRefresh();});
   option.active=true;
   addRenderableWidget(option);
  }
  button(x,height-25,296,"OK",this::onClose);
 }
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
  float fade=Math.clamp((System.nanoTime()-opened)/350_000_000f,0,1);
  g.fill(0,0,width,height,((int)(fade*235)<<24)|0x08090b);
  g.fill(width/2-148,34,width/2+148,35,0xff663334);
  g.centeredText(font,title,width/2,18,0xffd4d0cf);
  super.extractRenderState(g,mx,my,delta);
 }
 @Override public void onClose(){BetterThanHerobrineClient.save();minecraft.gui.setScreen(parent);}
}
