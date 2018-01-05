package server.model.region;

import server.model.players.Player;
import server.world.GameObject;

/**
 * Represents a single location in the game world.
 * 
 * @author Graham Edgecombe
 * @author Thomas Nappo
 */
public class Location{

	/**
	 * The x coordinate.
	 */
	private final int x;

	/**
	 * The y coordinate.
	 */
	private final int y;

	/**
	 * The z coordinate.
	 */
	private final int z;

	/**
	 * Creates a location.
	 * 
	 * @param x
	 *            The x coordinate.
	 * @param y
	 *            The y coordinate.
	 * @param z
	 *            The z coordinate.
	 * @return The location.
	 */
	public static Location create(int x, int y, int z){
		return new Location(x, y, z);
	}

	/**
	 * Creates a location.
	 * 
	 * @param x
	 *            The x coordinate.
	 * @param y
	 *            The y coordinate.
	 * @return loc(x, y, 0)
	 */
	public static Location create(int x, int y){
		return new Location(x, y, 0);
	}

	/**
	 * Creates a location.
	 * 
	 * @param x
	 *            The x coordinate.
	 * @param y
	 *            The y coordinate.
	 * @param z
	 *            The z coordinate.
	 */
	private Location(int x, int y, int z){
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Gets the absolute x coordinate.
	 * 
	 * @return The absolute x coordinate.
	 */
	public int getX(){
		return x;
	}

	/**
	 * Gets the absolute y coordinate.
	 * 
	 * @return The absolute y coordinate.
	 */
	public int getY(){
		return y;
	}

	/**
	 * Gets the z coordinate, or height.
	 * 
	 * @return The z coordinate.
	 */
	public int getZ(){
		return z;
	}

	/**
	 * Gets the local x coordinate relative to this region.
	 * 
	 * @return The local x coordinate relative to this region.
	 */
	public int getLocalX(){
		return getLocalX(this);
	}

	/**
	 * Gets the local y coordinate relative to this region.
	 * 
	 * @return The local y coordinate relative to this region.
	 */
	public int getLocalY(){
		return getLocalY(this);
	}

	/**
	 * Gets the local x coordinate relative to a specific region.
	 * 
	 * @param l
	 *            The region the coordinate will be relative to.
	 * @return The local x coordinate.
	 */
	public int getLocalX(Location l){
		return x - 8 * l.getRegionX();
	}

	/**
	 * Gets the local y coordinate relative to a specific region.
	 * 
	 * @param l
	 *            The region the coordinate will be relative to.
	 * @return The local y coordinate.
	 */
	public int getLocalY(Location l){
		return y - 8 * l.getRegionY();
	}

	/**
	 * Gets the region x coordinate.
	 * 
	 * @return The region x coordinate.
	 */
	public int getRegionX(){
		return (x >> 3) - 6;
	}

	/**
	 * Gets the region y coordinate.
	 * 
	 * @return The region y coordinate.
	 */
	public int getRegionY(){
		return (y >> 3) - 6;
	}

	/**
	 * Checks if a coordinate is within range of another.
	 * 
	 * @return <code>true</code> if the location is in range, <code>false</code>
	 *         if not.
	 */
	public boolean isWithinDistance(Player attacker, Player victim, int distance){
		return distanceToPoint(victim.getLocation()) <= distance;
	}

	/**
	 * Gets the distance to a location.
	 * 
	 * @param other
	 *            The location.
	 * @return The distance from the other location.
	 */
	public int distanceToPoint(Location other){
		int absX = x;
		int absY = y;
		int pointX = other.getX();
		int pointY = other.getY();
		return (int)Math.sqrt(Math.pow(absX - pointX, 2) + Math.pow(absY - pointY, 2));
	}

	/**
	 * Checks if this location is within range of another.
	 * 
	 * @param other
	 *            The other location.
	 * @return <code>true</code> if the location is in range, <code>false</code>
	 *         if not.
	 */
	public boolean isWithinDistance(Location other){
		if(z != other.z){
			return false;
		}
		int deltaX = other.x - x, deltaY = other.y - y;
		return deltaX <= 14 && deltaX >= -15 && deltaY <= 14 && deltaY >= -15;
	}

	/**
	 * Checks if this location is within interaction range of another.
	 * 
	 * @param other
	 *            The other location.
	 * @return <code>true</code> if the location is in range, <code>false</code>
	 *         if not.
	 */
	public boolean isWithinInteractionDistance(Location other){
		if(z != other.z){
			return false;
		}
		int deltaX = other.x - x, deltaY = other.y - y;
		return deltaX <= 2 && deltaX >= -3 && deltaY <= 2 && deltaY >= -3;
	}

	@Override
	public int hashCode(){
		return z << 30 | x << 15 | y;
	}

	@Override
	public boolean equals(Object other){
		if(!(other instanceof Location)){
			return false;
		}
		Location loc = (Location)other;
		return loc.x == x && loc.y == y && loc.z == z;
	}

	@Override
	public String toString(){
		return "[" + x + "," + y + "," + z + "]";
	}

	/**
	 * Creates a new location based on this location.
	 * 
	 * @param diffX
	 *            X difference.
	 * @param diffY
	 *            Y difference.
	 * @param diffZ
	 *            Z difference.
	 * @return The new location.
	 */
	public Location transform(int diffX, int diffY){
		return Location.create(x + diffX, y + diffX, z);
	}

	public Location transform(int diffX, int diffY, int diffZ){
		return Location.create(x + diffX, y + diffY, z + diffZ);
	}

	/**
	 * Gets the closest spot from a list of locations.
	 * 
	 * @param steps
	 *            The list of steps.
	 * @param location
	 *            The location we want to be close to.
	 * @return The closest location.
	 */
	public static Location[] getClosestSpot(Location target, Location[][] steps){
		Location closestStep[] = new Location[steps.length + 1];
		int index = 0;
		for(int i2 = 0; i2 < steps.length; i2++){
			for(int i = 0; i < steps[i2].length; i++){
				if(closestStep[index] == null || (getDistance(closestStep[index], target) > getDistance(steps[i2][i], target))){
					// if (RS2RegionLoader.positionIsWalkalble(e, p.getX(),
					// p.getY())) {
					// System.out.println("Setting walkable pos..");
					closestStep[index] = steps[i2][i];
				}
			}
			index++;
		}
		return closestStep;
	}

	public static double getDistance(Location p, Location p2){
		return Math.sqrt((p2.getX() - p.getX()) * (p2.getX() - p.getX()) + (p2.getY() - p.getY()) * (p2.getY() - p.getY()));
	}

	/**
	 * Checks if we're within combat range.
	 * 
	 * @param dist
	 *            The distance.
	 * @return <code>true</code> if, <code>false</code> if not.
	 */
	public boolean isWithinInteractingRange(Player e, Player target, int dist){
		int eSize = 1;
		int tSize = 1;
		Location loc = getClosestSpot(e.getLocation(), getValidSpots(tSize, target.getLocation(), e));
		if(loc == null){
			return false;
		}else{
			return withinRange(loc, dist - (eSize / 2));
		}
	}

	/**
	 * Gets a list of all the valid spots around another location, within a
	 * specific "size/range".
	 * 
	 * @param size
	 *            The size/range.
	 * @param location
	 *            The location we want to get locations within range from.
	 */
	public Location[] getValidSpots(int size, Location location, Player e){
		Location list[] = new Location[size * 4];
		int index = 0;
		for(int i = 0; i < size; i++){
			list[index++] = !clipped(e, new Location(location.getX() - 1, location.getY() + i, location.getZ())) ? (new Location(location.getX() - 1, location.getY() + i, location.getZ())) : null;
			list[index++] = !clipped(e, new Location(location.getX() + i, location.getY() - 1, location.getZ())) ? new Location(location.getX() + i, location.getY() - 1, location.getZ()) : null;
			list[index++] = !clipped(e, new Location(location.getX() + i, location.getY() + size, location.getZ())) ? new Location(location.getX() + i, location.getY() + size, location.getZ()) : null;
			list[index++] = !clipped(e, new Location(location.getX() + size, location.getY() + i, location.getZ())) ? new Location(location.getX() + size, location.getY() + i, location.getZ()) : null;
		}
		return list;
	}

	public boolean clipped(Player e, Location loc){
		for(GameObject obj : e.getRegion().getGameObjects())
			if(obj != null && obj.type() != 22 && obj.getLocation().equals(loc))
				return true;
		return false;
	}

	/**
	 * Gets the closest spot from a list of locations.
	 * 
	 * @param steps
	 *            The list of steps.
	 * @param location
	 *            The location we want to be close to.
	 * @return The closest location.
	 */
	public Location getClosestSpot(Location target, Location[] steps){
		Location closestStep = null;
		for(Location p : steps){
			if(p == null){
				break;
			}
			if(closestStep == null || (getDistance(closestStep, target) > getDistance(p, target))){
				// if (RS2RegionLoader.positionIsWalkalble(e, p.getX(),
				// p.getY())) {
				// System.out.println("Setting walkable pos..");
				closestStep = p;
				// }
			}
		}
		return closestStep;
	}

	/**
	 * Checks if this location is within interaction range of another.
	 * 
	 * @param other
	 *            The other location.
	 * @return <code>true</code> if the location is in range, <code>false</code>
	 *         if not.
	 */
	public int distance(Location other){
		int deltaX = other.x - x, deltaY = other.y - y;
		double dis = Math.sqrt(Math.pow(deltaX, 2D) + Math.pow(deltaY, 2D));
		if(dis > 1.0 && dis < 2)
			return 2;
		return (int)dis;
	}

	/**
	 * Checks if a specific location is within a specific radius.
	 * 
	 * @param rad
	 *            The radius.
	 * @return True if we're within distance/range, false if not.
	 */
	public boolean withinRange(Location p, int rad){
		if(p == null){
			return false;
		}
		int dX = Math.abs(x - p.x);
		int dY = Math.abs(y - p.y);
		return dX <= rad && dY <= rad;
	}

	/**
	 * Checks if a specific location is within a specific radius.
	 * 
	 * @param rad
	 *            The radius.
	 * @return True if we're within distance/range, false if not.
	 */
	public boolean withinRange(int x1, int y1, int rad){
		int dX = Math.abs(x - x1);
		int dY = Math.abs(y - y1);
		return dX <= rad && dY <= rad;
	}

}