package server.model.players.packets;

import server.Config;
import server.model.items.Item;
import server.model.minigames.Duel;
import server.model.players.Client;
import server.model.players.PacketType;
import server.util.Misc;

/**
 * Wear Item
 **/
public class WearItem implements PacketType{

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		c.wearId = c.getInStream().readUnsignedWord();
		c.wearSlot = c.getInStream().readUnsignedWordA();
		c.interfaceId = c.getInStream().readUnsignedWordA();
		int slot = Item.targetSlots[c.wearId];
		if((c.playerIndex > 0 || c.npcIndex > 0) && c.wearId != 4153)
			c.getCombat().resetPlayerAttack();
		if(c.wearId == 6722){
			c.startAnimation(2844);
			c.forcedChat("Mwuhahahaha!");
			c.resetWalkingQueue();
			c.stopMovement();
			return;
		}
		if((c.wearId == 14076 || c.wearId == 14077) && c.playerAppearance[0] == 1){
			c.inventory.replaceItem(c.wearId, c.wearId + 2);
			c.wearId += 2;
		}
		if(c.wearId == 14081 && c.playerAppearance[0] == 1)
			c.inventory.replaceItem(c.wearId, --c.wearId);
		if((c.wearId == 14078 || c.wearId == 14079) && c.playerAppearance[0] == 0){
			c.inventory.replaceItem(c.wearId, c.wearId - 2);
			c.wearId -= 2;
		}
		if(c.wearId == 14080 && c.playerAppearance[0] == 0)
			c.inventory.replaceItem(c.wearId, ++c.wearId);
		if(c.wearId >= 5509 && c.wearId <= 5515){
			int pouch = -1;
			int a = c.wearId;
			if(a == 5509)
				pouch = 0;
			if(a == 5510)
				pouch = 1;
			if(a == 5512)
				pouch = 2;
			if(a == 5514)
				pouch = 3;
			c.getPA().emptyPouch(pouch);
			return;
		}
		if(c.wearId == 6865){ // Blue Marionette
			c.startAnimation(3004);
			c.gfx0(512);
			return;
		}
		if(c.wearId == 6866){ // Green Marionette
			c.startAnimation(3004);
			c.gfx0(516);
			return;
		}
		if(c.wearId == 6867){ // Red Marionette
			c.startAnimation(3004);
			c.gfx0(508);
			return;
		}
		if(c.wearId == Config.EASTER_RING){
			if(c.isInCastleWars() || c.inCwWait || c.inCwGame || (c.duel != null && c.duel.status >= 3))
				return;
			if(c.duel != null && c.duel.status < 3)
				Duel.declineDuel(c, c.duel.getOtherPlayer(c.playerId) != null);
			int eggId = c.easterEggs[Misc.random(c.easterEggs.length - 1)];
			c.isWearingRing = true;
			c.npcId = eggId;
			c.isNpc = true;
			c.updateRequired = true;
			c.appearanceUpdateRequired = true;
		}else if(slot == c.playerRing){
			c.isWearingRing = false;
			c.npcId = -1;
			c.isNpc = false;
			c.updateRequired = true;
			c.appearanceUpdateRequired = true;
		}else if(slot == c.playerWeapon || c.wearSlot == c.playerWeapon)
			c.getPA().resetAutocast();
		// c.attackTimer = oldCombatTimer;
		c.getItems().wearItem(c.wearId, c.wearSlot);
	}
}