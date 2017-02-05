package solo.solofloattag;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.level.Level;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.MoveEntityPacket;
import cn.nukkit.network.protocol.RemoveEntityPacket;

public abstract class Tag{

	//Position data
	public String levelName;
	public int x, y, z;
	
	//Entity Data
	public long eid;
	protected AddEntityPacket addPk;
	protected MoveEntityPacket movePk;
	protected RemoveEntityPacket removePk;
	
	public String message;
	
	public Tag(String levelName, int x, int y, int z, String message){
		this.levelName = levelName;
		this.x = x;
		this.y = y;
		this.z = z;
		
		this.message = message;
		
		this.init();
	}
	
	public void init(){
		this.eid = Entity.entityCount++;
		
		this.addPk = new AddEntityPacket();
		this.addPk.entityUniqueId = this.eid;
		this.addPk.entityRuntimeId = this.eid;
		this.addPk.type = 15; //villager
		this.addPk.x = (float) (this.x + 0.5);
		this.addPk.y = (float) (this.y);
		this.addPk.z = (float) (this.z + 0.5);
		this.addPk.speedX = 0;
		this.addPk.speedY = 0;
		this.addPk.speedZ = 0;
		this.addPk.yaw = 0;
		this.addPk.pitch = 0;
		
		long flags = 0;
		flags |= 1 << Entity.DATA_FLAG_INVISIBLE;
		flags |= 1 << Entity.DATA_FLAG_CAN_SHOW_NAMETAG;
		flags |= 1 << Entity.DATA_FLAG_ALWAYS_SHOW_NAMETAG;
		flags |= 1 << Entity.DATA_FLAG_NO_AI;
		EntityMetadata metadata = new EntityMetadata()
				.putLong(Entity.DATA_FLAGS, flags)
				.putShort(Entity.DATA_AIR, 400)
				.putShort(Entity.DATA_MAX_AIR, 400)
				.putString(Entity.DATA_NAMETAG, this.getMessage())
				.putLong(Entity.DATA_LEAD_HOLDER_EID, -1)
				.putFloat(Entity.DATA_SCALE, 0.001f);
		
		this.addPk.metadata = metadata;
		
		this.movePk = new MoveEntityPacket();
		this.movePk.eid = this.eid;
		this.movePk.x = (float) (this.x + 0.5);
		this.movePk.y = (float) (this.y);
		this.movePk.z = (float) (this.z + 0.5);

		this.removePk = new RemoveEntityPacket();
		this.removePk.eid = this.eid;
		
		this.spawnToAll();
	}
	
	public String getMessage(){
		return this.message;
	}
	
	public void despawnFrom(Player player){
		player.dataPacket(this.removePk);
	}
	
	public void despawnFromAll(){
		Server.getInstance().getOnlinePlayers().values().forEach((p) -> this.despawnFrom(p));;
	}
	
	public void spawnTo(Player player){
		this.spawnTo(player, player.getLevel());
	}
	
	public void spawnTo(Player player, Level level){
		if(level == null || ! levelName.equals(level.getFolderName())){
			this.despawnFrom(player);
			return;
		}
		player.dataPacket(this.addPk);
		player.dataPacket(this.movePk);
	}
	
	public void spawnToAll(){
		Server.getInstance().getOnlinePlayers().values().forEach((p) -> this.spawnTo(p));;
	}
}