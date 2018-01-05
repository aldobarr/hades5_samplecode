package server.model.players.packets;

import server.Config;
import server.Server;
import server.model.items.GroundItem;
import server.model.minigames.CastleWars;
import server.model.players.Client;
import server.model.players.PacketType;

/**
 * Magic on floor items
 **/
public class MagicOnFloorItems implements PacketType{

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		int itemY = c.getInStream().readSignedWordBigEndian();
		int itemId = c.getInStream().readUnsignedWord();
		int itemX = c.getInStream().readSignedWordBigEndian();
		int spellId = c.getInStream().readUnsignedWordA();
		int spellId2 = spellId;
		if(spellId != spellId2)
			return;
		if(c.duel != null && c.duel.status > 0)
			return;
		if(itemId >= 6865 && itemId <= 6882)
			return;
		if(!Server.itemHandler.itemExists(itemId, itemX, itemY, c.heightLevel)){
			c.stopMovement();
			return;
		}
		boolean pickup = true;
		if(itemId == Config.ZOMBIE_HEAD)
			pickup = !c.inventory.hasItem(Config.ZOMBIE_HEAD) && c.bank.getBankAmount(Config.ZOMBIE_HEAD) == 0;
		if(!pickup){
			c.sendMessage("You already have one of these.");
			return;
		}
		GroundItem item = Server.itemHandler.getGrounItem(itemId, itemX, itemY, c.heightLevel);
		if(item != null)
			if(item.cWarsItem && !CastleWars.isInCw(c))
				return;
		c.usingMagic = true;
		if(!c.getCombat().checkMagicReqs(51)){
			c.stopMovement();
			return;
		}

		if(c.goodDistance(c.getX(), c.getY(), itemX, itemY, 12)){
			int offY = (c.getX() - itemX) * -1;
			int offX = (c.getY() - itemY) * -1;
			c.teleGrabX = itemX;
			c.teleGrabY = itemY;
			c.teleGrabItem = itemId;
			c.turnPlayerTo(itemX, itemY);
			c.teleGrabDelay = System.currentTimeMillis();
			c.startAnimation(c.MAGIC_SPELLS[51][2]);
			c.gfx100(c.MAGIC_SPELLS[51][3]);
			c.getPA().createPlayersStillGfx(144, itemX, itemY, 0, 72);
			c.getPA().createPlayersProjectile(c.getX(), c.getY(), offX, offY, 50, 70, c.MAGIC_SPELLS[51][4], 50, 10, 0, 50);
			c.getPA().addSkillXP(c.MAGIC_SPELLS[51][7], 6);
			c.getPA().refreshSkill(6);
			c.stopMovement();
		}
	}

}
