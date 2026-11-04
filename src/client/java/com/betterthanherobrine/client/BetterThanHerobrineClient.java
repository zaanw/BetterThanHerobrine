package com.betterthanherobrine.client;
import com.betterthanherobrine.config.HorrorConfig;
import com.betterthanherobrine.core.BetterThanHerobrine;
import com.betterthanherobrine.networking.*;
import com.betterthanherobrine.rendering.*;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import com.betterthanherobrine.rendering.WatcherRenderer;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.sdl.SDLScancode;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import java.nio.file.Path;
import java.io.IOException;

public final class BetterThanHerobrineClient implements ClientModInitializer {
 public static HorrorConfig config;
 public static AdminState adminState;
 public static int adminCooldown;
 public static final ClientAtmosphere ATMOSPHERE=new ClientAtmosphere();
 public static Path configPath(){return FabricLoader.getInstance().getConfigDir().resolve("betterthanherobrine-client.properties");}
 public static void save(){try{config.save(configPath());}catch(IOException e){BetterThanHerobrine.LOG.error("Cannot save local settings",e);}}
 @Override public void onInitializeClient(){
  try{config=HorrorConfig.load(configPath());}catch(IOException e){throw new IllegalStateException("Cannot read BTH local settings",e);}
  EntityRendererRegistry.register(BetterThanHerobrine.WATCHER,WatcherRenderer::new);
  ModelLayerRegistry.registerModelLayer(HerobrineModel.LAYER,HerobrineModel::layer);
  EntityRendererRegistry.register(BetterThanHerobrine.HEROBRINE,HerobrineRenderer::new);
  ClientPlayNetworking.registerGlobalReceiver(AdminState.TYPE,(packet,context)->context.client().execute(()->{
   adminState=packet;var client=context.client();
   if(packet.allowed()&&packet.open())client.gui.setScreen(new AdminScreen(client.gui.screen()));
   if(!packet.allowed()){
    if(client.gui.screen() instanceof AdminScreen)client.gui.setScreen(null);
    if(client.player!=null)client.player.displayClientMessage(net.minecraft.network.chat.Component.translatable("bth.denied"),true);
   }
  }));
  ClientPlayNetworking.registerGlobalReceiver(CuePayload.TYPE,(packet,context)->context.client().execute(()->ATMOSPHERE.accept(packet,config)));
  ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->{ATMOSPHERE.clear();adminState=null;});
  var category=KeyMapping.Category.register(BetterThanHerobrine.id("settings"));
  var key=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.betterthanherobrine.settings",InputConstants.Type.KEYBOARD,SDLScancode.SDL_SCANCODE_H,category));
  var adminKey=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.betterthanherobrine.admin",InputConstants.Type.KEYBOARD,SDLScancode.SDL_SCANCODE_F8,category));
  ClientTickEvents.END_CLIENT_TICK.register(client->{
   ATMOSPHERE.tick(config);if(adminCooldown>0)adminCooldown--;
   while(key.consumeClick())if(client.gui.screen()==null)client.gui.setScreen(new SettingsScreen(null));
   while(adminKey.consumeClick())if(client.gui.screen()==null&&client.player!=null&&ClientPlayNetworking.canSend(AdminRequest.TYPE))
    ClientPlayNetworking.send(new AdminRequest(AdminRequest.Action.OPEN,""));
  });
  HudElementRegistry.addLast(BetterThanHerobrine.id("atmosphere"),(graphics,tracker)->{ATMOSPHERE.draw(graphics);
   if(adminState!=null&&adminState.allowed()&&adminState.debug()){
    graphics.text(net.minecraft.client.Minecraft.getInstance().font,"BTH entities="+adminState.entities()+" scenes="+adminState.scenes(),8,8,0xffc0aaaa);
    graphics.text(net.minecraft.client.Minecraft.getInstance().font,adminState.details().substring(0,Math.min(120,adminState.details().length())),8,20,0xffb0aaaa);
   }
  });
 }
}
