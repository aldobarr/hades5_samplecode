package server.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import server.Config;
import server.model.items.bank.BankItem;
import server.model.items.bank.Tab;
import server.model.players.Client;
import server.model.players.PlayerSave;
import server.util.Misc;
import server.util.MySQLManager;

public class DonorReward{
	public DonorReward(Client c){
		this.giveReward(c, false);
	}

	public void giveReward(Client c, boolean originalName){
		String name = originalName ? c.originalName : c.playerName;
		String query = "SELECT `amount` FROM `donor` WHERE `name` = ? AND `status` = ?";
		int status = 1;
		boolean received = false;
		String amount[] = null;
		int tickets = 0;
		try(MySQLManager dbDonor = new MySQLManager(MySQLManager.SERVER)){
			try(PreparedStatement ps = dbDonor.conn.prepareStatement(query)){
				ps.setString(1, name);
				ps.setInt(2, 0);
				try(ResultSet rs = ps.executeQuery()){
					while(rs.next()){
						status = 0;
						amount = rs.getString("amount").split(",");
						for(String a : amount)
							tickets += Integer.parseInt(a);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			if(status == 0){
				query = "UPDATE `donor` SET `status` = ?, `claim_time` = ? WHERE `name` = ? AND `status` = ?";
				try(PreparedStatement ps = dbDonor.conn.prepareStatement(query)){
					ps.setInt(1, 1);
					ps.setInt(2, Misc.currentTimeSeconds());
					ps.setString(3, name);
					ps.setInt(4, 0);
					ps.executeUpdate();
				}catch(Exception e){
					e.printStackTrace();
				}
				synchronized(c){
					if(c.playerRights == 0){
						c.playerRights = 5;
						c.setDisconnected(true);
					}
					c.Donator = 1;
				}
				short location = 0;
				synchronized(c){
					if(c.inventory.hasItem(Config.DONATION_TICKET) || c.inventory.freeSlots() > 0){
						c.inventory.addItem(Config.DONATION_TICKET, tickets, -1);
						location = 1;
					}else if(c.bank.bankHasItem(Config.DONATION_TICKET)){
						Tab tab = c.bank.findItemTab(Config.DONATION_TICKET);
						for(BankItem item : tab.tabItems)
							if(item.id == Config.DONATION_TICKET + 1)
								item.amount += tickets;
						location = 2;
					}else if(c.bank.freeSlots() > 0){
						c.bank.tabs.get(0).tabItems.add(new BankItem(Config.DONATION_TICKET + 1, tickets, -1));
						location = 2;
					}else
						c.donationPoints += tickets;
					received = true;
					PlayerSave.saveGame(c);
					c.nextMessage = "You have received your " + (location == 0 ? "donor points" : ("donation tickets." + (location == 2 ? " They are in your bank." : "")));
				}
			}else if(!originalName && !c.playerName.equalsIgnoreCase(c.originalName))
				giveReward(c, true);
			synchronized(c){
				c.forcedText = received ? "I just donated, and got my reward!" : "I was not authorized to receive a donation reward";
				c.updateRequired = true;
				c.forcedChatUpdateRequired = true;
				c.donating = false;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static String getSuffix(int i){
		String days[] = {"st", "nd", "rd"};
		String suffix = "";
		if(i > 0 && i < 4){
			suffix = days[i - 1];
		}else if(i > 3 && i < 21)
			suffix = "th";
		else if((i > 20 && i < 24) || (i > 30)){
			i = i % 10;
			if(i > 0 && i < 4)
				suffix = days[i - 1];
			else
				suffix = "th";
		}else
			suffix = "th";
		return suffix;
	}

	public static void logVoteError(Client c, int i){
		String message = "Player " + c.playerName + " failed to receive his voting reward on the " + i + getSuffix(i) + " item in the pack.";
		try(BufferedWriter out = new BufferedWriter(new FileWriter("./Data/rewardfailure.txt", true))){
			out.write(message);
			out.newLine();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
