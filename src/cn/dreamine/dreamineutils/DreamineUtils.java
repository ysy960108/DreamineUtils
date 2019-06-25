package cn.dreamine.dreamineutils;

import cn.dreamine.dreamineutils.task.InformNumTask;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by ysy on 2019/6/22.
 */
public class DreamineUtils extends JavaPlugin implements Listener {

    private Map cfgMessages;
    private Map cfgStackSizes;
    private boolean cfgItemPerms;
    private int cfgStackSize;

    @Override
    public void onEnable(){
        getConfig().options().copyDefaults(true);
        saveConfig();
        reloadConfig();

        cfgMessages = getConfig().getConfigurationSection("message_formats").getValues(false);
        cfgStackSizes = getConfig().getConfigurationSection("special_size").getValues(false);
        cfgItemPerms = getConfig().getBoolean("per_item_perms");
        cfgStackSize = getConfig().getInt("stack_size");

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public  void  onDisable(){

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event){
        if(!(event.getPlayer().hasPermission("nonumbername.white"))){
            if(Pattern.compile("[0-9]*").matcher(event.getPlayer().getName()).matches()){
               // event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.translateAlternateColorCodes('&', getConfig().getString("numeric-username-kick-msg")));
                BukkitTask task = new InformNumTask(this,event.getPlayer()).runTaskTimerAsynchronously(this,100,1200);
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if(cmd.getName().equalsIgnoreCase("dmus") || cmd.getName().equalsIgnoreCase("dmustack")){
            if(sender instanceof Player)
            {
                if(sender.hasPermission("dreamine.stackitem"))
                {
                    if(((Player)sender).getInventory().getItemInMainHand().getType() != Material.AIR)
                    {
                        if(!cfgItemPerms || sender.hasPermission((new StringBuilder("dreamine.stackitem.")).append(((Player)sender).getInventory().getItemInMainHand().getType().toString()).toString()))
                        {
                            ItemStack item = ((Player)sender).getInventory().getItemInMainHand().clone();
                            HashMap items = ((Player)sender).getInventory().all(item.getType());
                            Integer amount = Integer.valueOf(item.getAmount());
                            Integer maxAmount = Integer.valueOf(cfgStackSize != 0 ? cfgStackSize : item.getType().getMaxStackSize());
                            if(cfgStackSizes.containsKey(item.getType().toString()))
                                maxAmount = Integer.valueOf(Integer.parseInt((String)cfgStackSizes.get(item.getType().toString())));
                            for(Iterator iterator = items.keySet().iterator(); iterator.hasNext();)
                            {
                                Integer tempSlot = (Integer)iterator.next();
                                if(((ItemStack)items.get(tempSlot)).isSimilar(item) && tempSlot.intValue() != ((Player)sender).getInventory().getHeldItemSlot())
                                {
                                    amount = Integer.valueOf(amount.intValue() + ((ItemStack)items.get(tempSlot)).getAmount());
                                    ((Player)sender).getInventory().clear(tempSlot.intValue());
                                }
                            }

                            if(amount.intValue() <= maxAmount.intValue())
                            {
                                ((Player)sender).getInventory().getItemInMainHand().setAmount(amount.intValue());
                            } else
                            {
                                item.setAmount(maxAmount.intValue());
                                ((Player)sender).getInventory().getItemInMainHand().setAmount(maxAmount.intValue());
                                for(int i = amount.intValue() - maxAmount.intValue(); i >= maxAmount.intValue(); i -= maxAmount.intValue())
                                    if(((Player)sender).getInventory().firstEmpty() != -1)
                                        ((Player)sender).getInventory().setItem(((Player)sender).getInventory().firstEmpty(), item);
                                    else
                                        ((Player)sender).getWorld().dropItem(((Player)sender).getLocation(), item);

                                if(amount.intValue() % maxAmount.intValue() > 0)
                                {
                                    item.setAmount(amount.intValue() % maxAmount.intValue());
                                    if(((Player)sender).getInventory().firstEmpty() != -1)
                                        ((Player)sender).getInventory().setItem(((Player)sender).getInventory().firstEmpty(), item);
                                    else
                                        ((Player)sender).getWorld().dropItem(((Player)sender).getLocation(), item);
                                }
                            }
                            sender.sendMessage(parseChatFormat(ChatColor.translateAlternateColorCodes('&', (String)cfgMessages.get("stacked"))).replaceAll("<TYPE>", ((Player)sender).getInventory().getItemInMainHand().getItemMeta().getDisplayName()));
                            return true;
                        } else
                        {
                            sender.sendMessage(parseChatFormat(ChatColor.translateAlternateColorCodes('&', (String)cfgMessages.get("no_itemperm"))).replaceAll("<PERM>", (new StringBuilder("itemstacker.stack.")).append(((Player)sender).getInventory().getItemInMainHand().getType().toString()).toString()));
                            return true;
                        }
                    } else
                    {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', (String)cfgMessages.get("no_item")));
                        return true;
                    }
                } else
                {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', (String)cfgMessages.get("no_perm")));
                    return true;
                }
            } else
            {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', (String)cfgMessages.get("no_player")));
                return true;
            }
        }
        if(cmd.getName().equalsIgnoreCase("dmureload")){
            onEnable();
        }
        return true;
    }

    public String parseChatFormat(String Message)
    {
        for(int i = Message.indexOf('&'); i != -1; i = Message.indexOf('&', i + 1))
        {
            if(i + 1 >= Message.length())
                break;
            if(i - 1 >= 0 && Message.charAt(i - 1) == '\\')
                Message = (new StringBuilder(String.valueOf(Message.substring(0, i - 1)))).append(Message.substring(i)).toString();
            else
                switch(Message.charAt(i + 1))
                {
                    case 48: // '0'
                    case 49: // '1'
                    case 50: // '2'
                    case 51: // '3'
                    case 52: // '4'
                    case 53: // '5'
                    case 54: // '6'
                    case 55: // '7'
                    case 56: // '8'
                    case 57: // '9'
                    case 65: // 'A'
                    case 66: // 'B'
                    case 67: // 'C'
                    case 68: // 'D'
                    case 69: // 'E'
                    case 70: // 'F'
                    case 75: // 'K'
                    case 76: // 'L'
                    case 77: // 'M'
                    case 78: // 'N'
                    case 79: // 'O'
                    case 82: // 'R'
                    case 97: // 'a'
                    case 98: // 'b'
                    case 99: // 'c'
                    case 100: // 'd'
                    case 101: // 'e'
                    case 102: // 'f'
                    case 107: // 'k'
                    case 108: // 'l'
                    case 109: // 'm'
                    case 110: // 'n'
                    case 111: // 'o'
                    case 114: // 'r'
                        Message = (new StringBuilder(String.valueOf(Message.substring(0, i)))).append('&').append(Message.substring(i + 1)).toString();
                        break;
                }
        }

        return Message;
    }


}
