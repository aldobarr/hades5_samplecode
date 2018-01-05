package server.model.players.packets;

import server.Config;
import server.model.minigames.Duel;
import server.model.players.Client;
import server.model.players.PacketType;
import server.model.players.PlayerHandler;
import server.util.Misc;

/**
 * Walking packet
 **/
public class Walking implements PacketType{

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		if(c.isBanking)
			c.isBanking = false;
		if(c.isShopping)
			c.isShopping = false;
		if(c.isJailed || c.capeMovement || c.ignoreClip || c.inCutScene)
			return;
		if(c.isWearingRing && !c.playerName.equalsIgnoreCase(Config.OWNER)){
			c.playerIndex = 0;
			return;
		}
		c.closeClanWars();
		if(c.inTrade)
			c.getTradeAndDuel().declineTrade(true);
		if(c.duel != null && c.duel.status > 0 && c.duel.status < 3)
			Duel.declineDuel(c, true);
		if(packetType == 248 || packetType == 164){
			c.faceUpdate(0);
			c.quincyStage = 0;
			c.npcIndex = 0;
			c.playerIndex = 0;
			if(c.followId > 0 || c.followId2 > 0)
				c.getPA().resetFollow();
		}
		c.getPA().removeAllWindows();
		if(c.duel != null && c.duel.rules != null && c.duel.rules.duelRule[1] && c.duel.status == 3){
			if(c.duel.getOtherPlayer(c.playerId) != null){
				if(!c.goodDistance(c.getX(), c.getY(), c.duel.getOtherPlayer(c.playerId).getX(), c.duel.getOtherPlayer(c.playerId).getY(), 1)){
					c.sendMessage("Movement has been disabled for this duel!");
				}
			}
			c.playerIndex = 0;
			return;
		}
		if(c.duel != null && c.duel.status == 4){
			if(c.duel.winner == c.playerId)
				c.duel.claimStakedItems(c);
			c.duel.resetDuel(c);
		}
		if(c.arenas() && (c.duel != null && c.duel.status != 3) && (c.playerRights == 0 || c.playerRights == 5))
			return;
		if(c.freezeTimer > 0){
			if(PlayerHandler.players[c.playerIndex] != null){
				if(c.goodDistance(c.getX(), c.getY(), PlayerHandler.players[c.playerIndex].getX(), PlayerHandler.players[c.playerIndex].getY(), 1) && packetType != 98){
					c.playerIndex = 0;
					return;
				}
			}
			if(packetType != 98){
				c.sendMessage("A magical force stops you from moving.");
				c.playerIndex = 0;
			}
			return;
		}

		if(System.currentTimeMillis() - c.lastSpear < 4000){
			c.sendMessage("You have been stunned.");
			c.playerIndex = 0;
			return;
		}

		if(packetType == 98){
			c.mageAllowed = true;
		}

		if(c.respawnTimer > 3){
			return;
		}
		if(c.inTrade){
			return;
		}
		c.getPA().removeAllWindows();
		c.getPA().closeInput();
		if(packetType == 248){
			packetSize -= 14;
		}
		c.newWalkCmdSteps = (packetSize - 5) / 2;
		if(++c.newWalkCmdSteps > c.walkingQueueSize){
			c.newWalkCmdSteps = 0;
			return;
		}
		c.killedBy = 0;
		c.getNewWalkCmdX()[0] = c.getNewWalkCmdY()[0] = 0;

		int firstStepX = c.getInStream().readSignedWordBigEndianA() - c.getMapRegionX() * 8;
		for(int i = 1; i < c.newWalkCmdSteps; i++){
			c.getNewWalkCmdX()[i] = c.getInStream().readSignedByte();
			c.getNewWalkCmdY()[i] = c.getInStream().readSignedByte();
		}

		int firstStepY = c.getInStream().readSignedWordBigEndian() - c.getMapRegionY() * 8;
		c.setNewWalkCmdIsRunning(c.getInStream().readSignedByteC() == 1);
		for(int i = 0; i < c.newWalkCmdSteps; i++){
			c.getNewWalkCmdX()[i] += firstStepX;
			c.getNewWalkCmdY()[i] += firstStepY;
			c.playerDirection = Misc.direction2(c.absX, c.absY, c.getNewWalkCmdX()[i] + firstStepX + c.getMapRegionX()*8, c.getNewWalkCmdY()[i] + firstStepY + c.getMapRegionY()*8);
		}
	}
}