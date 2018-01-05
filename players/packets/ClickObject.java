package server.model.players.packets;

import server.clip.region.Region;
import server.model.players.ActionHandler;
import server.model.players.Client;
import server.model.players.PacketType;

/**
 * Click Object
 */
public class ClickObject implements PacketType{
	public static final int FIRST_CLICK = 132, SECOND_CLICK = 252, THIRD_CLICK = 70;

	@Override
	public void processPacket(Client c, int packetType, int packetSize){
		c.clickObjectType = c.objectX = c.objectId = c.objectY = 0;
		c.objectYOffset = c.objectXOffset = 0;
		c.surroundCoords.clear();
		c.surround = false;
		c.getCombat().resetPlayerAttack();
		c.getPA().removeAllWindows();
		if(c.resting){
			c.resting = false;
			c.rest();
		}
		switch(packetType){
			case FIRST_CLICK:
				c.objectX = c.getInStream().readSignedWordBigEndianA();
				c.objectId = c.getInStream().readUnsignedWord();
				c.objectY = c.getInStream().readUnsignedWordA();
				c.objectDistance = 1;
				if(c.playerRights == 3)
					System.out.println("objectId: " + c.objectId + " objectX: " + c.objectX + " objectY: " + c.objectY);
				if(Math.abs(c.getX() - c.objectX) > 25 || Math.abs(c.getY() - c.objectY) > 25){
					c.resetWalkingQueue();
					break;
				}
				if(!ActionHandler.ignoreObjectCheck(c.objectId)){
					if(!Region.objectExists(c.objectId, c.objectX, c.objectY, c.heightLevel) && !ActionHandler.multiZObject(c.objectId))
						break;
					if(ActionHandler.multiZObject(c.objectId) && !Region.objectExists(c.objectId, c.objectX, c.objectY, 0))
						break;
				}
				for(int i = 0; i < c.getRunecrafting().altarID.length; i++){
					if(c.objectId == c.getRunecrafting().altarID[i]){
						c.getRunecrafting().craftRunes(c.objectId);
					}
				}
				switch(c.objectId){
					case 6:
					case 7:
					case 8:
					case 9:
						c.objectXOffset = c.objectYOffset = 1;
						c.objectDistance = 2;
						break;
					case 3044:
						c.objectDistance = 3;
						break;
					case 1738:
						c.objectDistance = 2;
						break;
					case 3192:
						c.objectDistance = 3;
						break;
					case 4463:
						c.objectYOffset = 1;
						break;
					case 28140:
						if(c.objectY == 3776){
							c.objectYOffset = 1;
							c.objectDistance = 2;
						}else{
							c.objectXOffset = c.objectYOffset = 1;
							c.objectDistance = 2;
						}
						c.objectX = 3321;
						c.objectY = c.objectY == 3774 ? 3774 : 3776;
						break;
					case 28214:
						c.objectYOffset = 2;
						c.objectDistance = 3;
						c.objectX = c.objectX == 3290 ? 3290 : 3298;
						c.objectY = c.objectY == 3831 ? 3831 : 3720;
						break;
					case 6434:
						c.objectX = 3084;
						c.objectY = 3272;
						break;
					case 10699:
						c.objectX = 2004;
						c.objectY = 4426;
						break;
					case 10707:
						c.objectX = c.objectX == 2006 ? 2006 : 2009;
						c.objectY = 4431;
						break;
					case 10708:
						c.objectX = c.objectX == 2006 ? 2006 : 2009;
						c.objectY = 4431;
						break;
					case 8987:
						c.objectX = 2523;
						c.objectY = 4777;
						break;
					case 9319:
					case 9320:
						c.objectX = c.objectX == 3447 ? 3447 : 3422;
						c.objectY = c.objectY == 3576 ? 3576 : 3550;
						break;
					case 2469:
						c.objectX = 3090;
						c.objectY = 3507;
						break;
					case 2465:
						c.objectX = 2315;
						c.objectY = 9805;
						break;
					case 1766:
						c.objectX = 3069;
						c.objectY = 10256;
						break;
					case 1765:
						c.objectX = 3017;
						c.objectY = 3849;
						break;
					case 1816:
						c.objectX = 3067;
						c.objectY = 10252;
						break;
					case 1817:
						c.objectX = 2271;
						c.objectY = 4680;
						break;
					case 5160:
						c.objectDistance = 3;
						c.objectYOffset = 2;
						break;
					case 272:
						c.objectYOffset = 1;
						c.objectDistance = 0;
						c.objectX = 3018;
						c.objectY = 3957;
						break;
					case 273:
						c.objectYOffset = 1;
						c.objectDistance = 0;
						c.objectX = 3018;
						c.objectY = 3957;
						break;
					case 245:
						c.objectYOffset = -1;
						c.objectDistance = 0;
						c.objectX = c.objectX == 3017 ? 3017 : 3019;
						c.objectY = 3959;
						break;
					case 246:
						c.objectYOffset = 1;
						c.objectDistance = 0;
						c.objectX = c.objectX == 3017 ? 3017 : 3019;
						c.objectY = 3959;
						break;
					case 61:
						c.objectDistance = 2;
						c.objectX = 3084;
						c.objectY = 3489;
						break;
					case 409:
						c.objectDistance = 2;
						c.objectX = 3091;
						c.objectY = 3506;
						break;
					case 410:
						c.objectX = 3104;
						c.objectY = 3508;
						break;
					case 6552:
						c.objectDistance = 3;
						c.objectX = 3096;
						c.objectY = 3500;
						break;
					case 1530:
						c.objectX = 2564;
						c.objectY = 3310;
						break;
					case 1814:
						c.objectX = 2561;
						c.objectY = 3311;
						break;
					case 1815:
						c.objectX = 3153;
						c.objectY = 3923;
						break;
					case 4493:
						c.objectDistance = 5;
						c.objectX = 3434;
						c.objectY = 3537;
						break;
					case 4494:
						c.objectDistance = 5;
						c.objectX = 3434;
						c.objectY = 3537;
						break;
					case 4495:
						c.objectDistance = 5;
						c.objectX = 3413;
						c.objectY = 3540;
						break;
					case 4496:
						c.objectDistance = 5;
						c.objectX = 3415;
						c.objectY = 3540;
						break;
					case 14314:
						c.objectX = 2660;
						c.objectY = 2639;
						break;
					case 14315:
						c.objectX = 2658;
						c.objectY = 2639;
						break;
					case 10229:
						c.objectDistance = 2;
						c.objectX = 2899;
						c.objectY = 4449;
						break;
					case 10230:
						c.objectX = 1911;
						c.objectY = 4367;
						break;
					case 6522:
						c.objectDistance = 2;
						break;
					case 8959:
						c.objectYOffset = 1;
						break;
					case 4417:
						if(c.objectX == 2425 && c.objectY == 3074)
							c.objectYOffset = 2;
						break;
					case 4420:
						if(c.getX() >= 2383 && c.getX() <= 2385){
							c.objectYOffset = 1;
						}else{
							c.objectYOffset = -2;
						}
						break;
					case 2878:
						c.objectDistance = 3;
						c.objectX = 2541;
						c.objectY = 4719;
						break;
					case 2879:
						c.objectDistance = 3;
						c.objectX = 2508;
						c.objectY = 4686;
						break;
					case 2558:
						c.objectDistance = 0;
						if(c.absX > c.objectX && c.objectX == 3044)
							c.objectXOffset = 1;
						if(c.absY > c.objectY)
							c.objectYOffset = 1;
						if(c.absX < c.objectX && c.objectX == 3038)
							c.objectXOffset = -1;
						break;
					// Start Castle Wars.
					case 4387: // Saradomin
						c.objectDistance = 2;
						c.objectYOffset = 1;
						c.objectX = 2436;
						c.objectY = 3096;
						break;
					case 4388: // Zamorak
						c.objectDistance = 2;
						c.objectX = 2436;
						c.objectY = 3082;
						break;
					case 4408: // Guthix
						c.objectDistance = 1;
						c.surround = true;
						c.surroundCoords.add(new int[]{2436, 3091});
						c.surroundCoords.add(new int[]{2437, 3091});
						c.surroundCoords.add(new int[]{2438, 3090});
						c.surroundCoords.add(new int[]{2438, 3089});
						c.surroundCoords.add(new int[]{2437, 3088});
						c.surroundCoords.add(new int[]{2436, 3088});
						c.objectX = 2436;
						c.objectY = 3089;
						break;
					case 4389: // Sara wating room portal
						c.objectDistance = 1;
						c.surround = true;
						c.surroundCoords.add(new int[]{2375, 9485});
						c.surroundCoords.add(new int[]{2374, 9485});
						c.surroundCoords.add(new int[]{2373, 9486});
						c.surroundCoords.add(new int[]{2373, 9487});
						c.surroundCoords.add(new int[]{2374, 9488});
						c.surroundCoords.add(new int[]{2375, 9488});
						c.surroundCoords.add(new int[]{2376, 9487});
						c.surroundCoords.add(new int[]{2376, 9486});
						c.objectX = 2374;
						c.objectY = 9486;
						break;
					case 4390: // Zammy waiting room portal
						c.objectDistance = 1;
						c.surround = true;
						c.surroundCoords.add(new int[]{2423, 9529});
						c.surroundCoords.add(new int[]{2423, 9530});
						c.surroundCoords.add(new int[]{2424, 9531});
						c.surroundCoords.add(new int[]{2425, 9531});
						c.surroundCoords.add(new int[]{2426, 9530});
						c.surroundCoords.add(new int[]{2426, 9529});
						c.surroundCoords.add(new int[]{2425, 9528});
						c.surroundCoords.add(new int[]{2424, 9528});
						c.objectX = 2424;
						c.objectY = 9529;
						break;
					// End Castle Wars.
					case 9369:
						c.objectX = 2399;
						c.objectY = 5176;
						break;
					case 9368:
						c.objectX = 2399;
						c.objectY = 5168;
						break;
					case 10284:
						c.objectX = 3551;
						c.objectY = 9695;
						break;
					case 9356:
						c.objectX = 2437;
						c.objectY = 5166;
						c.objectDistance = 2;
						break;
					case 9357:
						c.objectX = 2412;
						c.objectY = 5118;
						break;
					case 5959:
						c.objectDistance = 2;
						c.objectX = 3090;
						c.objectY = 3956;
						break;
					case 5960:
						c.objectDistance = 2;
						c.objectX = 2539;
						c.objectY = 4712;
						break;
					case 9293:
						c.objectDistance = 2;
						c.objectX = c.objectX == 2887 ? 2887 : 2890;
						c.objectY = 9799;
						break;
					case 4418:
						if(c.objectX == 2374 && c.objectY == 3131)
							c.objectYOffset = -2;
						else if(c.objectX == 2369 && c.objectY == 3126)
							c.objectXOffset = 2;
						else if(c.objectX == 2380 && c.objectY == 3127)
							c.objectYOffset = 2;
						else if(c.objectX == 2369 && c.objectY == 3126)
							c.objectXOffset = 2;
						else if(c.objectX == 2374 && c.objectY == 3131)
							c.objectYOffset = -2;
						break;
					case 9706:
						c.objectDistance = 0;
						c.objectXOffset = 1;
						c.objectX = 3104;
						c.objectY = 3956;
						break;
					case 9707:
						c.objectDistance = 0;
						c.objectYOffset = -1;
						c.objectX = 3105;
						c.objectY = 3952;
						break;
					case 9294:
						c.objectX = 2879;
						c.objectY = 9813;
						break;
					case 13999:
						c.getPA().startTeleport(3087, 3498, 0, true);
						c.teleportToX = 3093;
						c.teleportToY = 3487;

						break;
					case 4419:
						if(c.getX() >= 2414 && c.getX() <= 2416)
							c.objectYOffset = 0;
						else
							c.objectYOffset = 3;
						break;
					case 6707: // verac
						c.objectYOffset = 3;
						c.objectX = 3578;
						c.objectY = 9703;
						break;
					case 6823:
						c.objectDistance = 2;
						c.objectYOffset = 1;
						break;

					case 6706: // torag
						c.objectXOffset = 2;
						c.objectX = 3565;
						c.objectY = 9683;
						break;
					case 6772:
						c.objectDistance = 2;
						c.objectYOffset = 1;
						break;

					case 6705: // karils
						c.objectYOffset = -1;
						c.objectX = 3546;
						c.objectY = 9685;
						break;
					case 28213:
						c.objectXOffset = 1;
						c.objectDistance = 2;
						break;
					case 6822:
						c.objectDistance = 2;
						c.objectYOffset = 1;
						break;

					case 6704: // guthan stairs
						c.objectYOffset = -1;
						c.objectX = 3534;
						c.objectY = 9705;
						break;
					case 6773:
						c.objectDistance = 2;
						c.objectXOffset = 1;
						c.objectYOffset = 1;
						break;

					case 6703: // dharok stairs
						c.objectXOffset = -1;
						c.objectX = 3557;
						c.objectY = 9718;
						break;
					case 6771:
						c.objectDistance = 2;
						c.objectXOffset = 1;
						c.objectYOffset = 1;
						break;

					case 6702: // ahrim stairs
						c.objectXOffset = -1;
						c.objectX = 3558;
						c.objectY = 9703;
						break;
					case 6821:
						c.objectDistance = 2;
						c.objectXOffset = 1;
						c.objectYOffset = 1;
						break;
					case 4437:
						if(c.absY >= 9514 && c.objectX == 2400 && c.objectY == 9512)
							c.objectYOffset = 1;
						else if(c.absY >= 9496 && c.objectX == 2401 && c.objectY == 9494)
							c.objectYOffset = 1;
						else if(c.absX >= 2411 && c.objectX == 2409 && c.objectY == 9503)
							c.objectXOffset = 1;
						else if(c.absX >= 2393 && c.objectX == 2391 && c.objectY == 9501)
							c.objectXOffset = 1;
						break;
					case 1276:
					case 1278:// trees
					case 1281: // oak
					case 1308: // willow
					case 1307: // maple
					case 1309: // yew
					case 1306: // yew
						c.objectDistance = 3;
						break;
					default:
						c.objectDistance = 1;
						c.objectXOffset = 0;
						c.objectYOffset = 0;
						break;
				}
				if(c.goodDistance(c.objectX + c.objectXOffset, c.objectY + c.objectYOffset, c.getX(), c.getY(), c.objectDistance)){
					c.getActions().firstClickObject(c.objectId, c.objectX, c.objectY);
				}else{
					c.clickObjectType = 1;
				}
				break;

			case SECOND_CLICK:
				c.objectId = c.getInStream().readUnsignedWordBigEndianA();
				c.objectY = c.getInStream().readSignedWordBigEndian();
				c.objectX = c.getInStream().readUnsignedWordA();
				c.objectDistance = 1;
				if(c.playerRights >= 3){
					System.out.println("objectId: " + c.objectId + "  ObjectX: " + c.objectX + "  objectY: " + c.objectY + " Xoff: " + (c.getX() - c.objectX) + " Yoff: " + (c.getY() - c.objectY));
				}
				if(!ActionHandler.ignoreObjectCheck(c.objectId))
					if(!Region.objectExists(c.objectId, c.objectX, c.objectY, c.heightLevel))
						break;
				switch(c.objectId){
					case 6:
					case 7:
					case 8:
					case 9:
						c.objectXOffset = c.objectYOffset = 1;
						c.objectDistance = 2;
						break;
					case 6163:
					case 6165:
					case 6166:
					case 6164:
					case 6162:
						c.objectDistance = 2;
						break;
					default:
						c.objectDistance = 1;
						c.objectXOffset = 0;
						c.objectYOffset = 0;
						break;

				}
				if(c.goodDistance(c.objectX + c.objectXOffset, c.objectY + c.objectYOffset, c.getX(), c.getY(), c.objectDistance)){
					c.getActions().secondClickObject(c.objectId, c.objectX, c.objectY);
				}else{
					c.clickObjectType = 2;
				}
				break;

			case THIRD_CLICK:
				c.objectX = c.getInStream().readSignedWordBigEndian();
				c.objectY = c.getInStream().readUnsignedWord();
				c.objectId = c.getInStream().readUnsignedWordBigEndianA();

				if(c.playerRights >= 3){
					System.out.println("objectId: " + c.objectId + "  ObjectX: " + c.objectX + "  objectY: " + c.objectY + " Xoff: " + (c.getX() - c.objectX) + " Yoff: " + (c.getY() - c.objectY));
				}
				if(!ActionHandler.ignoreObjectCheck(c.objectId))
					if(!Region.objectExists(c.objectId, c.objectX, c.objectY, c.heightLevel))
						break;
				switch(c.objectId){
					default:
						c.objectDistance = 1;
						c.objectXOffset = 0;
						c.objectYOffset = 0;
						break;
				}
				if(c.goodDistance(c.objectX + c.objectXOffset, c.objectY + c.objectYOffset, c.getX(), c.getY(), c.objectDistance)){
					c.getActions().secondClickObject(c.objectId, c.objectX, c.objectY);
				}else{
					c.clickObjectType = 3;
				}
				break;
		}

	}

	public void handleSpecialCase(Client c, int id, int x, int y){

	}

}
