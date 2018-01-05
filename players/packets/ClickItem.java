package server.model.players.packets;

import server.model.minigames.CastleWars;
import server.model.players.Client;
import server.model.players.PacketType;
import server.model.players.PlayerHandler;

/**
 * Clicking an item, bury bone, eat food etc
 **/
public class ClickItem implements PacketType{

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		int junk = c.getInStream().readSignedWordBigEndianA();
		int junk2 = junk;
		if(junk != junk2)
			return;
		int itemSlot = c.getInStream().readUnsignedWordA();
		int itemId = c.getInStream().readUnsignedWordBigEndian();
		if(c.inventory.items[itemSlot] == null || itemId != c.inventory.items[itemSlot].id - 1){
			return;
		}
		if(c.playerRights == 3)
			System.out.println("Click Item ID: " + itemId);
		if(itemId >= 5509 && itemId <= 5514){
			int pouch = -1;
			int a = itemId;
			if(a == 5509)
				pouch = 0;
			if(a == 5510)
				pouch = 1;
			if(a == 5512)
				pouch = 2;
			if(a == 5514)
				pouch = 3;
			c.getPA().fillPouch(pouch);
			return;
		}
		if(itemId == 6865){ // Blue Marionette
			c.startAnimation(3003);
			c.gfx0(511);
		}
		if(itemId == 6866){ // Green Marionette
			c.startAnimation(3003);
			c.gfx0(515);
		}
		if(itemId == 6867){ // Red Marionette
			c.startAnimation(3003);
			c.gfx0(507);
		}
		if(itemId == 14057){
			c.startAnimation(10532);
			c.gfx0(1866);
		}
		if(itemId == 20667) // Vecna skull
			c.getPA().handleVecnaSkull();
		if(itemId == 13663)
			c.activateNexTicket();
		if(itemId == 6)
			c.getCannon().setBase();
		if(itemId == 15595){
			if(c.inventory.hasItem(15591) && c.inventory.hasItem(15592) && c.inventory.hasItem(15593) &&
					c.inventory.hasItem(15594) && c.inventory.hasItem(15595)){
				for(int i = 15591; i<=15595; i++)
					c.inventory.deleteItem(i, 1);
				c.inventory.addItem(22346, 1);
				c.gfx0(751);
				c.startAnimation(404);
				c.getPA().brightLight();
				c.getDH().sendStatement("As you join the mysterious pieces, you're blinded by a flash of light!");
			}else
				c.sendMessage("You don't have all of the pieces to perform this action.");
		}
		if(itemId == 10943)
			c.sendMessage("These are the tickets I got for brutally assassinating my friends.");
		if(itemId == 4053){
			CastleWars.setCade(c);
			return;
		}
		if(itemId == 4049 && CastleWars.isInCw(c)){
			if(System.currentTimeMillis() - c.foodDelay >= 1500 && c.playerLevel[3] > 0){
				c.foodDelay = System.currentTimeMillis();
				c.getCombat().resetPlayerAttack();
				c.attackTimer += 2;
				c.inventory.deleteItem(itemId, 1);
				int toHeal = Math.round(c.getLevelForXP(c.playerXP[3]) / 10);
				if(c.playerLevel[3] < c.getLevelForXP(c.playerXP[3])){
					c.playerLevel[3] += toHeal;
					if(c.playerLevel[3] > c.getLevelForXP(c.playerXP[3]) + c.zarosModifier)
						c.playerLevel[3] = c.getLevelForXP(c.playerXP[3]) + c.zarosModifier;
				}
				c.getPA().refreshSkill(3);
			}
		}
		if(itemId == 13649 && System.currentTimeMillis() - c.locateDelay >= 1500){
			c.locateDelay = System.currentTimeMillis();
			if(c.inHowlOfDeath){
				// Target x > player x then east
				// Target y > player y then north
				int targetId = c.howlOfDeath.getTargetId(c.playerId);
				int x = PlayerHandler.players[targetId].absX;
				int y = PlayerHandler.players[targetId].absY;
				boolean yBool = Math.abs(y - c.absY) >= 10;
				if(Math.abs(x - c.absX) >= 10){
					if(yBool && x > c.absX && y > c.absY)
						c.sendMessage("You feel your talisman pull to the North-East.");
					else if(yBool && x < c.absX && y > c.absY)
						c.sendMessage("You feel your talisman pull to the North-West.");
					else if(yBool && x > c.absX && y < c.absY)
						c.sendMessage("You feel your talisman pull to the South-East.");
					else if(yBool && x < c.absX && y < c.absY)
						c.sendMessage("You feel your talisman pull to the South-West.");
					else if(!yBool && x > c.absX)
						c.sendMessage("You feel your talisman pull to the East.");
					else
						c.sendMessage("You feel your talisman pull to the West.");
				}else{
					if(yBool && y > c.absY)
						c.sendMessage("You feel your talisman pull to the North.");
					else if(yBool && y < c.absY)
						c.sendMessage("You feel your talisman pull to the South.");
					else
						c.sendMessage("You feel your target is nearby.");
				}
			}else
				c.sendMessage("You don't really feel anything.");
		}
		if(itemId == 4049 && !CastleWars.isInCw(c)){
			CastleWars.deleteGameItems(c, true);
		}
		if(c.getHerblore().isUnidHerb(itemId))
			c.getHerblore().handleHerbClick(itemId);
		if(c.getFood().isFood(itemId))
			c.getFood().eat(itemId, itemSlot);
		// ScriptManager.callFunc("itemClick_"+itemId, c, itemId, itemSlot);
		if(c.getPotions().isPotion(itemId))
			c.getPotions().handlePotion(itemId, itemSlot);
		if(c.getPrayer().isBone(itemId))
			c.getPrayer().buryBone(itemId, itemSlot);
		if(itemId == 952){
			if(c.inArea(3553, 3301, 3561, 3294)){
				c.teleTimer = 3;
				c.newLocation = 1;
			}else if(c.inArea(3550, 3287, 3557, 3278)){
				c.teleTimer = 3;
				c.newLocation = 2;
			}else if(c.inArea(3561, 3292, 3568, 3285)){
				c.teleTimer = 3;
				c.newLocation = 3;
			}else if(c.inArea(3570, 3302, 3579, 3293)){
				c.teleTimer = 3;
				c.newLocation = 4;
			}else if(c.inArea(3571, 3285, 3582, 3278)){
				c.teleTimer = 3;
				c.newLocation = 5;
			}else if(c.inArea(3562, 3279, 3569, 3273)){
				c.teleTimer = 3;
				c.newLocation = 6;
			}
		}
		if(itemId == 6722){
			c.startAnimation(2840);
			c.forcedChat("Alas!");
			c.resetWalkingQueue();
			c.stopMovement();
		}
	}
}
