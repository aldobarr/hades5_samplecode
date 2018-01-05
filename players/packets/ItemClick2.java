package server.model.players.packets;

import server.model.players.Client;
import server.model.players.PacketType;

public class ItemClick2 implements PacketType{

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		int itemId = c.getInStream().readSignedWordA();

		if(!c.inventory.hasItem(itemId, 1))
			return;

		switch(itemId){
			case 24455:
			case 24456:
			case 24457:
				c.handleCrucibleEmote();
				break;
			case 6865: // Blue Marionette
				c.startAnimation(3005);
				c.gfx0(513);
				break;
			case 6866: // Green Marionette
				c.startAnimation(3005);
				c.gfx0(517);
				break;
			case 6867: // Red Marionette
				c.startAnimation(3005);
				c.gfx0(509);
				break;
			case 11283:
			case 11284:
				c.sendMessage("Your Dragonfire Shield has " + c.dfsCharges + (c.dfsCharges == 1 ? " charge" : " charges") + " left");
				break;
			case 6722:
				c.teleAction = -1;
				c.dialogueAction = 15;
				c.getDH().sendDialogues(49, -1);
				break;
			default:
				if(c.playerRights == 3)
					System.out.println(c.playerName + " - Item3rdOption: " + itemId);
				break;
		}
	}
}