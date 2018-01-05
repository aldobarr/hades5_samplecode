package server.model.players.packets;

import server.Config;
import server.model.players.Client;
import server.model.players.PacketType;

/**
 * 
 * @author Aldo Barreras
 * 
 */
public class ChangeAppearance implements PacketType{
	@Override
	public void processPacket(final Client client, final int packetType, final int packetSize){
		final int gender = client.getInStream().readSignedByte();
		if(gender != 0 && gender != 1)
			return;
		final int GENDER[][] = gender == 0 ? Config.MALE_VALUES : Config.FEMALE_VALUES;
		final int appearances[] = new int[Config.MALE_VALUES.length];
		/*
		 * Appearance value check.
		 */
		for(int i = 0; i < appearances.length; i++){
			int value = client.getInStream().readSignedByte();
			appearances[i] = value < GENDER[i][0] || value > GENDER[i][1] ? GENDER[i][0] : value;
		}
		final int colors[] = new int[Config.ALLOWED_COLORS.length];
		/*
		 * Color value check.
		 */
		for(int i = 0; i < colors.length; i++){
			int value = client.getInStream().readSignedByte();
			colors[i] = value < Config.ALLOWED_COLORS[i][0] || value > Config.ALLOWED_COLORS[i][1] ? Config.ALLOWED_COLORS[i][0] : value;
		}

		if(client.canChangeAppearance){
			client.playerAppearance[0] = gender; // gender
			client.playerAppearance[1] = appearances[0]; // head
			client.playerAppearance[2] = appearances[2]; // torso
			client.playerAppearance[3] = appearances[3]; // arms
			client.playerAppearance[4] = appearances[4]; // hands
			client.playerAppearance[5] = appearances[5]; // legs
			client.playerAppearance[6] = appearances[6]; // feet
			client.playerAppearance[7] = appearances[1]; // beard
			client.playerAppearance[8] = colors[0]; // hair colour
			client.playerAppearance[9] = colors[1]; // torso colour
			client.playerAppearance[10] = colors[2]; // legs colour
			client.playerAppearance[11] = colors[3]; // feet colour
			client.playerAppearance[12] = colors[4]; // skin colour

			client.getPA().removeAllWindows();
			client.getPA().requestUpdates();
			client.canChangeAppearance = false;
		}
	}
}