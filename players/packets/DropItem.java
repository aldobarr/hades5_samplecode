package server.model.players.packets;

import server.Config;
import server.Server;
import server.model.HadesThread;
import server.model.items.GroundItem;
import server.model.minigames.CastleWars;
//import server.model.HadesThread;
import server.model.players.Client;
import server.model.players.PacketType;
import server.model.players.PlayerHandler;
import server.model.players.PlayerSave;

/**
 * Drop Item
 **/
public class DropItem implements PacketType{

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		if(c.inTrade){
			c.sendMessage("You can not drop items while in a trade");
			return;
		}
		if(c.arenas()){
			c.sendMessage("You can't drop items inside the arena!");
			return;
		}
		if(c.isBanking){
			c.sendMessage("You can not drop items while you are banking.");
			return;
		}
		if(c.isDead2){
			c.sendMessage("You can't drop items while you are being under attack!");
			return;
		}
		if(c.underAttackBy > 0 && PlayerHandler.players[c.underAttackBy] != null){
			if(PlayerHandler.players[c.underAttackBy].playerIndex == c.playerId){
				c.sendMessage("You can't drop items while you are being under attack!");
				return;
			}
		}
		if(c.usingMagic || (c.duel != null && c.duel.status > 0))
			return;
		if(c.playerRights == 2 && !Config.ADMIN_DROP_ITEMS){
			c.sendMessage("Admins can't drop items!");
			return;
		}
		int itemId = c.getInStream().readUnsignedWordA();
		c.getInStream().readUnsignedByte();
		c.getInStream().readUnsignedByte();
		int slot = c.getInStream().readUnsignedWordA();
		if(!c.inventory.hasItem(itemId))
			return;
		boolean droppable = true;
		for(int i : Config.UNDROPPABLE_ITEMS){
			if(i == itemId || c.isSkillCapeItem(itemId)){
				droppable = false;
				break;
			}
		}
		if(c.inventory.items[slot] != null && itemId != -1 && c.inventory.items[slot].id == itemId + 1){
			if(droppable){
				if(c.underAttackBy > 0){
					if(c.getShops().getItemShopValue(itemId) > 1000){
						c.sendMessage("You may not drop items worth more than 1000 while in combat.");
						return;
					}
				}
				int amount = c.inventory.items[slot].amount;
				if(Config.isBannableItem(itemId) && amount > 5){
					// new HadesThread(HadesThread.AUTO_BAN, c,
					// "Dropping rares.", 7, true);
					// return;
				}
				c.inventory.deleteItem(itemId, slot, c.inventory.items[slot].amount);
				itemId = c.getDegrade().getDroppedId(itemId);
				PlayerSave.saveGame(c);
				if(itemId == 20097){
					new HadesThread(HadesThread.REPORT, c, "Ginrei Dropped - " + amount, 9, true);
					return;
				}
				if(itemId == 4045){
					c.playerLevel[3] -= 15;
					if(c.playerLevel[3] < 0)
						c.playerLevel[3] = 0;
					c.handleHitMask(15);
					c.getPA().refreshSkill(3);
				}else{
					GroundItem item = Server.itemHandler.createGroundItem(c, itemId, c.absX, c.absY, c.heightLevel, amount, c.playerId);
					if(CastleWars.isInCw(c))
						item.cWarsItem = true;
				}
			}else{
				c.sendMessage("This item cannot be dropped.");
			}
		}
	}
}
