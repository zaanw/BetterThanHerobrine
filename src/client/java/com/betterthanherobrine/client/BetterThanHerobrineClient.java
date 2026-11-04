package com.betterthanherobrine.client;
import com.betterthanherobrine.config.HorrorConfig;
import com.betterthanherobrine.core.BetterThanHerobrine;
import com.betterthanherobrine.networking.CuePayload;
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
 public static final ClientAtmosphere ATMOSPHERE=new ClientAtmosphere();
 public static Path configPath(){return FabricLoader.getInstance().getConfigDir().resolve("betterthanherobrine-client.properties");}
 public static void save(){try{config.save(configPath());}catch(IOException e){BetterThanHerobrine.LOG.error("Cannot save local settings",e);}}
 @Override public void onInitializeClient(){
  try{config=HorrorConfig.load(configPath());}catch(IOException e){throw new IllegalStateException("Cannot read BTH local settings",e);}
  EntityRendererRegistry.register(BetterThanHerobrine.WATCHER,WatcherRenderer::new);
  ClientPlayNetworking.registerGlobalReceiver(CuePayload.TYPE,(packet,context)->context.client().execute(()->ATMOSPHERE.accept(packet,config)));
  ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->ATMOSPHERE.clear());
  var category=KeyMapping.Category.register(BetterThanHerobrine.id("settings"));
  var key=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.betterthanherobrine.settings",InputConstants.Type.KEYBOARD,SDLScancode.SDL_SCANCODE_H,category));
  var adminKey=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.betterthanherobrine.admin",InputConstants.Type.KEYBOARD,SDLScancode.SDL_SCANCODE_F8,category));
  ClientTickEvents.END_CLIENT_TICK.register(client->{
   ATMOSPHERE.tick(config);
   while(key.consumeClick())if(client.gui.screen()==null)client.gui.setScreen(new SettingsScreen(null));
   while(adminKey.consumeClick())if(client.gui.screen()==null
    && client.player!=null
    && "zaanw".equalsIgnoreCase(client.player.getGameProfile().name()))
    client.gui.setScreen(new AdminScreen(null));
  });
  HudElementRegistry.addLast(BetterThanHerobrine.id("atmosphere"),(graphics,tracker)->ATMOSPHERE.draw(graphics));
 }
}
