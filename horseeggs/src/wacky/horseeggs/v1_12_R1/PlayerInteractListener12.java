package wacky.horseeggs.v1_12_R1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftAbstractHorse;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LlamaInventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import wacky.horseeggs.HorseEggs;

public class PlayerInteractListener12 implements Listener{

	private HorseEggs plugin;

	public PlayerInteractListener12(HorseEggs plugin){
    	this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event){
		if(event.isCancelled()) return;
		Player player = event.getPlayer();
		PlayerInventory inv = player.getInventory();
		Entity entity = event.getRightClicked();
		AbstractHorse horse;
		if(entity instanceof AbstractHorse){//馬系
			horse = (AbstractHorse)entity;
		}else{
			return;
		}
		ItemStack itemInHand = player.getInventory().getItemInMainHand();
		if(plugin.isHorseEgg(itemInHand)){//offhandの時は先に馬に乗る処理になるはず
			event.setCancelled(true);//子馬が生まれないように
			if(!player.hasPermission("horseeggs.release")) return;
			Location loc = event.getRightClicked().getLocation();
			new ReleaseHorse(itemInHand, loc);
			int amount = itemInHand.getAmount();
			if(plugin.config.getBoolean("single-use")){
				if(amount == 1) inv.setItemInMainHand(null);
				else itemInHand.setAmount(amount -1);
			}else if(itemInHand.getAmount() == 1) inv.setItemInMainHand(plugin.emptyHorseEgg(1));
			else{
				itemInHand.setAmount(amount -1);
				inv.setItemInMainHand(itemInHand);
				if(inv.firstEmpty() == -1 || inv.firstEmpty() >= 36){
					loc.add(0, 0.5, 0);
					horse.getWorld().dropItem(loc, plugin.emptyHorseEgg(1));
				}else{
					player.getInventory().addItem(plugin.emptyHorseEgg(1));
				}
			}
			return;

			//何かが乗ってる時は回収不能、子馬・出した直後もNG
		}else if(plugin.isEmptyHorseEgg(itemInHand) && horse.isAdult() && horse.getPassengers().isEmpty() && horse.getAge() < 5980){

			event.setCancelled(true);//馬に卵を使ったことになるんだとか
			if(!player.hasPermission("horseeggs.capture")) return;

			EntityType type = horse.getType();
			ItemStack horseegg = new ItemStack(Material.MONSTER_EGG, 1);
			NBTTagCompound tag = new NBTTagCompound();//見た目を馬卵にする方法
			net.minecraft.server.v1_12_R1.ItemStack stack = CraftItemStack.asNMSCopy(horseegg);
			NBTTagCompound id = new NBTTagCompound();
			id.setString("id", type.toString());//大文字だけ
			tag.set("EntityTag", id);
			NBTTagCompound horseData = new NBTTagCompound();
			List<String> list = new ArrayList<String>();

			//名前
			if(horse.getCustomName() != null) horseData.setString("Name", horse.getCustomName());
			//体力
			horseData.setDouble("Health",horse.getHealth());
			horseData.setDouble("MaxHealth", horse.getMaxHealth());
			list.add("HP: " + (int)horse.getHealth() +"/"+ (int)horse.getMaxHealth());

			//速度、43倍すると実際の速度に
			NBTTagCompound tag2 = new NBTTagCompound();
			((CraftAbstractHorse)horse).getHandle().b(tag2);
			NBTTagList attributes = tag2.getList("Attributes", 10);
			for (int i=0; i<attributes.size(); i++) {
				NBTTagCompound attr = attributes.get(i);
				if (attr.getString("Name").equals("generic.movementSpeed")) {
					Double speed = attr.getDouble("Base");
					horseData.setDouble("Speed", speed);
					if(Double.toString(speed*43).length() > 6) list.add("Speed: " + Double.toString(speed*43).substring(0, 6));
					else list.add("Speed: " + Double.toString(speed*43));
				}
			}

			//跳躍力、NBTにのみ書かれるべき
			Double jump = horse.getJumpStrength();
			horseData.setDouble("Jump", jump);

			double jumpHeight = 0;//from Zyin's HUD、ジャンプ高度
			while (jump > 0)
			{
				jumpHeight += jump;
				jump -= 0.08;
				jump *= 0.98;
			}
			if(Double.toString(jumpHeight).length() > 5) list.add("Height: " + Double.toString(jumpHeight).substring(0, 5));
			else list.add("Height: " + Double.toString(jumpHeight));


			horseData.setString("Type", horse.getType().toString());
			horseData.setString("Variant", horse.getVariant().toString());
			
			if(horse.getType() == EntityType.LLAMA){
				horseData.setInt("Strength", ((Llama) horse).getStrength());
				list.add("Strength: " + ((Llama) horse).getStrength());
				
				horseData.setString("Color", ((Llama) horse).getColor().toString());
				list.add(horse.getType().toString() + "/" + ((Llama) horse).getColor().toString());
			}
			else if(horse.getType() != EntityType.HORSE){//馬以外
				list.add(horse.getType().toString());
			}
			else{//馬
				horseData.setString("Color", ((Horse) horse).getColor().toString());
				horseData.setString("Style", ((Horse) horse).getStyle().toString());
				list.add(((Horse) horse).getColor().toString() + "/" + ((Horse) horse).getStyle().toString());

			}

			Location loc = horse.getLocation();
			loc.add(0, 0.5, 0);
			if(horse.isTamed()){//飼いならした人、UUIDを内部的には使用する。
				if(horse.getOwner() != null){
					AnimalTamer owner = horse.getOwner();
					horseData.setLong("UUIDMost", owner.getUniqueId().getMostSignificantBits());
					horseData.setLong("UUIDLeast", owner.getUniqueId().getLeastSignificantBits());
					list.add("Owner: " + owner.getName());
				}

				AbstractHorseInventory hInv = horse.getInventory();
				//サドル
				horseData.setBoolean("Saddle",hInv.getSaddle() != null);
				String str1 = hInv.getSaddle() == null ? "" : "[SADDLE]";

				String str2 = "";
				if(type == EntityType.HORSE && ((HorseInventory) hInv).getArmor() != null){
					horseData.setString("Armor", ((HorseInventory) hInv).getArmor().getType().toString());
					str2 = "[" + ((HorseInventory) hInv).getArmor().getType().toString() + "]";
				}else if(type == EntityType.LLAMA && ((LlamaInventory) hInv).getDecor() != null){
					horseData.setString("Armor", ((LlamaInventory) hInv).getDecor().getType().toString());
					horseData.setShort("Decor", ((LlamaInventory) hInv).getDecor().getDurability());
					str2 = "[" + ((LlamaInventory) hInv).getDecor().getType().toString() + ":" + ((LlamaInventory) hInv).getDecor().getDurability() + "]";
				}
				if(entity instanceof ChestedHorse){
					horseData.setBoolean("Chest", ((ChestedHorse) horse).isCarryingChest());
					if(((ChestedHorse) horse).isCarryingChest()) str2 = str2 + "[CHEST]";//ラマがカーペットとチェスト両持ちできる
				}else{
					horseData.setBoolean("Chest", false);
				}

				if((str1 + str2).length() > 0) list.add(str1 + str2);

				for(int i = 2; i <hInv.getSize();i++){//チェストと鎧?を除く
					if(hInv.getItem(i) == null) continue;
					horse.getWorld().dropItem(loc, hInv.getItem(i));
				}
			}

			tag.set("HorseEgg",horseData);
			stack.setTag(tag);
			horseegg = CraftItemStack.asBukkitCopy(stack);
			ItemMeta meta = horseegg.getItemMeta();
			meta.setDisplayName(horse.getCustomName());
			meta.setLore(list);
			horseegg.setItemMeta(meta);

			if(inv.getItemInMainHand().getAmount() == 1) inv.setItemInMainHand(horseegg);
			else{
				itemInHand.setAmount(itemInHand.getAmount() -1);
				if(inv.firstEmpty() == -1 || inv.firstEmpty() >= 36){
					horse.getWorld().dropItem(loc, horseegg);
				}else inv.addItem(horseegg);
			}
			horse.remove();
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		Player player = event.getPlayer();
		PlayerInventory inv = player.getInventory();
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

			//馬がめり込まないようにしたい
			Block centerBlock = event.getClickedBlock().getRelative(event.getBlockFace());
			Location loc = centerBlock.getLocation();
			Boolean[][] blocks = new Boolean[5][5];
			Arrays.fill(blocks[0], false);
			Arrays.fill(blocks[4], false);
			boolean canSpawnCenter = true;
			for(int i = 1; i <= 3; i++){
				Arrays.fill(blocks[i], false);
				for(int j = 1; j <= 3; j++){
					blocks[i][j] = isSuffocating(centerBlock.getRelative(i-2, 1, j-2).getType());
					if(blocks[i][j]) canSpawnCenter = false;
				}
			}
			if(canSpawnCenter){
				loc.add(0.5, 0, 0.5);
				new ReleaseHorse(item, loc);
			}
			else search:{//どっかにブロック有り。
				for(int i = 0; i < 3; i++){
					for(int j = 0; j < 3; j++){//周囲9マス()にブロックが無いか
						Boolean canSpawn = !blocks[i][j] && !blocks[i][j+1] && !blocks[i][j+2] && !blocks[i+1][j] && !blocks[i+1][j+1] && !blocks[i+1][j+2] && !blocks[i+2][j] && !blocks[i+2][j+1] && !blocks[i+2][j+2];
						if(canSpawn){
							loc.add(i*0.5, 0, j*0.5);
							new ReleaseHorse(item, loc);
							break search;
						}
					}
				}//スポーン失敗。
				event.setCancelled(true);
				return;
			}

			//オフハンド対策
			int amount = item.getAmount();
			if(plugin.config.getBoolean("single-use")){
				if(amount == 1){
					if(event.getHand() == EquipmentSlot.HAND) inv.setItemInMainHand(null);
					else inv.setItemInOffHand(null);
				}
				else item.setAmount(amount -1);
			}else if(item.getAmount() == 1){
				if(event.getHand() == EquipmentSlot.HAND) inv.setItemInMainHand(plugin.emptyHorseEgg(1));
				else inv.setItemInOffHand(plugin.emptyHorseEgg(1));
			}
			else{
				item.setAmount(amount -1);
				if(inv.firstEmpty() == -1 || inv.firstEmpty() >= 36){
					loc.add(0, 0.5, 0);
					loc.getWorld().dropItem(loc, plugin.emptyHorseEgg(1));
				}else{
					inv.addItem(plugin.emptyHorseEgg(1));
				}
			}
		}
	}

	private boolean isSuffocating(Material mat){
		if(mat.isOccluding()) return true;//窒息する透過ブロック
		else if(mat == Material.TNT || mat == Material.ICE || mat == Material.GLOWSTONE || mat == Material.REDSTONE_BLOCK || mat == Material.SEA_LANTERN){
			return true;
		}
		return false;
	}
}
