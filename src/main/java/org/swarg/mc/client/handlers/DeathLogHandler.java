package org.swarg.mc.client.handlers;

import java.io.File;
import java.time.ZoneId;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;

import org.apache.commons.io.FileUtils;
import static net.minecraft.util.StringUtils.isNullOrEmpty;

/**
 * 13-02-21
 * @author Swarg
 * Death coords logger to client file
 */
public class DeathLogHandler {
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final File DEATH_LOGFILE = new File("death.log");
    private int lastDeathX;
    private int lastDeathY;
    private int lastDeathZ;


    /**
     * 
     * @param event 
     */
    @SubscribeEvent
    //@SideOnly(Side.CLIENT)
    public void onClientGUI(net.minecraftforge.client.event.GuiOpenEvent event) {
        if (event.gui != null && event.gui.getClass() == net.minecraft.client.gui.GuiGameOver.class) {
            ///*DEBUG*/ ClientCommandBlackList.LOG.info("[###] [onGuiGameOver]");
            Minecraft m = Minecraft.getMinecraft();
            if (m != null && m.thePlayer != null) {
                final int deathX = (int) m.thePlayer.posX;
                final int deathY = (int) m.thePlayer.posY;
                final int deathZ = (int) m.thePlayer.posZ;
                if (lastDeathY != deathY || lastDeathX != deathX || lastDeathZ != deathZ) {
                    String line = "[" + Instant.ofEpochMilli( System.currentTimeMillis() ).atZone(ZoneId.systemDefault()).format(DT_FORMAT)
                            //todo server name
                            + "] Death x:" + deathX + " y:" + deathY + " z:" + deathZ + "\n";
                    ///*DEBUG*/ ClientCommandBlackList.LOG.info(line);
                    deathlog( line );
                    this.lastDeathX = deathX;
                    this.lastDeathY = deathY;
                    this.lastDeathZ = deathZ;
                }
            }            
        }
    }

    /**
     * Append mode
     * @param line
     * @return
     */
    public boolean deathlog(String line) {
        if (!isNullOrEmpty(line)) {
            try {
                FileUtils.write(DEATH_LOGFILE, line, "UTF-8", true);
                return true;
            }
            catch (Exception e) {
                ///*DEBUG*/ClientCommandBlackList.LOG.catching(Level.ERROR, e);
            }
        }
        return false;
    }

    ////Side.CLIENT
    //@SubscribeEvent
    //dont work in client side
    //public void onDeath(LivingDeathEvent event) {
    //    if (event != null && event.entityLiving instanceof EntityPlayer) {
    //        final String name = event.entityLiving.getCommandSenderName();//((EntityPlayer) event.entityLiving).getCommandSenderName();
    //        /*DEBUG*/ ClientCommandBlackList.LOG.info("[###] [onDeath] {} ", name);
    //        if (name != null && name.equals( getSelfName() )) {
    //            int deathX = (int) event.entityLiving.posX;
    //            int deathY = (int) event.entityLiving.posY;
    //            int deathZ = (int) event.entityLiving.posZ;
    //            String line = Instant.ofEpochMilli( System.currentTimeMillis() ).atZone(ZoneId.systemDefault()).format(DT_FORMAT)
    //                    //todo server name
    //                    + "Death Coords x:" + deathX + " y:" + deathY + " z:" + deathZ;
    //            /*DEBUG*/ ClientCommandBlackList.LOG.info(line);
    //            deathlog( line );
    //        }
    //    }
    //}

}
