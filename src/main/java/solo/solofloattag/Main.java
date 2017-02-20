package solo.solofloattag;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityLevelChangeEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import solo.solobasepackage.util.Debug;
import solo.solobasepackage.util.Message;

public class Main extends PluginBase implements Listener{
	
	public Config config;
	public HashMap<String, Tag> tags = new HashMap<String, Tag>();
	public HashMap<String, String> queue = new HashMap<String, String>();
	
	public void onEnable(){
		this.getDataFolder().mkdirs();
		this.config = new Config(new File(this.getDataFolder(), "tag.yml"), Config.YAML);
		for(Map.Entry<String, Object> entry : this.config.getAll().entrySet()){
			try{
				String levelName = entry.getKey().split(":")[0];
				int x = Integer.parseInt(entry.getKey().split(":")[1]);
				int y = Integer.parseInt(entry.getKey().split(":")[2]);
				int z = Integer.parseInt(entry.getKey().split(":")[3]);
				Tag tag = new NormalTag(levelName, x, y, z, (String) entry.getValue());
				this.tags.put(this.getHash(tag), tag);
			}catch(Exception e){
				Debug.alert(this, "태그를 로드하던 중 오류가 발생하였습니다.");
			}
		}
		
		this.getServer().getPluginManager().registerEvents(this, this);
	}
	
	public void onDisable(){
		for(Map.Entry<String, Tag> entry : this.tags.entrySet()){
			this.config.set(this.getHash(entry.getValue()), entry.getValue().getMessage());
		}
		this.config.save();
	}
	
	public String getHash(Position pos){
		return pos.getLevel().getFolderName() + ":" + Integer.toString(pos.getFloorX()) + ":" + Integer.toString(pos.getFloorY()) + ":" + Integer.toString(pos.getFloorZ());
	}
	
	public String getHash(Tag tag){
		return tag.levelName + ":" + Integer.toString(tag.x) + ":" + Integer.toString(tag.y) + ":" + Integer.toString(tag.z);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event){
		this.tags.values().forEach((t) -> {
			t.spawnTo(event.getPlayer());
		});
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onTeleport(EntityLevelChangeEvent event){
		if(event.getEntity() instanceof Player){
			Player player = (Player) event.getEntity();
			this.tags.values().forEach((t) -> {
				t.spawnTo(player);
			});
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInteract(PlayerInteractEvent event){
		String name = event.getPlayer().getName().toLowerCase();
		if(this.queue.containsKey(name)){
			if(this.queue.get(name).equals("DELETE_MODE")){
				String hash = this.getHash(event.getBlock());
				if(this.tags.containsKey(hash)){
					this.tags.remove(name).despawnFromAll();
					Message.normal(event.getPlayer(), "태그를 제거하였습니다.");
				}
				return;
			}
			Tag tag = new NormalTag(
					event.getBlock().getLevel().getFolderName(),
					event.getBlock().getFloorX(),
					event.getBlock().getFloorY(),
					event.getBlock().getFloorZ(),
					this.queue.get(name)
				);
			this.tags.put(this.getHash(tag), tag);
			Message.normal(event.getPlayer(), "태그를 추가하였습니다.");
			this.queue.remove(name);
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		if(command.getName().equals("태그")){
			if(args.length == 0){
				args = new String[]{"x"};
			}
			switch(args[0]){
				case "추가":
					if(args.length <= 1){
						Message.usage(sender, "/태그 추가 [내용...]");
						return true;
					}
					StringBuilder sb = new StringBuilder();
					for(int i = 1; i < args.length; i++){
						sb.append(args[i]);
						if(i != args.length - 1){
							sb.append(" ");
						}
					}
					this.queue.put(sender.getName().toLowerCase(), sb.toString());
					Message.normal(sender, "태그를 추가할 위치에 터치하세요. 중지하려면 /태그 중지 명령어를 입력하세요.");
					return true;
					
				case "삭제":
					this.queue.put(sender.getName().toLowerCase(), "DELETE_MODE");
					Message.normal(sender, "태그 삭제 모드를 켰습니다. 중지하려면 /태그 중지 명령어를 입력하세요.");
					return true;
					
				case "중지":
					if(! this.queue.containsKey(sender.getName().toLowerCase())){
						Message.alert(sender, "진행중이던 작업이 없습니다.");
						return true;
					}
					this.queue.remove(sender.getName().toLowerCase());
					Message.normal(sender, "진행중이던 작업을 중단하였습니다.");
					return true;
					
				case "목록":
					ArrayList<String> information = new ArrayList<String>();
					for(Map.Entry<String, Tag> entry : this.tags.entrySet()){
						information.add(entry.getKey() + " => " + entry.getValue().getMessage());
					}
					int page = 1;
					try{
						page = Integer.parseInt(args[1]);
					}catch(Exception e){
						
					}
					Message.page(sender, "태그 목록", information, page);
					return true;
					
				default:
					LinkedHashMap<String, String> help = new LinkedHashMap<>();
					help.put("/태그 추가 [내용...]", "태그를 추가합니다.");
					help.put("/태그 삭제", "태그를 삭제합니다.");
					help.put("/태그 중지", "진행중이던 태그 작업을 중지합니다.");
					help.put("/태그 목록", "태그 목록을 봅니다.");
					Message.commandHelp(sender, "태그 명령어 목록", help);
					return true;
			}
		}
		return true;
	}
}