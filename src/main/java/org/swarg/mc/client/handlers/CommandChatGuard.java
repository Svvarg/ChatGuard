package org.swarg.mc.client.handlers;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.CommandBase;
import net.minecraft.util.ChatComponentText;
import net.minecraft.entity.player.EntityPlayer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import static org.swarg.mc.client.ChatGuard.LOG;
import static org.swarg.mc.client.ChatGuard.BUILD;
import static org.swarg.mc.client.ChatGuard.VERSION;
import static net.minecraft.util.StringUtils.isNullOrEmpty;
import org.swarg.mc.client.ChatGuardState;

/**
 * 12-02-21
 * Chat-Guard: to hide the messages from the players in blacklist
 * [ClientSide]
 * @author Swarg
 */
public class CommandChatGuard extends CommandBase {

    public CommandChatGuard() {
        //incoming chat message filter via onClientChatReceivedEvent() method 
        ChatGuardState.instance().loadBlackList();//load backlist from file + set hasFilter
    }


    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public String getCommandName() {
        return "chat-guard"; //my-ignore ignore-discord
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return getCommandName() + " <version/status/reload/list/add/remove/enable/discord/reset>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        ChatGuardState cg = ChatGuardState.instance();

        /*DEBUG*/if (cg.debug) { LOG.info("[###] [ChatGuard] processCommand()");}

        if (sender != null && sender.getEntityWorld() != null && sender.getEntityWorld().isRemote && args != null) {
            final int sz = args.length;
            final int aIndex = 0;
            String cmd = aIndex >= sz ? "help" : args[aIndex]; // 0 - "discord"
            String response = "?";
            if ("help".equalsIgnoreCase(cmd)) {
                response = getCommandUsage(sender);
            }
            else if ("version".equalsIgnoreCase(cmd) || "v".equalsIgnoreCase(cmd)) {
                response = VERSION + "b." + BUILD;
            }
            else if ("status".equalsIgnoreCase(cmd) || "st".equalsIgnoreCase(cmd)) {
                response = cg.status();
            }
            else if ("list".equalsIgnoreCase(cmd) || "ls".equalsIgnoreCase(cmd)) {
                response = cg.getNamesInBlackList();
            }
            //yet work only reload black list
            else if ("reload".equalsIgnoreCase(cmd)) {
                response = "Reloaded: " + cg.loadBlackList() + " Names in blacklist: " + cg.blacklist.size();
            }

            //'add Name of Player support spaces' for towny title case
            else if ("add".equalsIgnoreCase(cmd)) {
                final int i = aIndex + 1;
                String name = getName(args, i); //= i >= sz ? null : args[i];
                if (isNullOrEmpty(name)) {
                    response = "(player-name)";
                }
                else {
                    if (name.equals(sender.getCommandSenderName())) {
                        response = "it is forbidden to add your own name";
                    } else {
                        boolean b = cg.addToBlackList(name);
                        response = "Player '"+ name + (b ? "' added to" : " already contains in") + " blacklist";
                        if (b) {
                            cg.saveBlackList();
                        }
                    }
                }
            }
            //support spaces in name
            else if ("remove".equalsIgnoreCase(cmd) || "rm".equalsIgnoreCase(cmd)) {
                final int i = aIndex + 1;
                String name = getName(args, i); //= i >= sz ? null : args[i];
                if (isNullOrEmpty(name)) {
                    response = "(player-name)";
                } else {
                    boolean b = cg.removeFromBlackList(name);
                    response = "Player '" + name + (b ? "' removed from": " not found in") + " blacklist";
                    if (b) {
                        cg.saveBlackList();
                    }
                }
            }

            //switch on off of chat-guard work with blacklist
            else if ("enable".equalsIgnoreCase(cmd)) {
                cg.chatGuardEnabled = !cg.chatGuardEnabled;
                response = "BlackListEnabled: " + cg.chatGuardEnabled;
            }
            //hide all message from discord
            else if ("discord".equalsIgnoreCase(cmd)) {
                cg.hideAllDiscordMessages = !cg.hideAllDiscordMessages;
                response = "Hide All Message From Discord: " + cg.hideAllDiscordMessages;
            }
            //clear stat
            else if ("reset".equalsIgnoreCase(cmd)) {
                cg.chatGuardEnabled = true;
                cg.hideAllDiscordMessages = false;
                cg.hasFilter = false;
                cg.blacklist.clear();
                response = "Reset to default, blacklist cleared";
            }

            //Trace to ClientSide Logger
            else if ("debug".equalsIgnoreCase(cmd)) {
                cg.debug = !cg.debug;
                response = "Debug: " + cg.debug;
            }

            //todo use white list

            showChatMsg(response);
        }
    }

    /**
     * Client Side: playernames are taken only from world of current player
     * getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
     * todo from tab (clinetSideOnline)
     * @param sender
     * @param args
     * @return
     */
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        Minecraft mc = Minecraft.getMinecraft();
        if (args != null && args.length > 1 && mc != null && mc.theWorld != null && mc.theWorld.playerEntities != null) {
            String s1 = args[args.length - 1];
            //for 'remove' & 'and'
            final int i = mc.theWorld.playerEntities.size();
            List list = new ArrayList();

            for (int j = 0; j < i; ++j) {
                Object o = mc.theWorld.playerEntities.get(j);
                if (o instanceof EntityPlayer) {
                    final String name = ((EntityPlayer) o).getCommandSenderName();
                    if (doesStringStartWith(s1, name)) {
                        list.add(name);
                    }
                }
            }
            return list;
        }
        return Collections.EMPTY_LIST;
    }







    /**
     * Show Message to Chat Locally in Client Side
     * @param response
     */
    public void showChatMsg(String response) {
        if (response != null) {
            try {
                GuiIngame guiInGame = Minecraft.getMinecraft().ingameGUI;//@SideOnly(Side.CLIENT)
                if (response.contains("\n")) {
                    String[] a = response.split("\n");
                    for (String line : a) {
                        guiInGame.getChatGUI().printChatMessage(new ChatComponentText(line));
                    }
                } else {
                    guiInGame.getChatGUI().printChatMessage(new ChatComponentText(response));
                }
            }
            catch (Exception e) {
                e.printStackTrace();//todo log
            }
        }
    }

    //cmd4j join
    public static String getName(String[] args, int i) {
        if (args != null && i > -1 && i < args.length) {
                                             //     0    1     2      3
            final int rm = args.length - i; // cg add Brave Solder Name
            if (rm > 1) {
                StringBuilder sb = new StringBuilder();
                for (int j = i; j < args.length; j++) {
                    if (j > i) {
                        sb.append(' ');
                    }
                    sb.append(args[j]);
                }
                return sb.toString();
            } else {
                return args[i];
            }
        }
        return null;
    }




    //public void onCommandEvent(net.minecraftforge.event.CommandEvent event) {
    //    if (event instanceof CommandEvent && "cbc".equals(event.command.getCommandName()) ) { //ClientSide
    //        String cmd = event.parameters[0];
    //        if ("echo".equals(cmd)) {
    //            clientPrintChatMessage("[Echo]"+ cmd);
    //        }
    //    }
    //}
}
