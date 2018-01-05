package server.model.objects;

import server.Server;

public class Object{

	public int objectId;
	public int objectX;
	public int objectY;
	public int height;
	public int face;
	public int type;
	public int newId;
	public int tick;
	public int clip = -1;

	public Object(int id, int x, int y, int height, int face, int type, int newId, int ticks){
		this.objectId = id;
		this.objectX = x;
		this.objectY = y;
		this.height = height;
		this.face = face;
		this.type = type;
		this.newId = newId;
		this.tick = ticks;
		Server.objectManager.addObject(this);
	}

	public void setClip(int clip){
		this.clip = clip;
	}

	public Objects toObjects(){
		Objects o = new Objects(this.newId, this.objectX, this.objectY, this.height, this.face, this.type, this.tick);
		return o;
	}
	
	public Object changeId(int newId){
		this.objectId = newId;
		return this;
	}
}