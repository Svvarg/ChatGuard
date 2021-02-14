package org.swarg.mc.client.handlers;

import java.util.List;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import org.apache.logging.log4j.Level;
import org.swarg.mc.client.ChatGuardState;
import static org.swarg.mc.client.ChatGuard.*;
import static net.minecraft.util.StringUtils.isNullOrEmpty;

/**
 * 14-02-21
 * @author Swarg
 */
public class ChatReceivHadler {
    private static final String DISCORD_TAG = "Discord"; //"[§9Discord§f]";

    @SubscribeEvent
    public void onClientChatReceivedEvent(ClientChatReceivedEvent event) {
        ChatGuardState cg = ChatGuardState.instance();

        /*DEBUG*/if (cg.debug) try { LOG.info("[###] onClientChatReceivedEvent: '{}' ", event.message.getUnformattedText());} catch (Exception e) {;}

        if (cg.chatGuardEnabled && (cg.hasFilter || cg.hideAllDiscordMessages)) {
            IChatComponent icm = event.message;
            if (icm != null) {
                try {
                    String msg = icm.getUnformattedText();
                    final int msglen = msg == null ? 0 : msg.length();
                    if (msglen > 4) {
                        String name = null;

                        //apply blacklist for messages from discord or hide all messages
                        if ( isMsgFromDiscord(msg) ) { //aka msg.startsWith(DISCORD_PREFIX)
                            if (cg.hideAllDiscordMessages) {
                                /*DEBUG*/if (cg.debug) { LOG.info("[###] Hide All Msg from Discord Cancelable: {}", event.isCancelable());}
                                if (event.isCancelable()) {
                                    event.setCanceled(true);
                                }
                                return;//work done
                            }
                            name = getDiscordAccountName(msg);
                            /*DEBUG*/if (cg.debug) { LOG.info("[###] Detected Message from Discord from: '{}'", name); }
                        }
                        //apply blacklist for messages from game
                        else if (cg.hasFilter) {
                            name = getSenderPlayerNameFrom(msg);//return null if not found player name
                            /*DEBUG*/if (cg.debug && name != null) {LOG.info("[###] Found PlayerName from Msg: '{}'", name); }
                        }

                        if (name != null && inBlackList(name, cg.blacklist)) {
                            /*DEBUG*/if (cg.debug) { LOG.info("[###] Ignore Player: '{}' Cancelable: {}", name, event.isCancelable());}
                            if (event.isCancelable()) {
                                event.setCanceled(true);
                            }
                        }
                    }
                }
                catch (Exception e) {
                    LOG.catching(Level.WARN,e);
                }
            }
        }
    }


    //=========================== UTIL ======================================\\



    /**
     * final String DISCORD_PREF:
     * "[§9Discord§f]" ->  5B 12A7 39446973636F7264 12A7 665D
     *                         ? §                     §
     * "[9Discordf]"   ->  5B      39446973636F7264      665D
     * simple message.startsWith(DISCORD_PREFIX); don`t work here
     * Actual hex:
     * 5BA739446973636F7264A7665D20
     *  [ § 9 D i s c o r d § f ]
     * @param msg
     * @return
     */
    public static boolean isMsgFromDiscord(String msg) {
        final int len = msg == null ? 0 : msg.length();
        if (len > 16 && msg.charAt(0) == '[') { //[§9Discord§f] <Name>: Text
            final int close = msg.indexOf(']');
            final int taglen = DISCORD_TAG.length();//"Discord"
            if (close > 7 && close < 8 + taglen) { // with reserve for color
                //the tag must be inside the brackets
                final int di = msg.lastIndexOf(DISCORD_TAG, close);
                return (di > 0);
            }
        }
        return false;
    }
    /**
     * [§9Discord§f] <PlayerName>: message-text
     * @param msg
     * @return
     */
    public static String getDiscordAccountName(String msg) {
        if (!isNullOrEmpty(msg)) {
            final int offset = DISCORD_TAG.length();
            int b = msg.indexOf('<', offset);
            if (b > -1) {
                int e = msg.indexOf(">: ", ++b);
                if (e > -1 && e > b) {
                    String name = msg.substring(b, e);
                    if (name.contains("§")) {
                        name = removeColor(name);
                    }
                    return name;
                }
            }
        }
        return null;
    }

    /**
     * I check if it's a message from the player
     * if yes I pull out the sender's name
     * simple message '<player2> hi'
     * emotion        '* player1 hi'
     * private msg    '[Player -> I] personal msg'
     * @param msg
     * @return
     */
    public static String getSenderPlayerNameFrom(String msg) {
        final int len = msg == null ? 0 : msg.length();
        if (len > 3) {
            final char fc = msg.charAt(0);
            int begin = -1;
            int close = -1;
            //simple message from player
            //<Name>: text
            //<Player_Name [ \ ]> global-message
            if (fc == '<') {
                begin = 1;
                close = msg.indexOf('>'); // [discord] <name>: message
                if (close > begin) {
                    final int p = msg.lastIndexOf('[', close);
                    //check case '<Name [ \ ]> msg'
                    if (p > begin) {
                        close = p;
                    }
                }
            }
            //private message [Player_Name -> I] private message
            //[Town] <Player_Name>: message word2
            else if (fc == '[') {
                final int closeB = msg.indexOf(']');
                begin = 1;
                close = msg.lastIndexOf("->", closeB); //private
                //[Town] <Player_Name>: message
                if (close < 0  && (begin = msg.indexOf('<', closeB) ) > 0 ) {
                    begin++;
                    close = msg.indexOf('>', begin);
                }
            }
            //emotion
            else if (fc == '*' && msg.charAt(1) == ' ') {
                begin = 2;
                close = msg.indexOf(' ', begin);
            }
            //player2 whispers hellow
            else if (len > 12 && Character.isAlphabetic(msg.charAt(0)) && (close = msg.indexOf(" whispers ")) > 0) {
                final int space = msg.indexOf(' ');
                if (close == space) {
                    begin = 0;
                }
            }

            if (close > begin && begin > -1) {
                final String name = msg.substring(begin, close).trim();
                return name;
            }
        }
        return null;
    }

    //TODO
    public static boolean inBlackList(String name, List<String> blacklist) {
        if (!isNullOrEmpty(name) && blacklist != null && !blacklist.isEmpty()) {
            for (int i = 0; i < blacklist.size(); i++) {
                String bname = blacklist.get(i);
                if (name.equalsIgnoreCase(bname)) {
                    return true;
                }
                //todo lang replace ru-en o-o p-p
            }
        }
        return false;
    }

    public static String removeColor(String name) {
        if (name != null) {
            StringBuilder sb = new StringBuilder();
            final int len = name.length();
            for (int i = 0; i < len; i++) {
                char c = name.charAt(i);
                char c2 = i + 1 >= len ? 0x00 : name.charAt(i + 1);
                if (c == 0xA7) {//§
                    i++;//§1
                }
                else if (c == 1042 && c2 == 0xA7/*'§'*/) {//167
                    i += 2;//§1
                }
                else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
        return name;
    }

}
