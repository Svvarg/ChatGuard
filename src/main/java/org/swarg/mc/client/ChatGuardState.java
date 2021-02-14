package org.swarg.mc.client;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import static org.swarg.mc.client.ChatGuard.LOG;
import static net.minecraft.util.StringUtils.isNullOrEmpty;

/**
 * 14-02-21
 * @author Swarg
 */
public class ChatGuardState {
    /*File in ClientSide laucher root dir containing then blacklist playernames*/
    private static final String BLACKLIST_FILENAME = "chat-blacklist.txt";
    private static ChatGuardState INSTANCE;

    public final List<String> blacklist = new ArrayList<String>();//todo ability to switch as whitelist ?
    public boolean chatGuardEnabled = true;
    public boolean hideAllDiscordMessages;//streamer mode
    public boolean hasFilter;
    public boolean debug;

    public static ChatGuardState instance() {
        if (INSTANCE == null) {
            INSTANCE = new ChatGuardState();            
        }
        return INSTANCE;
    }


    public String status() {
        File blacklistfile = new File(BLACKLIST_FILENAME).getAbsoluteFile();
        return
          (debug ? "[DEBUG]\n" : "") +
            "ChatGuardEnabled: " + chatGuardEnabled +
          "\nHideAllDiscordMessages: " + hideAllDiscordMessages +
          "\nNames in blacklist: "     + blacklist.size() +
          "\nHasFilter: "              + hasFilter + //any name in black list
          "\nBlacklist file: "         + (blacklistfile.exists() ? blacklistfile : "not exists")
          ;
    }

    public String getNamesInBlackList() {
        if (this.blacklist == null || this.blacklist.isEmpty()) {
            return "blacklist is empty";
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < blacklist.size(); i++) {
                String name = blacklist.get(i);
                if (!isNullOrEmpty(name)) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(name);
                }
            }
            return sb.toString();
        }
    }

    public boolean addToBlackList(String name) {
        if (this.blacklist != null && !isNullOrEmpty(name) && !name.equalsIgnoreCase(getSelfName()) && !this.blacklist.contains(name)) {
            this.blacklist.add(name);
            this.hasFilter = this.blacklist.size() > 0;
            return true;
        }
        return false;
    }

    public boolean removeFromBlackList(String name) {
        if (this.blacklist != null) {
            boolean removed = this.blacklist.remove(name);
            this.hasFilter = this.blacklist.size() > 0;
            return removed;
        } else {
            return false;
        }
    }
    public boolean saveBlackList() {
        if (this.blacklist != null && !isNullOrEmpty(BLACKLIST_FILENAME)) {
            try {
                FileUtils.writeLines(new File(BLACKLIST_FILENAME), "UTF-8", blacklist, "\n", false);
                return true;
            }
            catch (Exception e) {
                LOG.error("onSave chat-blacklist to file {} {}", BLACKLIST_FILENAME, e);
            }
        }
        return false;
    }


    public boolean loadBlackList() {
        try {
            this.blacklist.clear();
            File file = new File(BLACKLIST_FILENAME);
            final String selfname = getSelfName();

            if (file.exists() && file.canRead() ) {
                LineIterator it = FileUtils.lineIterator(file, "UTF-8");

                while (it.hasNext()) {
                    String name = it.nextLine();
                    if (!isNullOrEmpty(name) && !name.startsWith("#") && !name.equalsIgnoreCase(selfname)) {
                        this.blacklist.add(name);
                    }
                }
                this.hasFilter = this.blacklist.size() > 0;
                return true;
            }
        }
        catch (Exception e) {
            LOG.error("onLoad chat-blacklist from file {} {}", BLACKLIST_FILENAME, e);
        }
        return false;
    }

    public static String getSelfName() {
        Minecraft m = Minecraft.getMinecraft();
        if (m != null) {
            if (m.thePlayer != null) {
                return m.thePlayer.getCommandSenderName();
            }
        }
        return null;
    }

}
