package server.model.region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import server.Server;
import server.model.items.GroundItem;
import server.model.npcs.NPC;
import server.model.players.Client;
import server.world.GameObject;

/**
 * Represents a single region.
 * 
 * @author Graham Edgecombe
 * @author Thomas Nappo
 */
public class Region{

	/**
	 * The region coordinates.
	 */
	private RegionCoordinates coordinate;

	/**
	 * A list of players in this region.
	 */
	private List<Client> players = new LinkedList<Client>();

	/**
	 * A list of NPCs in this region.
	 */
	private List<NPC> npcs = new LinkedList<NPC>();

	/**
	 * A list of objects in this region.
	 */
	private List<GameObject> objects = new LinkedList<GameObject>();

	/**
	 * A list of ground items in this region.
	 */
	private List<GroundItem> groundItems = new ArrayList<GroundItem>();

	/**
	 * A queue of ground items to be removed in this region.
	 */
	private Queue<GroundItem> removalGroundItems = new LinkedList<GroundItem>();

	/**
	 * A map of tiles in this region.
	 */
	private Map<Location, Tile> tiles = new HashMap<Location, Tile>();

	/**
	 * Creates a region.
	 * 
	 * @param coordinate
	 *            The coordinate.
	 */
	public Region(RegionCoordinates coordinate){
		this.coordinate = coordinate;
	}

	/**
	 * Sets a tile.
	 * 
	 * @param tile
	 *            The tile.
	 * @param location
	 *            The location.
	 */
	public void setTile(Tile tile, Location location){
		tiles.put(location, tile);
	}

	/**
	 * Gets a tile.
	 * 
	 * @param location
	 *            The location of the tile.
	 * @return The tile.
	 */
	public Tile getTile(Location location){
		if(tiles.get(location) == null){
			setTile(new Tile(), location);
		}
		return tiles.get(location);
	}

	/**
	 * @return the tiles
	 */
	public Map<Location, Tile> getTiles(){
		return tiles;
	}

	/**
	 * Gets the region coordinates.
	 * 
	 * @return The region coordinates.
	 */
	public RegionCoordinates getCoordinates(){
		return coordinate;
	}

	/**
	 * Gets the list of players.
	 * 
	 * @return The list of players.
	 */
	public Collection<Client> getPlayers(){
		synchronized(this){
			return Collections.unmodifiableCollection(new LinkedList<Client>(players));
		}
	}

	/**
	 * Gets the list of NPCs.
	 * 
	 * @return The list of NPCs.
	 */
	public Collection<NPC> getNpcs(){
		synchronized(this){
			return Collections.unmodifiableCollection(new LinkedList<NPC>(npcs));
		}
	}

	/**
	 * Gets the list of objects.
	 * 
	 * @return The list of objects.
	 */
	public Collection<GameObject> getGameObjects(){
		return objects;
	}

	/**
	 * @return groundItems
	 */
	public List<GroundItem> getGroundItems(){
		return groundItems;
	}

	/**
	 * @return removalGroundItems
	 */
	public Queue<GroundItem> getRemovalGroundItems(){
		return removalGroundItems;
	}

	/**
	 * Removes queued ground items from the region.
	 */
	public void removalQueuedGroundItems(Client player){
		while(!removalGroundItems.isEmpty()){
			GroundItem gi = removalGroundItems.poll();
			Server.itemHandler.removeGroundItem(player, gi.itemId, gi.itemX, gi.itemY, gi.itemH, true);
		}
	}

	/**
	 * Adds a new player.
	 * 
	 * @param player
	 *            The player to add.
	 */
	public void addPlayer(Client player){
		synchronized(this){
			players.add(player);
		}
	}

	/**
	 * Removes an old player.
	 * 
	 * @param player
	 *            The player to remove.
	 */
	public void removePlayer(Client player){
		synchronized(this){
			players.remove(player);
		}
	}

	/**
	 * Adds a new NPC.
	 * 
	 * @param npc
	 *            The NPC to add.
	 */
	public void addNpc(NPC npc){
		synchronized(this){
			npcs.add(npc);
		}
	}

	/**
	 * Removes an old NPC.
	 * 
	 * @param npc
	 *            The NPC to remove.
	 */
	public void removeNpc(NPC npc){
		synchronized(this){
			npcs.remove(npc);
		}
	}

	/**
	 * Gets the regions surrounding a location.
	 * 
	 * @return The regions surrounding the location.
	 */
	public Region[] getSurroundingRegions(){
		Region surrounding[] = new Region[9];
		surrounding[0] = this;
		surrounding[1] = RegionManager.getRegion(this.getCoordinates().getX() - 1, this.getCoordinates().getY() - 1);
		surrounding[2] = RegionManager.getRegion(this.getCoordinates().getX() + 1, this.getCoordinates().getY() + 1);
		surrounding[3] = RegionManager.getRegion(this.getCoordinates().getX() - 1, this.getCoordinates().getY());
		surrounding[4] = RegionManager.getRegion(this.getCoordinates().getX(), this.getCoordinates().getY() - 1);
		surrounding[5] = RegionManager.getRegion(this.getCoordinates().getX() + 1, this.getCoordinates().getY());
		surrounding[6] = RegionManager.getRegion(this.getCoordinates().getX(), this.getCoordinates().getY() + 1);
		surrounding[7] = RegionManager.getRegion(this.getCoordinates().getX() - 1, this.getCoordinates().getY() + 1);
		surrounding[8] = RegionManager.getRegion(this.getCoordinates().getX() + 1, this.getCoordinates().getY() - 1);
		return surrounding;
	}
}