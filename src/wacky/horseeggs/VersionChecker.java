package wacky.horseeggs;


public class VersionChecker {
	String[] versions = {"v1_18_R2"};

	public VersionChecker() {
		// TODO 自動生成されたコンストラクター・スタブ.
	}

	public String check(){
		for(int i = 0; i<versions.length; i++){

			try{
				Class<?> c;
				c = Class.forName("org.bukkit.craftbukkit." + versions[i] + ".CraftServer");
				c.getName();
			}catch(ClassNotFoundException e){
				continue;
			}
			return versions[i];
		}
		return "other";
	}
}