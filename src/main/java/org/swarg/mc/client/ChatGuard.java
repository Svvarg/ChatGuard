package org.swarg.mc.client;

import org.swarg.mc.client.handlers.DeathLogHandler;
import org.swarg.mc.client.handlers.CommandChatGuard;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.swarg.mc.client.handlers.ChatReceivHadler;


/**
 * 12-02-21
 * @author Swarg
 */
@Mod(modid = ChatGuard.MODID, version = ChatGuard.VERSION)
public class ChatGuard {
    public static final Logger LOG = LogManager.getLogger("ChatGuard");
    public static final String MODID = "ChatGuard";
    public static final String VERSION = "0.4";
    public static final int BUILD = 39;
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide() == Side.CLIENT) {
            //commands chat-guard ignore & blacklist for messages from discord private emote global (todo Towny prefix)
            net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new CommandChatGuard());
            
            //Gui Guard
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new ChatReceivHadler());

            //death coords logger
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new DeathLogHandler());
        }
    }
}
