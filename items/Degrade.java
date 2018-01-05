package server.model.items;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import server.Config;
import server.Server;
import server.model.players.Client;
import server.util.Misc;

public class Degrade{
	private Client player;
	private static ArrayList<DegradeItem> degradableItems = new ArrayList<DegradeItem>();
	public Degrade(Client player){
		this.player = player;
	}
	
	/**
	 * Checks all the degrade items to see if the provided id is an item that is indeed degradable.
	 * @param itemId The item being checked.
	 * @return True if and only if the id matched a degrade item.
	 */
	public static boolean isDegradeItem(int itemId){
		for(int i = 0; i<degradableItems.size(); i++)
			if(degradableItems.get(i).isDegradeItem(itemId))
				return true;
		return false;
	}
	
	/**
	 * Checks all the degrade items to see if the provided id is an item that is degraded.
	 * @param itemId The item being checked.
	 * @return True if and only if the id matched a degraded item.
	 */
	public static boolean isDegradedItem(int itemId){
		for(int i = 0; i<degradableItems.size(); i++)
			if(degradableItems.get(i).isDegradedItem(itemId))
				return true;
		return false;
	}
	
	/**
	 * Finds the degrade item object for the item.
	 * @param itemId The item being searched.
	 * @return The degrade item object for the item if it is found. Null otherwise.
	 */
	public static DegradeItem getDegradeItem(int itemId){
		for(int i = 0; i<degradableItems.size(); i++)
			if(degradableItems.get(i).isDegradeItem(itemId))
				return degradableItems.get(i);
		return null;
	}
	
	/**
	 * Checks whether the degrade item is still fully charged and unused.
	 * @param degradeItem The degrade item object.
	 * @param itemId The id of the item.
	 * @return True if and only if the item being checked is unused.
	 */
	public static boolean itemNotDegraded(DegradeItem degradeItem, int itemId){
		if(degradeItem.getDegradeItems().length == 0 || itemId == Config.ECTOPLASMATOR)
			return false;
		return itemId == degradeItem.getItemId();
	}
	
	/**
	 * Loads all degrade item configuration data.
	 */
	public static void setup(){
		try(Scanner in = new Scanner(new File("./Data/cfg/degrade.cfg"))){
			while(in.hasNextLine()){
				String line[] = in.nextLine().split("//")[0].trim().split(", ");
				if(line.length < 2)
					continue;
				int itemId = Integer.parseInt(line[0].trim());
				int degradeTime = Integer.parseInt(line[1].trim());
				int otherItems[] = new int[line.length - 2];
				for(int i = 0; i<otherItems.length; i++)
					otherItems[i] = Integer.parseInt(line[i + 2].trim());
				degradableItems.add(new DegradeItem(itemId, Math.abs(degradeTime), otherItems, degradeTime > 0));
			}
		}catch(Exception e){
			e.printStackTrace(); // Looks like the config file is malformed.
		}
	}
	
	public int getDroppedId(int itemId){
		if(!isDegradeItem(itemId))
			return itemId;
		DegradeItem degradeItem = getDegradeItem(itemId);
		return degradeItem.getDegradeItems().length == 1 ? (isDegradedItem(itemId) ? -1 : itemId) : degradeItem.getDegradeItems()[degradeItem.getDegradeItems().length - 1];
	}
	
	/**
	 * Handles degrading items.
	 */
	public void handleDegrade(){
		for(int i = 0; i<player.playerEquipment.length; i++){
			if(!isDegradeItem(player.playerEquipment[i]))
				continue;
			DegradeItem degradeItem = getDegradeItem(player.playerEquipment[i]);
			if(itemNotDegraded(degradeItem, player.playerEquipment[i])){
				player.getItems().deleteEquipment(1, i);
				player.getItems().setEquipment(degradeItem.getDegradeItems()[0], 1, i);
				if(i == player.playerWeapon)
					player.getItems().sendWeapon(degradeItem.getDegradeItems()[0], player.getItems().getItemName(degradeItem.getDegradeItems()[0]));
				player.playerEquipmentDT[i] = Misc.currentTimeSeconds();
			}else if(player.playerEquipmentDT[i] <= 0){
				player.playerEquipmentDT[i] = Misc.currentTimeSeconds();
			}else{
				int time = Misc.currentTimeSeconds();
				player.playerEquipmentD[i] += time - player.playerEquipmentDT[i];
				player.playerEquipmentDT[i] = time;
				if(player.playerEquipmentD[i] >= degradeItem.getDegradeTime()){
					player.getItems().deleteEquipment(1, i);
					player.playerEquipmentD[i] = -1;
					player.playerEquipmentDT[i] = -1;
					if(degradeItem.getDegradeItems().length > 1 && degradeItem.getDegradeItems()[1] != 592)
						player.getItems().setEquipment(degradeItem.getDegradeItems()[1], 1, i);
					else if(degradeItem.getDegradeItems().length > 0 && degradeItem.getItemId() == Config.ECTOPLASMATOR){
						if(degradeItem.getItemId() == Config.ECTOPLASMATOR)
							player.sendMessage("Guthix drains your Ectoplasmator in exchange for the help he gave you in battle.");
						if(player.inventory.freeSlots() > 0)
							player.inventory.addItem(592, 1);
						else
							Server.itemHandler.createGroundItem(player, 592, player.absX, player.absY, player.heightLevel, 1, player.playerId);
					}
				}
			}
		}
	}
}