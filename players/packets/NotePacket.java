package server.model.players.packets;

import server.model.players.Client;
import server.model.players.PacketType;
import server.util.Misc;

public class NotePacket implements PacketType{
	public void processPacket(Client c, int packetType, int packetSize){
		if(packetType == 88){
			String note = c.getInStream().readString().trim();
			if(c.noteAddTime > Misc.currentTimeSeconds())
				return;
			c.noteAddTime = Misc.currentTimeSeconds() + 1;
			if(Misc.getTextWidth(note) > 167){
				c.sendMessage("That note is too long!");
				return;
			}
			if(c.playerNotes.size() == 30){
				c.sendMessage("There is no more room for new notes!");
				return;
			}
			if(note.isEmpty())
				return;
			c.getPA().sendText("", 13800);
			c.playerNotes.add(note);
			c.getPA().sendText(note, 18801 + (c.playerNotes.size() - 1));
		}else{
			int id = c.getInStream().readSignedByte();
			if(c.noteDeleteTime > Misc.currentTimeSeconds())
				return;
			c.noteDeleteTime = Misc.currentTimeSeconds() + 1;
			if(c.playerNotes.size() <= id)
				return;
			c.playerNotes.remove(id);
			for(int i = 0; i<30; i++)
				c.getPA().sendText(i < c.playerNotes.size() ? c.playerNotes.get(i) : "", 18801 + i);
			c.getPA().sendText(c.playerNotes.isEmpty() ? "No notes" : "", 13800);
		}
	}
}