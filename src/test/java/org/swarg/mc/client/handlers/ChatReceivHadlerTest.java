package org.swarg.mc.client.handlers;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.swarg.mc.client.handlers.ChatReceivHadler.getDiscordAccountName;
import static org.swarg.mc.client.handlers.ChatReceivHadler.inBlackList;
import static org.swarg.mc.client.handlers.ChatReceivHadler.removeColor;

/**
 *
 * @author Swarg
 */
public class ChatReceivHadlerTest
{

    public ChatReceivHadlerTest() {
    }

    @Test
    public void test_getDiscordAccountName() {
        System.out.println("getDiscordAccountName");

        int cap = 10;
        String[] l = new String[cap];
        String[] r = new String[cap];
        String[] e = new String[cap];
        int n = 0;
        final String pattern = "[§9Discord§f] <$name>: message-text";
        l[n] = "player";    e[n++]= "player";
        l[n] = "pla>yer";   e[n++]= "pla>yer";
        l[n] = "pla>:yer";  e[n++]= "pla>:yer";
        l[n] = "pla>: yer"; e[n++]= "pla";
        l[n] = "§4player§r";e[n++]= "player";

        for (int i = 0; i < n; i++) {
            String msg = pattern.replace("$name", l[i]);
            String exp = e[i];
            String res = getDiscordAccountName(msg);
            assertEquals(exp, res);
        }
    }

    //@Test
    public void test_() {
        System.out.println("_");
        String l = "§";
        char c0 = l.charAt(0);
        int ic = (int)c0;
        System.out.println("ic0=" + ic);
        if (l.length() >1) {
            char c1 = l.charAt(1);
            System.out.println("ic1=" + (int)c1 + " strlen = " + l.length());
        }
        //ic0=1042
        //ic1=167 strlen = 2
    }

    @Test
    public void test_removeColor() {
        System.out.println("");

        int cap = 10;
        String[] l = new String[cap];
        String[] r = new String[cap];
        String[] e = new String[cap];
        int n = 0;
        /*00*/l[n] = null;   e[n++] = null;
        /*01*/l[n] = "";     e[n++] = "";
        /*02*/l[n] = "n";    e[n++] = "n";
        /*03*/l[n] = "name"; e[n++] = "name";
        //
        /*04*/l[n] = "§4name§r";   e[n++] = "name";
        /*05*/l[n] = "§4n§rame§r"; e[n++] = "name";
        ///*06*/l[n] = "§§n§rame§r"; e[n++] = "§name";//broken

        for (int i = 0; i < n; i++) {
            String line = l[i];
            String exp = e[i];
            String res = removeColor(line);
            String inf = "i:"+i;
            assertEquals(inf, exp, res);
        }
    }

    @Test
    public void test_inBlackList() {
        System.out.println("inBlackList");
        List<String> bl = new ArrayList<String>();
        bl.add("name1");
        bl.add("name2");
        bl.add("name3");

        assertEquals(true, inBlackList("name1", bl));
        assertEquals(true, inBlackList("nAme1", bl));
        assertEquals(true, inBlackList("nAmE1", bl));
        assertEquals(true, inBlackList("NAmE1", bl));
        assertEquals(true, inBlackList("NAME3", bl));
    }

    //@Test
    //public void isMsgFromDiscord() {
        //final String msg = "[§9Discord§f] <Player>: check";
        ////final String hex = stringToHexString(ClientCommandBlackList.DISCORD_PREFIX);
        //System.out.println(hex); //5B 12A7 39446973636F7264 12A7 665D
        //final String hex2 = stringToHexString("[9Discordf]");
        //System.out.println(hex2);//5B   §  39446973636F7264      665D
        //boolean disc = (msg != null && msg.startsWith(DISCORD_PREFIX));
        //assertEquals(true, disc);
    //}

