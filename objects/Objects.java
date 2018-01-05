package server.model.objects;

public class Objects{

	public int objectId;
	public int objectX;
	public int objectY;
	public int objectHeight;
	public int objectFace;
	public int objectType;
	public int objectTicks;
	public int itemId = -1;
	public int treeId = -1, treeSpawn = -1;
	public DwarfCannon cannon;

	public Objects(int id, int x, int y, int height, int face, int type, int ticks){
		this.objectId = id;
		this.objectX = x;
		this.objectY = y;
		this.objectHeight = height;
		this.objectFace = face;
		this.objectType = type;
		this.objectTicks = ticks;
	}
	
	public void addCannon(DwarfCannon cannon){
		this.cannon = cannon;
	}

	public void addItem(int id){
		itemId = id;
	}

	public Object toObject(){
		Object o = new Object(this.objectId, this.objectX, this.objectY, this.objectHeight, this.objectFace, this.objectType, this.objectId, this.objectTicks);
		return o;
	}

	public int getObjectId(){
		return this.objectId;
	}

	public int getObjectX(){
		return this.objectX;
	}

	public int getObjectY(){
		return this.objectY;
	}

	public int getObjectHeight(){
		return this.objectHeight;
	}

	public int getObjectFace(){
		return this.objectFace;
	}

	public int getObjectType(){
		return this.objectType;
	}

}