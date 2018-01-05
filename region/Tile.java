package server.model.region;

import java.util.ArrayList;
import java.util.List;
import server.model.items.GroundItem;

/**
 * Holds selective data for a specific tile.
 * 
 * @author Thomas Nappo
 */
public class Tile{

	/**
	 * A list of ground items on this tile.
	 */
	private List<GroundItem> groundItems = new ArrayList<GroundItem>();

	/**
	 * Gets the ground items on this tile.
	 * 
	 * @return the groundItems
	 */
	public List<GroundItem> getGroundItems(){
		return groundItems;
	}
}