    @Test
    public void test_isMsgFromDiscord() {
        System.out.println("isMsgFromDiscord");
        String msg = "[§9Discord§f] <Player>: check";
        assertEquals(true, ChatReceivHadler.isMsgFromDiscord(msg));

        msg = "[Discord] <Player>: check";
        assertEquals(true, ChatReceivHadler.isMsgFromDiscord(msg));

        msg = "[] Discord] <Player>: check";
        assertEquals(false, ChatReceivHadler.isMsgFromDiscord(msg));

        msg = "[msg] Discord] <Player>: check";
        assertEquals(false, ChatReceivHadler.isMsgFromDiscord(msg));

        msg = " [Discord] <Player>: check";
        assertEquals(false, ChatReceivHadler.isMsgFromDiscord(msg));

        msg = " <Discord> <Player>: check";
        assertEquals(false, ChatReceivHadler.isMsgFromDiscord(msg));
    }


    @Test
    public void test_getPlayerNameFrom() {
        System.out.println("getPlayerNameFrom");

        String msg = "<Player> text";
        assertEquals("Player", ChatReceivHadler.getSenderPlayerNameFrom(msg));

        msg = "* Player emotion";
        assertEquals("Player", ChatReceivHadler.getSenderPlayerNameFrom(msg));

        msg = "[Player -> I] private";
        assertEquals("Player", ChatReceivHadler.getSenderPlayerNameFrom(msg));

        msg = "<a> text";
        assertEquals("a", ChatReceivHadler.getSenderPlayerNameFrom(msg));

        msg = "* a emotion";
        assertEquals("a", ChatReceivHadler.getSenderPlayerNameFrom(msg));

        msg = "[a -> I] private";
        assertEquals("a", ChatReceivHadler.getSenderPlayerNameFrom(msg));

        msg = "[name->I] private";
        assertEquals("name", ChatReceivHadler.getSenderPlayerNameFrom(msg));

        msg = "player2 whispers hellow";
        assertEquals("player2", ChatReceivHadler.getSenderPlayerNameFrom(msg));

        msg = "<player2 whispers hellow";
        assertEquals(null, ChatReceivHadler.getSenderPlayerNameFrom(msg));

        msg = "!player2 whispers hellow";
        assertEquals(null, ChatReceivHadler.getSenderPlayerNameFrom(msg));

        msg = "player ? whispers hellow";
        assertEquals(null, ChatReceivHadler.getSenderPlayerNameFrom(msg));

        msg = "<Name [ \\ ]> global-message";
        assertEquals("Name", ChatReceivHadler.getSenderPlayerNameFrom(msg));

        msg = "<Name [ \\ ]> global-message";
        assertEquals("Name", ChatReceivHadler.getSenderPlayerNameFrom(msg));

        msg = "<Name [ \\ ]> global-message";
        assertEquals("Name", ChatReceivHadler.getSenderPlayerNameFrom(msg));

        msg = "<Name3>: text";
        assertEquals("Name3", ChatReceivHadler.getSenderPlayerNameFrom(msg));

        msg = "[Town] <Player_Name>: message";
        assertEquals("Player_Name", ChatReceivHadler.getSenderPlayerNameFrom(msg));

        msg = "[Town] <a>: message";
        assertEquals("a", ChatReceivHadler.getSenderPlayerNameFrom(msg));

        //msg = "[Town] <a [ \\ ]>: message";
        //assertEquals("a", ClientCommandBlackList.getSenderPlayerNameFrom(msg));

        msg = "[Town2] <Brave Solder Name8>: message";
        assertEquals("Brave Solder Name8", ChatReceivHadler.getSenderPlayerNameFrom(msg));
    }


    @Test
    public void test_getName() {
        System.out.println("getName");
        String expName = "Brave Solder Player_Name";
        String[] args = ("add "+expName).split(" ");
        String resName = CommandChatGuard.getName(args, 1);
        assertEquals(expName, resName);

        assertEquals("Name", CommandChatGuard.getName("add Name".split(" "),1));

    }

}
