package server.model.minigames;

import java.util.Iterator;
import server.Config;
import server.clip.region.Region;
import server.model.players.Client;

public class CastleWarObjects{
	public static void handleObject(Client c, int id, int x, int y){
		if(!CastleWars.isInCw(c) && !c.playerName.equalsIgnoreCase(Config.OWNER)){
			c.sendMessage("You gotta be in castle wars before you can use these objects");
			return;
		}
		Iterator<Client> players = null;
		switch(id){
			case 4469:
				if(CastleWars.getTeamNumber(c) == 2){
					c.sendMessage("You are not allowed in the other teams spawn point.");
					break;
				}
				if(c.getItems().playerHasEquipped(CastleWars.SARA_BANNER) || c.getItems().playerHasEquipped(CastleWars.ZAMMY_BANNER)){
					c.sendMessage("You can not enter this area with the flag on.");
					break;
				}
				if(x == 2426){
					if(c.getY() == 3080){
						c.inCWBase = false;
						c.getPA().movePlayer(2426, 3081, c.heightLevel);
					}else if(c.getY() == 3081){
						c.inCWBase = true;
						c.getPA().movePlayer(2426, 3080, c.heightLevel);
					}
				}else if(x == 2422){
					if(c.getX() == 2422){
						c.inCWBase = true;
						c.getPA().movePlayer(2423, 3076, c.heightLevel);
					}else if(c.getX() == 2423){
						c.inCWBase = false;
						c.getPA().movePlayer(2422, 3076, c.heightLevel);
					}
				}
				break;
			case 4423:
				Region.setClipping(x, y, c.heightLevel, 0);
				Region.setClipping(x, y - 1, c.heightLevel, 0);
				players = CastleWars.gameRoom.keySet().iterator();
				c.getPA().object(-1, x, y, 0, 0);
				c.getPA().object(id + 2, x, y - 1, 0, 0);
				while(players.hasNext()){
					Client player = players.next();
					if(player.playerId == c.playerId)
						continue;
					player.getPA().object(-1, x, y, 0, 0);
					player.getPA().object(id + 2, x, y - 1, 0, 0);
				}
				players = null;
				break;
			case 4424:
				Region.setClipping(x, y, c.heightLevel, 0);
				Region.setClipping(x, y - 1, c.heightLevel, 0);
				players = CastleWars.gameRoom.keySet().iterator();
				c.getPA().object(-1, x, y, 0, 0);
				c.getPA().object(id + 2, x, y - 1, -2, 0);
				while(players.hasNext()){
					Client player = players.next();
					if(player.playerId == c.playerId)
						continue;
					player.getPA().object(-1, x, y, 0, 0);
					player.getPA().object(id + 2, x, y - 1, -2, 0);
				}
				players = null;
				break;
			case 4425:
				Region.setClipping(x, y, c.heightLevel, 66690);
				Region.setClipping(x, y + 1, c.heightLevel, 82080);
				players = CastleWars.gameRoom.keySet().iterator();
				c.getPA().object(-1, x, y, 0, 0);
				c.getPA().object(id - 2, x, y + 1, -1, 0);
				while(players.hasNext()){
					Client player = players.next();
					if(player.playerId == c.playerId)
						continue;
					player.getPA().object(-1, x, y, 0, 0);
					player.getPA().object(id - 2, x, y + 1, -1, 0);
				}
				players = null;
				break;
			case 4426:
				Region.setClipping(x, y, c.heightLevel, 5130);
				Region.setClipping(x, y + 1, c.heightLevel, 20520);
				players = CastleWars.gameRoom.keySet().iterator();
				c.getPA().object(-1, x, y, 0, 0);
				c.getPA().object(id - 2, x, y + 1, -1, 0);
				while(players.hasNext()){
					Client player = players.next();
					if(player.playerId == c.playerId)
						continue;
					player.getPA().object(-1, x, y, 0, 0);
					player.getPA().object(id - 2, x, y + 1, -1, 0);
				}
				players = null;
				break;
			case 4427:
				Region.setClipping(x, y, c.heightLevel, 0);
				Region.setClipping(x, y + 1, c.heightLevel, 0);
				players = CastleWars.gameRoom.keySet().iterator();
				c.getPA().object(-1, x, y, 0, 0);
				c.getPA().object(id + 2, x, y + 1, -2, 0);
				while(players.hasNext()){
					Client player = players.next();
					if(player.playerId == c.playerId)
						continue;
					player.getPA().object(-1, x, y, 0, 0);
					player.getPA().object(id + 2, x, y + 1, -2, 0);
				}
				players = null;
				break;
			case 4428:
				Region.setClipping(x, y, c.heightLevel, 0);
				Region.setClipping(x, y + 1, c.heightLevel, 0);
				players = CastleWars.gameRoom.keySet().iterator();
				c.getPA().object(-1, x, y, 0, 0);
				c.getPA().object(id + 2, x, y + 1, 0, 0);
				while(players.hasNext()){
					Client player = players.next();
					if(player.playerId == c.playerId)
						continue;
					player.getPA().object(-1, x, y, 0, 0);
					player.getPA().object(id + 2, x, y + 1, 0, 0);
				}
				players = null;
				break;
			case 4429:
				Region.setClipping(x, y, 0, 20520);
				Region.setClipping(x, y - 1, 0, 5130);
				players = CastleWars.gameRoom.keySet().iterator();
				c.getPA().object(-1, x, y, 0, 0);
				c.getPA().object(id - 2, x, y - 1, 1, 0);
				while(players.hasNext()){
					Client player = players.next();
					if(player.playerId == c.playerId)
						continue;
					player.getPA().object(-1, x, y, 0, 0);
					player.getPA().object(id - 2, x, y - 1, 1, 0);
				}
				players = null;
				break;
			case 4430:
				Region.setClipping(x, y, 0, 16416);
				Region.setClipping(x, y - 1, 0, 66690);
				players = CastleWars.gameRoom.keySet().iterator();
				c.getPA().object(-1, x, y, 0, 0);
				c.getPA().object(id - 2, x, y - 1, 1, 0);
				while(players.hasNext()){
					Client player = players.next();
					if(player.playerId == c.playerId)
						continue;
					player.getPA().object(-1, x, y, 0, 0);
					player.getPA().object(id - 2, x, y - 1, 1, 0);
				}
				players = null;
				break;
			case 4465:
				Region.setClipping(x, y, c.heightLevel, 0);
				Region.setClipping(x - 1, y, c.heightLevel, 0);
				players = CastleWars.gameRoom.keySet().iterator();
				c.getPA().object(-1, x, y, 0, 0);
				c.getPA().object(id + 1, x - 1, y, -3, 0);
				while(players.hasNext()){
					Client player = players.next();
					if(player.playerId == c.playerId)
						continue;
					player.getPA().object(-1, x, y, 0, 0);
					player.getPA().object(id + 1, x - 1, y, -3, 0);
				}
				break;
			case 4466:
				Region.setClipping(x, y, c.heightLevel, 4104);
				Region.setClipping(x + 1, y, c.heightLevel, 83106);
				players = CastleWars.gameRoom.keySet().iterator();
				c.getPA().object(-1, x, y, 0, 0);
				c.getPA().object(id - 1, x + 1, y, 0, 0);
				while(players.hasNext()){
					Client player = players.next();
					if(player.playerId == c.playerId)
						continue;
					player.getPA().object(-1, x, y, 0, 0);
					player.getPA().object(id - 1, x + 1, y, 0, 0);
				}
				break;
			case 4467:
				Region.setClipping(x, y, c.heightLevel, 0);
				Region.setClipping(x + 1, y, c.heightLevel, 0);
				players = CastleWars.gameRoom.keySet().iterator();
				c.getPA().object(-1, x, y, 0, 0);
				c.getPA().object(id + 1, x + 1, y, -1, 0);
				while(players.hasNext()){
					Client player = players.next();
					if(player.playerId == c.playerId)
						continue;
					player.getPA().object(-1, x, y, 0, 0);
					player.getPA().object(id + 1, x + 1, y, -1, 0);
				}
				break;
			case 4468:
				Region.setClipping(x, y, c.heightLevel, 65664);
				Region.setClipping(x - 1, y, c.heightLevel, 21546);
				players = CastleWars.gameRoom.keySet().iterator();
				c.getPA().object(-1, x, y, 0, 0);
				c.getPA().object(id - 1, x - 1, y, -2, 0);
				while(players.hasNext()){
					Client player = players.next();
					if(player.playerId == c.playerId)
						continue;
					player.getPA().object(-1, x, y, 0, 0);
					player.getPA().object(id - 1, x - 1, y, -2, 0);
				}
				break;
			case 4470:
				if(CastleWars.getTeamNumber(c) == 1){
					c.sendMessage("You are not allowed in the other teams spawn point.");
					break;
				}
				if(c.getItems().playerHasEquipped(CastleWars.SARA_BANNER) || c.getItems().playerHasEquipped(CastleWars.ZAMMY_BANNER)){
					c.sendMessage("You can not enter this area with the flag on.");
					break;
				}
				if(x == 2373 && y == 3126){
					if(c.getY() == 3126){
						c.inCWBase = true;
						c.getPA().movePlayer(2373, 3127, 1);
					}else if(c.getY() == 3127){
						c.inCWBase = false;
						c.getPA().movePlayer(2373, 3126, 1);
					}
				}else if(x == 2377 && y == 3131){
					if(c.getX() == 2376){
						c.inCWBase = false;
						c.getPA().movePlayer(2377, 3131, 1);
					}else if(c.getX() == 2377){
						c.inCWBase = true;
						c.getPA().movePlayer(2376, 3131, 1);
					}
				}
				break;
			case 4417:
				if(x == 2428 && y == 3081 && c.heightLevel == 1){
					c.getPA().movePlayer(2430, 3080, 2);
				}
				if(x == 2425 && y == 3074 && c.heightLevel == 2){
					c.getPA().movePlayer(2426, 3074, 3);
					if(CastleWars.saraFlag == 0)
						c.getPA().object(4902, CastleWars.FLAG_STANDS[0][0], CastleWars.FLAG_STANDS[0][1], 0, 10);
					else
						c.getPA().object(4377, CastleWars.FLAG_STANDS[0][0], CastleWars.FLAG_STANDS[0][1], 0, 10);
				}
				if(x == 2419 && y == 3078 && c.heightLevel == 0){
					c.getPA().movePlayer(2420, 3080, 1);
				}
				break;
			case 4415:
				c.getPA().object(-1, CastleWars.FLAG_STANDS[0][0], CastleWars.FLAG_STANDS[0][1], 0, 10);
				c.getPA().object(-1, CastleWars.FLAG_STANDS[1][0], CastleWars.FLAG_STANDS[1][1], 0, 10);
				if(x == 2419 && y == 3080 && c.heightLevel == 1){
					c.getPA().movePlayer(2419, 3077, 0);
				}
				if(x == 2430 && y == 3081 && c.heightLevel == 2){
					c.getPA().movePlayer(2427, 3081, 1);
				}
				if(x == 2425 && y == 3074 && c.heightLevel == 3){
					c.getPA().movePlayer(2425, 3077, 2);
				}
				if(x == 2374 && y == 3133 && c.heightLevel == 3){
					c.getPA().movePlayer(2374, 3130, 2);
				}
				if(x == 2369 && y == 3126 && c.heightLevel == 2){
					c.getPA().movePlayer(2372, 3126, 1);
				}
				if(x == 2380 && y == 3127 && c.heightLevel == 1){
					c.getPA().movePlayer(2380, 3130, 0);
				}
				break;
			case 4411:
				if(x == 2421 && y == 3073 && c.heightLevel == 1){
					c.getPA().movePlayer(c.getX(), c.getY(), 0);
				}
				break;
			case 4419:
				if(x == 2417 && y == 3074 && c.heightLevel == 0){
					if(c.getX() >= 2414 && c.getX() <= 2416)
						c.getPA().movePlayer(2417, 3077, 0);
					else
						c.getPA().movePlayer(2416, 3074, 0);
				}
				break;
			case 4420:
				if(x == 2382 && y == 3131 && c.heightLevel == 0){
					if(c.getX() >= 2383 && c.getX() <= 2385)
						c.getPA().movePlayer(2382, 3130, 0);
					else
						c.getPA().movePlayer(2383, 3133, 0);
				}
				break;
			case 4911:
				if(x == 2421 && y == 3073 && c.heightLevel == 1){
					c.getPA().movePlayer(2421, 3074, 0);
				}
				if(x == 2378 && y == 3134 && c.heightLevel == 1){
					c.getPA().movePlayer(2378, 3133, 0);
				}
				break;
			case 1747:
				if(x == 2421 && y == 3073 && c.heightLevel == 0){
					c.getPA().movePlayer(2421, 3074, 1);
				}
				if(x == 2378 && y == 3134 && c.heightLevel == 0){
					c.getPA().movePlayer(2378, 3133, 1);
				}
				break;
			case 4912:
				if(x == 2430 && y == 3082 && c.heightLevel == 0){
					c.getPA().movePlayer(c.getX(), c.getY() + 6400, 0);
				}
				if(x == 2369 && y == 3125 && c.heightLevel == 0){
					c.getPA().movePlayer(2369, 9526, 0);
				}
				break;
			case 1757:
				if(x == 2399 && y == 9499){
					c.getPA().movePlayer(2400, 3107, 0);
				}else if(x == 2369 && y == 9525){
					c.getPA().movePlayer(2369, 3126, 0);
				}else if(x == 2430 && y == 9482){
					c.getPA().movePlayer(2430, 3081, 0);
				}else{
					c.getPA().movePlayer(2399, 3100, 0);
				}
				break;

			case 4418:
				if(x == 2380 && y == 3127 && c.heightLevel == 0){
					c.getPA().movePlayer(2379, 3127, 1);
				}
				if(x == 2369 && y == 3126 && c.heightLevel == 1){
					c.getPA().movePlayer(2369, 3127, 2);
				}
				if(x == 2374 && y == 3131 && c.heightLevel == 2){
					c.getPA().movePlayer(2373, 3133, 3);
					if(CastleWars.zammyFlag == 0)
						c.getPA().object(4903, CastleWars.FLAG_STANDS[1][0], CastleWars.FLAG_STANDS[1][1], 0, 10);
					else
						c.getPA().object(4378, CastleWars.FLAG_STANDS[1][0], CastleWars.FLAG_STANDS[1][1], 0, 10);
				}
				break;
			case 4437:
				if(x == 2400 && y == 9512){
					if(c.absY == 9514)
						c.getPA().movePlayer(2400, 9511, 0);
					else
						c.getPA().movePlayer(2400, 9514, 0);
				}else if(x == 2391 && y == 9501){
					if(c.absX == 2393)
						c.getPA().movePlayer(2390, 9502, 0);
					else
						c.getPA().movePlayer(2393, 9502, 0);
				}else if(x == 2409 && y == 9503){
					if(c.absX == 2411)
						c.getPA().movePlayer(2408, 9503, 0);
					else
						c.getPA().movePlayer(2411, 9503, 0);
				}else if(x == 2401 && y == 9494){
					if(c.absY == 9496)
						c.getPA().movePlayer(2401, 9493, 0);
					else
						c.getPA().movePlayer(2401, 9496, 0);
				}
				break;
			case 1568:
				if(x == 2400 && y == 3108){
					c.getPA().movePlayer(2399, 9500, 0);
				}else{
					c.getPA().movePlayer(2400, 9507, 0);
				}
				break;
			case 6281:
				c.getPA().movePlayer(2370, 3132, 2);
				break;
			case 4472:
				if(CastleWars.isInZammy(c) && !c.getItems().playerHasEquipped(CastleWars.SARA_BANNER) && !c.getItems().playerHasEquipped(CastleWars.ZAMMY_BANNER))
					c.getPA().movePlayer(2370, 3132, 1);
				break;
			case 6280:
				c.getPA().movePlayer(2429, 3075, 2);
				break;
			case 4471:
				if(CastleWars.isInSara(c) && !c.getItems().playerHasEquipped(CastleWars.SARA_BANNER) && !c.getItems().playerHasEquipped(CastleWars.ZAMMY_BANNER))
					c.getPA().movePlayer(2429, 3075, 1);
				break;
			case 4406:
			case 4407:
				CastleWars.removePlayerFromCw(c);
				break;
			case 4458:
				if(CastleWars.isInCw(c) && c.inCWBase){
					c.startAnimation(881);
					c.inventory.addItem(4049, 1, -1);
					c.sendMessage("You get some bandages");
				}
				break;
			case 4902: // sara flag
			case 4377:
				if(c.isDead)
					break;
				switch(CastleWars.getTeamNumber(c)){
					case 1:
						CastleWars.returnFlag(c, c.playerEquipment[c.playerWeapon]);
						break;
					case 2:
						CastleWars.captureFlag(c);
						break;
				}
				break;
			case 4903: // zammy flag
			case 4378:
				if(c.isDead)
					break;
				switch(CastleWars.getTeamNumber(c)){
					case 1:
						CastleWars.captureFlag(c);
						break;
					case 2:
						CastleWars.returnFlag(c, c.playerEquipment[c.playerWeapon]);
						break;
				}
				break;
			case 4461: // barricades
				c.sendMessage("You get a barricade!");
				c.inventory.addItem(4053, 1, -1);
				break;
			case 4463: // explosive potion!
				c.sendMessage("You get an explosive potion!");
				c.inventory.addItem(4045, 1, -1);
				break;
			case 4464: // pickaxe table
				c.sendMessage("You get a bronzen pickaxe for mining.");
				c.inventory.addItem(1265, 1, -1);
				break;
			case 4900:
			case 4901:
				if(!c.isDead)
					CastleWars.pickupFlag(c);
			default:
				break;

		}
	}
}