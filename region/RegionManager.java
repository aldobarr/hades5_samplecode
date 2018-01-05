package server.model.region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import server.model.players.Client;
import server.model.region.Location;
import server.model.npcs.NPC;
import server.model.players.Player;

/**
 * Manages the world regions.
 * 
 * @author Graham Edgecombe
 * @author Thomas Nappo
 */
public class RegionManager{

	/**
	 * The region size.
	 */
	public static final int REGION_SIZE = 32;

	/**
	 * The lower bound that splits the region in half.
	 */
	@SuppressWarnings("unused")
	private static final int LOWER_BOUND = REGION_SIZE / 2 - 1;

	/**
	 * The active (loaded) region map.
	 */
	private static Map<RegionCoordinates, Region> activeRegions = new HashMap<RegionCoordinates, Region>();

	/**
	 * Gets the local players around an entity.
	 * 
	 * @param entity
	 *            The entity.
	 * @return The collection of local players.
	 */
	public static Collection<Player> getLocalPlayers(Player entity){
		List<Player> localPlayers = new LinkedList<Player>();
		Region regions[] = getSurroundingRegions(entity.getLocation());
		for(Region region : regions){
			for(Client player : region.getPlayers()){
				if(player.getLocation().isWithinDistance(entity.getLocation())){
					localPlayers.add(player);
				}
			}
		}
		return Collections.unmodifiableCollection(localPlayers);
	}

	public static Collection<Player> getLocalPlayers(Location loc){
		List<Player> localPlayers = new LinkedList<Player>();
		Region regions[] = getSurroundingRegions(loc);
		for(Region region : regions)
			for(Client player : region.getPlayers())
				if(player.getLocation().isWithinDistance(loc))
					localPlayers.add(player);
		return Collections.unmodifiableCollection(localPlayers);
	}

	/**
	 * Ensure there are no "leftover" regions.
	 */
	public static void cleanup(){
		Set<RegionCoordinates> rcc = activeRegions.keySet();
		Map<Region, ArrayList<Client>> removePlayers = new HashMap<Region, ArrayList<Client>>();
		Map<Region, ArrayList<NPC>> removeNpcs = new HashMap<Region, ArrayList<NPC>>();
		synchronized(rcc){
			for(RegionCoordinates rc : rcc){
				synchronized(rc){
					if(rc == null)
						continue;
					Region current = activeRegions.get(rc);
					if(current == null)
						continue;
					Collection<Client> players = current.getPlayers();
					Collection<NPC> npcs = current.getNpcs();
					for(Client c : players){
						if(c == null){
							if(removePlayers.containsKey(current))
								removePlayers.get(current).add(c);
							else{
								ArrayList<Client> al = new ArrayList<Client>();
								al.add(c);
								removePlayers.put(current, al);
							}
							continue;
						}
						if(c.disconnected || !c.isActive){
							if(removePlayers.containsKey(current))
								removePlayers.get(current).add(c);
							else{
								ArrayList<Client> al = new ArrayList<Client>();
								al.add(c);
								removePlayers.put(current, al);
							}
							continue;
						}
						Region temp = RegionManager.getRegionByLocation(Location.create(c.absX, c.absY, c.heightLevel));
						if(temp != current){
							if(removePlayers.containsKey(current))
								removePlayers.get(current).add(c);
							else{
								ArrayList<Client> al = new ArrayList<Client>();
								al.add(c);
								removePlayers.put(current, al);
							}
						}
					}
					for(NPC npc : npcs){
						if(npc == null){
							if(removePlayers.containsKey(current))
								removeNpcs.get(current).add(npc);
							else{
								ArrayList<NPC> al = new ArrayList<NPC>();
								al.add(npc);
								removeNpcs.put(current, al);
							}
							continue;
						}
						if(npc.isDead){
							if(removePlayers.containsKey(current))
								removeNpcs.get(current).add(npc);
							else{
								ArrayList<NPC> al = new ArrayList<NPC>();
								al.add(npc);
								removeNpcs.put(current, al);
							}
							continue;
						}
						Region temp = RegionManager.getRegionByLocation(Location.create(npc.absX, npc.absY, npc.heightLevel));
						if(temp != current){
							if(removePlayers.containsKey(current))
								removeNpcs.get(current).add(npc);
							else{
								ArrayList<NPC> al = new ArrayList<NPC>();
								al.add(npc);
								removeNpcs.put(current, al);
							}
						}
					}
				}
			}
		}
		Set<Region> p = removePlayers.keySet();
		Set<Region> n = removeNpcs.keySet();
		for(Region r : p){
			ArrayList<Client> players = removePlayers.get(r);
			synchronized(activeRegions){
				for(Client c : players)
					r.removePlayer(c);
			}
		}
		for(Region r : n){
			ArrayList<NPC> npcs = removeNpcs.get(r);
			synchronized(activeRegions){
				for(NPC npc : npcs)
					r.removeNpc(npc);
			}
		}
	}

