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

import wacky.horseeggs.v1_18_R2.PlayerInteractListener18;



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

		if(version.equals("v1_18_R2")){
			new PlayerInteractListener18(this);
		}
		//new ItemDespawnListener(this);.
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
		ItemStack egg = new ItemStack(Material.GHAST_SPAWN_EGG, i);
		ItemMeta meta = egg.getItemMeta();
		meta.setDisplayName("HorseEgg");
		List<String> lore = new ArrayList<String>();
		lore.add("Empty");
		meta.setLore(lore);
		egg.setItemMeta(meta);
		return egg;
	}

	public boolean isEmptyHorseEgg(ItemStack item){//1.13では白い馬卵が無い
		if(item.getType() == Material.GHAST_SPAWN_EGG || item.getType() == Material.PIG_SPAWN_EGG && item.getItemMeta().hasLore()){
			if(item.getItemMeta().getLore().get(0).equals("Empty")) return true;
		}
		return false;
	}

	public boolean isHorseEgg(ItemStack item){//1.8まではダメージ値100、1.9ではメタ内にエンティティ記載あり
		if(item.getType() == Material.HORSE_SPAWN_EGG || item.getType() == Material.ZOMBIE_HORSE_SPAWN_EGG || item.getType() == Material.SKELETON_HORSE_SPAWN_EGG || item.getType() == Material.DONKEY_SPAWN_EGG ||item.getType() == Material.MULE_SPAWN_EGG || item.getType() == Material.LLAMA_SPAWN_EGG){
			if(item.getItemMeta().hasLore() && item.getItemMeta().getLore().size() >= 3) return true;
		}
		return false;
	}

	public boolean isClickable(Block block) {//名前変わりすぎ
		switch(block.getType()){
		case ANVIL:
		case ACACIA_SIGN:
		case ACACIA_WALL_SIGN:
		case BEACON:
		case BIRCH_DOOR:
		case BIRCH_TRAPDOOR:
		case BIRCH_SIGN:
		case BIRCH_WALL_SIGN:
		case BIRCH_FENCE_GATE:
		case BREWING_STAND:
		case FURNACE:
		case CAKE:
		case CRIMSON_BUTTON :
		case CRIMSON_DOOR :
		case CRIMSON_FENCE_GATE :
		case CRIMSON_SIGN :
		case CRIMSON_TRAPDOOR :
		case CRIMSON_WALL_SIGN :
		case CHEST:
		case COMMAND_BLOCK:
		case DARK_OAK_BUTTON:
		case DARK_OAK_DOOR:
		case DARK_OAK_SIGN:
		case DARK_OAK_TRAPDOOR:
		case DARK_OAK_FENCE_GATE:
		case DARK_OAK_WALL_SIGN:
		case DAYLIGHT_DETECTOR:
		case REPEATER:
		case DISPENSER:
		case DROPPER:
		case ENCHANTING_TABLE:
		case ENDER_CHEST:
		case HOPPER:
		case IRON_DOOR:
		case IRON_TRAPDOOR:
		case JUNGLE_DOOR:
		case JUNGLE_FENCE_GATE:
		case JUNGLE_SIGN:
		case JUNGLE_WALL_SIGN:
		case LEVER:
		case NOTE_BLOCK:
		case COMPARATOR:
		case SPRUCE_DOOR:
		case SPRUCE_FENCE_GATE:
		case SPRUCE_SIGN:
		case SPRUCE_WALL_SIGN:
		case STONE_BUTTON:
		case TRAPPED_CHEST:
		case WARPED_SIGN:
		case WARPED_TRAPDOOR:
		case WARPED_WALL_SIGN:
		case OAK_BUTTON:
		case OAK_DOOR:
		case OAK_TRAPDOOR:
		case OAK_SIGN:
		case OAK_WALL_SIGN:
		case CRAFTING_TABLE:
		case RED_BED:
		case ORANGE_BED:
		case YELLOW_BED:
		case LIME_BED:
		case GREEN_BED:
		case CYAN_BED:
		case BLUE_BED:
		case PURPLE_BED:
		case MAGENTA_BED:
		case LIGHT_BLUE_BED:
		case PINK_BED:
		case BROWN_BED:
		case WHITE_BED:
		case GRAY_BED:
		case LIGHT_GRAY_BED:
		case BLACK_BED:
			return true;
		default:
			return false;
		}
	}

	public String getVersion(){
		return this.version;
	}
}
