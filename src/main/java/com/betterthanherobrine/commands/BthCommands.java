package com.betterthanherobrine.commands;
import com.betterthanherobrine.core.BetterThanHerobrine;
import com.betterthanherobrine.horror.Intensity;
import com.betterthanherobrine.events.*;
import com.mojang.brigadier.arguments.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;
import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionLevel;
import static net.minecraft.commands.Commands.*;

public final class BthCommands {
 private BthCommands(){}
 public static void register(BetterThanHerobrine mod){
  CommandRegistrationCallback.EVENT.register((dispatcher,registry,environment)->dispatcher.register(literal("bth")
   .then(literal("help").executes(c->{c.getSource().sendSuccess(()->Component.translatable("bth.help"),false);return 1;}))
   .then(literal("status").executes(c->{
    var p=c.getSource().getPlayerOrException();var m=mod.director.memory(p.getUUID());
    String status=mod.config.debug?Intensity.of(m.tension)+" "+Math.round(m.tension)+" / "+m.encounters:mod.config.enabled?"enabled":"disabled";
    c.getSource().sendSuccess(()->Component.literal("BetterThanHerobrine: "+status),false);return 1;
   }))
   .then(literal("debug").requires(PermissionPredicates.require(BetterThanHerobrine.id("command/debug"),PermissionLevel.GAMEMASTERS))
    .executes(c->{mod.config.debug=!mod.config.debug;mod.saveConfig();c.getSource().sendSuccess(()->Component.literal("BTH debug: "+mod.config.debug),true);return 1;}))
   .then(literal("summon").requires(PermissionPredicates.require(BetterThanHerobrine.id("command/summon"),PermissionLevel.GAMEMASTERS))
    .executes(c->{var p=c.getSource().getPlayerOrException();if(!mod.config.enabled||mod.config.entityFrequency==0)return 0;mod.emit(p,Cue.watcher(0,26,4,60));return 1;}))
   .then(literal("event").requires(PermissionPredicates.require(BetterThanHerobrine.id("command/event"),PermissionLevel.GAMEMASTERS))
    .then(argument("event_id",StringArgumentType.word()).suggests((c,b)->SharedSuggestionProvider.suggest(Events.ALL.stream().map(HorrorEvent::id),b))
     .executes(c->{var p=c.getSource().getPlayerOrException();boolean ok=mod.director.force(p.getUUID(),StringArgumentType.getString(c,"event_id"),mod.context(p));
      if(!ok)c.getSource().sendFailure(Component.translatable("bth.busy"));return ok?1:0;})))
   .then(literal("intensity").requires(PermissionPredicates.require(BetterThanHerobrine.id("command/intensity"),PermissionLevel.GAMEMASTERS))
    .then(argument("value",DoubleArgumentType.doubleArg(0,2)).executes(c->{mod.config.intensity=DoubleArgumentType.getDouble(c,"value");mod.saveConfig();return 1;})))
   .then(literal("reset").requires(PermissionPredicates.require(BetterThanHerobrine.id("command/reset"),PermissionLevel.GAMEMASTERS))
    .executes(c->{var p=c.getSource().getPlayerOrException();mod.director.reset(p.getUUID());mod.placement.clear(p.getUUID());mod.emit(p,Cue.effect(0,Cue.Kind.CLEAR,0,0));mod.save();return 1;}))
  ));
 }
}