	/**
	 * Gets the local NPCs around an entity.
	 * 
	 * @param entity
	 *            The entity.
	 * @return The collection of local NPCs.
	 */
	public static Collection<NPC> getLocalNpcs(Player entity){
		List<NPC> localNpcs = new LinkedList<NPC>();
		Region regions[] = getSurroundingRegions(entity.getLocation());
		for(Region region : regions){
			for(NPC npc : region.getNpcs()){
				if(npc.getLocation().isWithinDistance(entity.getLocation())){
					localNpcs.add(npc);
				}
			}
		}
		return Collections.unmodifiableCollection(localNpcs);
	}

	public static Collection<NPC> getLocalNpcs(Location loc){
		List<NPC> localNpcs = new LinkedList<NPC>();
		Region regions[] = getSurroundingRegions(loc);
		for(Region region : regions)
			for(NPC npc : region.getNpcs())
				if(npc.getLocation().isWithinDistance(loc))
					localNpcs.add(npc);
		return Collections.unmodifiableCollection(localNpcs);
	}

	/**
	 * Gets the regions surrounding a location.
	 * 
	 * @param location
	 *            The location.
	 * @return The regions surrounding the location.
	 */
	public static Region[] getSurroundingRegions(Location location){
		int regionX = location.getX() / REGION_SIZE;
		int regionY = location.getY() / REGION_SIZE;

		// int regionPositionX = location.getX() % REGION_SIZE;
		// int regionPositionY = location.getY() % REGION_SIZE;

		Region surrounding[] = new Region[9];
		surrounding[0] = getRegion(regionX, regionY);
		surrounding[1] = getRegion(regionX - 1, regionY - 1);
		surrounding[2] = getRegion(regionX + 1, regionY + 1);
		surrounding[3] = getRegion(regionX - 1, regionY);
		surrounding[4] = getRegion(regionX, regionY - 1);
		surrounding[5] = getRegion(regionX + 1, regionY);
		surrounding[6] = getRegion(regionX, regionY + 1);
		surrounding[7] = getRegion(regionX - 1, regionY + 1);
		surrounding[8] = getRegion(regionX + 1, regionY - 1);

		return surrounding;
	}

	/**
	 * Gets a region by location.
	 * 
	 * @param location
	 *            The location.
	 * @return The region.
	 */
	public static Region getRegionByLocation(Location location){
		return getRegion(location.getX() / REGION_SIZE, location.getY() / REGION_SIZE);
	}

	/**
	 * Gets a region by its x and y coordinates.
	 * 
	 * @param x
	 *            The x coordinate.
	 * @param y
	 *            The y coordinate.
	 * @return The region.
	 */
	public static Region getRegion(int x, int y){
		RegionCoordinates key = new RegionCoordinates(x, y);
		if(activeRegions.containsKey(key)){
			return activeRegions.get(key);
		}else{
			Region region = new Region(key);
			activeRegions.put(key, region);
			return region;
		}
	}
}