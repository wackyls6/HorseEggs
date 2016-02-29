package wacky.horseeggs;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.NBTTagList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerInteractListener implements Listener{

	private HorseEggs plugin;

	public PlayerInteractListener(HorseEggs plugin){
    	this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event){
		if(event.isCancelled()) return;
		Player player = event.getPlayer();
		Entity entity = event.getRightClicked();
		Horse horse;
		if(entity.getType() == EntityType.HORSE){
			horse = (Horse)entity;
		}else{
			return;
		}
		ItemStack itemInHand = player.getItemInHand();
		if(plugin.isHorseEgg(itemInHand)){
			event.setCancelled(true);//子馬が生まれないように
			if(!player.hasPermission("horseeggs.release")) return;
			Location loc = event.getRightClicked().getLocation();
			new ReleaseHorse(itemInHand, loc);
			int amount = itemInHand.getAmount();
			if(plugin.config.getBoolean("single-use")){
				if(amount == 1) player.setItemInHand(null);
				else itemInHand.setAmount(amount -1);
			}else if(itemInHand.getAmount() == 1) player.setItemInHand(plugin.emptyHorseEgg(1));
			else{
				itemInHand.setAmount(amount -1);
				player.setItemInHand(itemInHand);
				if(player.getInventory().firstEmpty() == -1 || player.getInventory().firstEmpty() >= 36){
					loc.add(0, 0.5, 0);
					horse.getWorld().dropItem(loc, plugin.emptyHorseEgg(1));
				}else{
					player.getInventory().addItem(plugin.emptyHorseEgg(1));
				}
			}
			return;

		}else if(plugin.isEmptyHorseEgg(itemInHand) && horse.isAdult() && horse.getAge() < 5980){

			event.setCancelled(true);//馬に卵を使ったことになるんだとか
			if(!player.hasPermission("horseeggs.capture")) return;
			ItemStack horseegg = new ItemStack(Material.MONSTER_EGG, 1, (short) 100);
			ItemMeta meta = horseegg.getItemMeta();
			meta.setDisplayName(horse.getCustomName());
			List<String> list = new ArrayList<String>();
			list.add("HP: " + (int)horse.getHealth() +"/"+ (int)horse.getMaxHealth());

			//互換性に大問題
			NBTTagCompound tag = new NBTTagCompound();
			((CraftHorse)horse).getHandle().b(tag);
			NBTTagList attributes = tag.getList("Attributes", 10);
			for (int i=0; i<attributes.size(); i++) {
				NBTTagCompound attr = attributes.get(i);
				if (attr.getString("Name").equals("generic.movementSpeed")) {
					list.add("Speed: " +  attr.getDouble("Base") *43);
				}
			}
			Double jump = horse.getJumpStrength();
			list.add("Jump: " + jump);

			double jumpHeight = 0;//from Zyin's HUD
			while (jump > 0)
			{
				jumpHeight += jump;
				jump -= 0.08;
				jump *= 0.98;
			}
			list.add("Height: " + jumpHeight);

			if(horse.getVariant() != Variant.HORSE) list.add(horse.getVariant().toString());
			else{
				list.add(horse.getColor().toString() + "/" + horse.getStyle().toString());
			}

			Location loc = horse.getLocation();
			loc.add(0, 0.5, 0);
			if(horse.getOwner() != null){
				list.add("Owner: " + horse.getOwner().getName());

				HorseInventory inv = horse.getInventory();
				String str1 = inv.getSaddle() == null ? "" : "[SADDLE]";
				String str2 = "";
				if(inv.getArmor() != null) str2 = "[" + inv.getArmor().getType().toString() + "]";
				if(horse.isCarryingChest()) str2 = "[CHEST]";
				if((str1 + str2).length() > 0) list.add(str1 + str2);

				for(int i = 2; i <inv.getSize();i++){
					if(inv.getItem(i) == null) continue;
					horse.getWorld().dropItem(loc, inv.getItem(i));
				}
			}
			meta.setLore(list);
			horseegg.setItemMeta(meta);

			if(player.getItemInHand().getAmount() == 1) player.setItemInHand(horseegg);
			else{
				itemInHand.setAmount(itemInHand.getAmount() -1);
				if(player.getInventory().firstEmpty() == -1 || player.getInventory().firstEmpty() >= 36){
					horse.getWorld().dropItem(loc, horseegg);
				}else player.getInventory().addItem(horseegg);
			}
			horse.remove();
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		if(item == null) return;
		if(plugin.isHorseEgg(item)){
			event.setUseItemInHand(Result.DENY);
			if(event.isCancelled()){//まさかの水源、溶岩源を右クリックした場合、cancelledのはずだけどスポーンエッグは使える
				return;
			}
			if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
			if(!player.isSneaking()  && plugin.isClickable(event.getClickedBlock())) return;//ブロック優先
			event.setUseInteractedBlock(Result.DENY);
			if(!player.hasPermission("horseeggs.release")){
				event.setCancelled(true);
				return;
			}
			Location loc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
			loc.add(0.5, 0, 0.5);
			new ReleaseHorse(item, loc);

			//一応HorseEggをスタックしてても動くように
			int amount = item.getAmount();
			if(plugin.config.getBoolean("single-use")){
				if(amount == 1) player.setItemInHand(null);
				else item.setAmount(amount -1);
			}else if(item.getAmount() == 1) player.setItemInHand(plugin.emptyHorseEgg(1));
			else{
				item.setAmount(amount -1);
				player.setItemInHand(item);
				if(player.getInventory().firstEmpty() == -1 || player.getInventory().firstEmpty() >= 36){
					loc.add(0, 0.5, 0);
					player.getWorld().dropItem(loc, plugin.emptyHorseEgg(1));
				}else{
					player.getInventory().addItem(plugin.emptyHorseEgg(1));
				}
			}
		}
	}
}
