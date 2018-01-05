package server.model.players;

import server.model.players.packets.*;

public class PacketHandler{

	private static PacketType packetId[] = new PacketType[256];

	static{
		SilentPacket u = new SilentPacket();
		packetId[3] = u;
		packetId[14] = new ItemOnPlayer();
		packetId[202] = u;
		packetId[77] = u;
		packetId[86] = u;
		packetId[78] = u;
		packetId[36] = u;
		packetId[226] = u;
		packetId[246] = u;
		packetId[148] = u;
		packetId[183] = u;
		packetId[230] = u;
		packetId[136] = u;
		packetId[189] = u;
		packetId[152] = u;
		packetId[200] = u;
		packetId[85] = u;
		packetId[165] = u;
		packetId[238] = u;
		packetId[234] = u;
		packetId[150] = u;
		packetId[170] = new MoveToTab();
		packetId[40] = new Dialogue();
		packetId[253] = new LightFloorLogs();
		ClickObject co = new ClickObject();
		packetId[132] = co;
		packetId[252] = co;
		packetId[70] = co;
		packetId[57] = new ItemOnNpc();
		ClickNPC cn = new ClickNPC();
		packetId[72] = cn;
		packetId[131] = cn;
		packetId[155] = cn;
		packetId[17] = cn;
		packetId[21] = cn;
		packetId[16] = new ItemClick2();
		packetId[75] = new ItemClick3();
		packetId[122] = new ClickItem();
		packetId[241] = new ClickingInGame();
		packetId[4] = new Chat();
		packetId[236] = new PickupItem();
		packetId[87] = new DropItem();
		packetId[185] = new ClickingButtons();
		packetId[130] = new ClickingStuff();
		packetId[103] = new Commands();
		packetId[214] = new MoveItems();
		packetId[237] = new MagicOnItems();
		packetId[181] = new MagicOnFloorItems();
		packetId[202] = new IdleLogout();
		AttackPlayer ap = new AttackPlayer();
		packetId[73] = ap;
		packetId[249] = ap;
		ChallengePlayer cp = new ChallengePlayer();
		packetId[125] = cp;
		packetId[127] = cp;
		packetId[128] = cp;
		packetId[139] = new Trade();
		packetId[39] = new FollowPlayer();
		packetId[41] = new WearItem();
		packetId[145] = new RemoveItem();
		packetId[117] = new Bank5();
		packetId[43] = new Bank10();
		packetId[129] = new BankAll();
		packetId[101] = new ChangeAppearance();
		PrivateMessaging pm = new PrivateMessaging();
		packetId[188] = pm;
		packetId[126] = pm;
		packetId[215] = pm;
		packetId[74] = pm;
		packetId[228] = u;
		packetId[95] = pm;
		packetId[133] = pm;
		packetId[135] = new BankX1();
		packetId[208] = new BankX2();
		NotePacket np = new NotePacket();
		packetId[88] = np;
		packetId[89] = np;
		Walking w = new Walking();
		packetId[98] = w;
		packetId[164] = w;
		packetId[248] = w;
		packetId[53] = new ItemOnItem();
		packetId[192] = new ItemOnObject();
		packetId[25] = new ItemOnGroundItem();
		ChangeRegions cr = new ChangeRegions();
		packetId[121] = cr;
		packetId[210] = cr;
		packetId[60] = new ClanChat();
		packetId[62] = new ClanSettings();
		packetId[63] = new ClanPermissions();
		packetId[64] = new SearchBank();
		packetId[140] = new ChatSetting();
		packetId[18] = u;
		packetId[35] = new ChargeOrbs();
	}

	public static void processPacket(Client c, int packetType, int packetSize){
		if(packetType < 0 || packetType >= packetId.length)
			return;
		int realSize = Client.PACKET_SIZES[packetType] == -1 ? packetSize : Client.PACKET_SIZES[packetType];
		PacketType p = packetId[packetType];
		if(p != null && packetType > 0 && packetType < 257 && realSize == packetSize){
			try{
				p.processPacket(c, packetType, packetSize);
			}catch(Exception e){
				PlayerHandler.removePlayer(c);
				e.printStackTrace();
			}
		}else{
			PlayerHandler.removePlayer(c);
			System.out.println("Unhandled packet type: " + packetType + " - size: " + packetSize);
		}
	}
}