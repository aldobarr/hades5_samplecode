package server.model.players.packets;

import server.model.players.Client;
import server.model.players.PacketType;

public class ItemClick3 implements PacketType{

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		int itemId11 = c.getInStream().readSignedWordBigEndianA();
		int itemId1 = c.getInStream().readSignedWordA();
		int itemId = c.getInStream().readSignedWordA();

		switch(itemId){
			case 1706:
			case 1708:
			case 1710:
			case 1712:
			case 10354:
			case 10356:
			case 10358:
			case 10360:
				c.getPA().handleGlory(itemId, false);
				break;
			case 6865: // blue Marionette
				c.startAnimation(3006);
				c.gfx0(514);
				break;
			case 6866: // Green Marionette
				c.startAnimation(3006);
				c.gfx0(518);
				break;
			case 6867: // Red Marionette
				c.startAnimation(3006);
				c.gfx0(510);
				break;
			case 2552:
			case 2554:
			case 2556:
			case 2558:
			case 2560:
			case 2562:
			case 2564:
			case 2566:
				c.getPA().handleRODueling(itemId, false);
				break;
			case 14057:
				c.getPA().teleBroom();
				break;
			case 11283:
				c.dfsCharges = 0;
				if(!c.inventory.hasItem(11283))
					break;
				c.inventory.deleteItem(11283, 1);
				c.inventory.addItem(11284, 1, -1);
				c.sendMessage("Your Dragonfire Shield has been emptied.");
				break;
			default:
				if(c.playerRights == 3)
					System.out.println(c.playerName + " - Item3rdOption: " + itemId + " : " + itemId11 + " : " + itemId1);
				break;
		}
	}
}