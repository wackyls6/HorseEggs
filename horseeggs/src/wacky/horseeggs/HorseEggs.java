package wacky.horseeggs;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import wacky.horseeggs.v1_10_R1.PlayerInteractListener10;
import wacky.horseeggs.v1_11_R1.PlayerInteractListener11;
import wacky.horseeggs.v1_12_R1.PlayerInteractListener12;
import wacky.horseeggs.v1_9_R2.PlayerInteractListener9;


public class HorseEggs extends JavaPlugin implements Listener{

	public FileConfiguration config;
	String version;

	@Override
	public void onEnable() {
		VersionChecker vc = new VersionChecker();
		version = vc.check();
		if(version.equals("other")){
			this.getLogger().warning("Version error.");
			this.setEnabled(false);
			return;
		}

		config = this.getConfig();
		config.options().copyDefaults(true);
		config.options().header("HorseEggs Configuration");
		this.saveConfig();

		ShapedRecipe storageSignRecipe = new ShapedRecipe(emptyHorseEgg(1));
		storageSignRecipe.shape(" P ","PEP"," P ");
		storageSignRecipe.setIngredient('P', Material.ENDER_PEARL);
		storageSignRecipe.setIngredient('E', Material.EGG);
		getServer().addRecipe(storageSignRecipe);

		getServer().getPluginManager().registerEvents(this, this);

		if(version.equals("v1_12_R1")){
			new PlayerInteractListener12(this);
		}else if(version.equals("v1_11_R1")){
			new PlayerInteractListener11(this);
		}else if(version.equals("v1_10_R1")){
			new PlayerInteractListener10(this);
		}else if(version.equals("v1_9_R2")){
			new PlayerInteractListener9(this);
		}
		//new ItemDespawnListener(this);
	}

	@Override
	public void onDisable(){}

	@EventHandler
	public void onBlockDispense(BlockDispenseEvent event){
		if(event.isCancelled() || event.getBlock().getType() == Material.DROPPER) return;
		if(isHorseEgg(event.getItem()) || isEmptyHorseEgg(event.getItem())){
			event.setCancelled(true);//仕様変更用にキャンセルだけ.凍結中
			/*
			Dispenser dispenserM = (Dispenser) event.getBlock().getState().getData();
			Location loc = event.getBlock().getRelative(dispenserM.getFacing()).getLocation();
			loc.add(0.5, 0.2, 0.5);
			releaseHorse(event.getItem(),loc);
			org.bukkit.block.Dispenser dispenserS = (org.bukkit.block.Dispenser)event.getBlock().getState();
			dispenserS.getInventory().remove(event.getItem());
			*/
		}
	}

	//定義があるのは空だけ
	public ItemStack emptyHorseEgg(int i){
		ItemStack egg = new ItemStack(Material.MONSTER_EGG, i);
		ItemMeta meta = egg.getItemMeta();
		meta.setDisplayName("HorseEgg");
		List<String> lore = new ArrayList<String>();
		lore.add("Empty");
		meta.setLore(lore);
		egg.setItemMeta(meta);
		return egg;
	}

	public boolean isEmptyHorseEgg(ItemStack item){//1.9では全てダメージ値0なので変更が必要かも
		if(item.getType() == Material.MONSTER_EGG && item.getItemMeta().hasLore()){
			if(item.getItemMeta().getLore().get(0).equals("Empty")) return true;
		}
		return false;
	}

	public boolean isHorseEgg(ItemStack item){//1.8まではダメージ値100、1.9ではメタ内にエンティティ記載あり
		if(item.getType() == Material.MONSTER_EGG && item.getItemMeta().hasLore()){
			if(item.getItemMeta().getLore().size() >= 3) return true;
		}
		return false;
	}

	public boolean isClickable(Block block) {//わざわざ作らないかんのか(困惑)
		switch(block.getType()){
		case ANVIL:
		case BEACON:
		case BED_BLOCK:
		case BIRCH_DOOR:
		case BIRCH_FENCE_GATE:
		case BREWING_STAND:
		case BURNING_FURNACE:
		case CAKE_BLOCK:
		case CHEST:
		case COMMAND:
		case DARK_OAK_DOOR:
		case DARK_OAK_FENCE_GATE:
		case DAYLIGHT_DETECTOR:
		case DAYLIGHT_DETECTOR_INVERTED:
		case DIODE_BLOCK_OFF:
		case DIODE_BLOCK_ON:
		case DISPENSER:
		case DROPPER:
		case ENCHANTMENT_TABLE:
		case ENDER_CHEST:
		case FENCE_GATE:
		case FURNACE:
		case HOPPER:
		case IRON_DOOR_BLOCK:
		case IRON_TRAPDOOR:
		case JUNGLE_DOOR:
		case JUNGLE_FENCE_GATE:
		case LEVER:
		case NOTE_BLOCK:
		case REDSTONE_COMPARATOR_OFF:
		case REDSTONE_COMPARATOR_ON:
		case SIGN_POST:
		case SPRUCE_DOOR:
		case SPRUCE_FENCE_GATE:
		case STONE_BUTTON:
		case TRAP_DOOR:
		case TRAPPED_CHEST:
		case WALL_SIGN:
		case WOOD_BUTTON:
		case WOOD_DOOR:
		case WOODEN_DOOR:
		case WORKBENCH:
			return true;
		default:
			return false;
		}
	}

	public String getVersion(){
		return this.version;
	}
}
