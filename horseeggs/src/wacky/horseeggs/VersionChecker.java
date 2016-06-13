package wacky.horseeggs;


public class VersionChecker {
	String version;
	VersionChecker(String version) {
		this.version = version;
	}
//てすと
	public boolean check(){
		try{
			Class<?> c;
			c = Class.forName("org.bukkit.craftbukkit." + version + ".CraftServer");
			c.getName();
		}catch(ClassNotFoundException e){
			return false;
		}
		return true;
	}
}
