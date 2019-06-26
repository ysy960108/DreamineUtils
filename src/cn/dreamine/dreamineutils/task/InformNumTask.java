package cn.dreamine.dreamineutils.task;

import cn.dreamine.dreamineutils.DreamineUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by ysy on 2019/6/25.
 */
public class InformNumTask  extends BukkitRunnable {

    private DreamineUtils plugin;
    private Player player;

    public InformNumTask(DreamineUtils plugin, Player player){
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void run(){
        player.sendTitle(ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("numeric-username-title")),
                ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("numeric-username-subtitle")),
                20,100,20);
    }

}