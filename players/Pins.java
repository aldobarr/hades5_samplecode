package server.model.players;

import java.util.ArrayList;
import server.Config;
import server.model.Encryption;
import server.util.Misc;

public class Pins{
	private int state = 1;
	private String enteredPin = "";
	private String fullPin = "";
	private Client client;
	private int numbers[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
	private String message[] = {"Customers are reminded", "that they should NEVER", "tell anyone their Bank", "PINs or passwords, nor", " should they ever enter", " their PINs on any website", "form."};

	// private String yes = "Yes, I really want to do that.";
	// private String no = "No, forget I asked.";

	public Pins(Client client){
		this.client = client;
	}

	private ArrayList<Integer> getInts(){
		ArrayList<Integer> temp = new ArrayList<Integer>();
		for(int i = 0; i < numbers.length; i++)
			temp.add(numbers[i]);
		return temp;
	}

	public void reset(){
		client.bankPin = "";
		client.attempts = 3;
		enteredPin = "";
		fullPin = "";
	}

	
	public void open(){
		if(!fullPin.equalsIgnoreCase("") && !client.settingPin){
			client.getPA().openUpBank();
			return;
		}
		enteredPin = "";
		client.getPA().showInterface(7424);
		resend();
		state = 1;
		int days = getDays();
		client.getPA().sendText(client.resetPin > 0 ? ("YOUR PIN WILL BE DELETED IN " + days + " DAY" + (days > 1 ? "S" : "")) : "", 14923);
		client.getPA().sendText("?", 14913);
		client.getPA().sendText("?", 14914);
		client.getPA().sendText("?", 14915);
	}
	
	public void close(){
		enteredPin = "";
		client.getPA().removeAllWindows();
		state = 1;
	}

	private int getDays(){
		return (int)Math.ceil((double)(client.resetPin - Misc.currentTimeSeconds()) / (double)86400);
	}

	public void openSettings(){
		client.getPA().showInterface(14924);
		for(int i = 0; i < message.length; i++)
			client.getPA().sendText(message[i], 15038 + i);
		if(client.setPin){
			client.getPA().sendText("", 15075);
			client.getPA().sendText("", 15076);
			client.getPA().sendText("Change your PIN", 15078);
			client.getPA().sendText("Delete your PIN", 15079);
			client.getPA().sendText("Change your recovery delay", 15080);
			client.getPA().sendText("", 15082);
			client.getPA().sendText("@gre@Bank PIN set", 15105);
		}else{
			client.getPA().sendText("Set a PIN", 15075);
			client.getPA().sendText("Change your recovery delay", 15076);
			client.getPA().sendText("", 15078);
			client.getPA().sendText("", 15079);
			client.getPA().sendText("", 15080);
			client.getPA().sendText("", 15082);
			client.getPA().sendText("No PIN set", 15105);
		}
		client.getPA().sendText("X days", 15107);
		client.getPA().sendText("", 15171);
		client.getPA().sendText("", 15176);
	}

	private void resend(){
		if(!fullPin.equalsIgnoreCase("") && !client.settingPin){
			client.getPA().openUpBank();
			if(client.resetPin > 0){
				client.sendMessage("Your pin will not be reset.");
				client.resetPin = 0;
				client.resetPinNow = false;
			}
			return;
		}
		mixNumbers();
		switch(state){
			case 1:
				client.getPA().sendText("First click the FIRST digit", 15313);
				break;
			case 2:
				client.getPA().sendText("Then click the SECOND digit", 15313);
				client.getPA().sendText("*", 14913);
				break;
			case 3:
				client.getPA().sendText("Then click the THIRD digit", 15313);
				client.getPA().sendText("*", 14914);
				break;
			case 4:
				client.getPA().sendText("And lastly click the FOURTH digit", 15313);
				client.getPA().sendText("*", 14915);
				break;
		}
		sendPins();
	}

	public void pinEnter(int button){
		if(state > 0 && state < 5)
			enterPin(button, state);
	}

	public void handleIDK(){
		if(!client.setPin){
			client.sendMessage("You do not have a bank pin!");
			return;
		}
		client.resetPin = Misc.currentTimeSeconds() + Config.ONE_WEEK;
		String messages[] = {"Your pin will be reset in one week.", "If you remember your pin, enter it at any time before the week", "and the pin will not be reset."};
		for(String msg : messages)
			client.sendMessage(msg);
		client.getPA().closeAllWindows();
	}

	private void enterPin(int button, int which){
		for(int i = 0; i < getActionButtons().length; i++){
			if(getActionButtons()[i] == button){
				enteredPin += getBankPins()[i] + "";
			}
		}
		Encryption hash = new Encryption();
		switch(which){
			case 1:
				state = 2;
				resend();
				break;
			case 2:
				state = 3;
				resend();
				break;
			case 3:
				state = 4;
				resend();
				break;
			case 4:
				if(!client.setPin && !client.settingPin){
					client.sendMessage("Re-enter your bank pin.");
					client.bankPin = hash.getHash(Config.HASH, "saltedhades5" + enteredPin.trim() + client.originalName.toLowerCase());
					fullPin = client.bankPin;
					client.settingPin = true;
					open();
					resend();
				}else if(client.settingPin){
					if(client.bankPin.equals(hash.getHash(Config.HASH,  "saltedhades5" + enteredPin.trim() + client.originalName.toLowerCase()))){
						client.settingPin = false;
						client.setPin = true;
						client.getPA().closeAllWindows();
						client.sendMessage("You have successfully set your bank pin.");
					}else{
						client.sendMessage("The pins you entered did not match. Please try again.");
						client.settingPin = client.setPin = false;
						client.bankPin = "";
						fullPin = "";
						open();
					}
					client.saveGame();						
				}else{
					if(client.bankPin.equals(hash.getHash(Config.HASH, "saltedhades5" + enteredPin.trim() + client.originalName.toLowerCase()))){
						client.sendMessage("You have successfully entered your bank pin.");
						fullPin = client.bankPin;
						resend();
					}else{
						client.sendMessage("The pin you entered is incorrect.");
						close();
					}
				}
				state = 1;
				break;
		}
	}

	private void sendPins(){
		if(!fullPin.equalsIgnoreCase("") && !client.settingPin){
			client.getPA().openUpBank();
			return;
		}
		for(int i = 0; i < getBankPins().length; i++){
			client.getPA().sendText("" + getBankPins()[i], 14883 + i);
		}
	}

	private void mixNumbers(){
		for(int i = 0; i < bankPins.length; i++){
			bankPins[i] = -1;
		}
		ArrayList<Integer> nums = getInts();
		int i = 0;
		while(nums.size() > 0){
			int index = Misc.random(nums.size() - 1);
			bankPins[i++] = nums.remove(index);
		}
		sendPins();
	}

	private int[] getBankPins(){
		return bankPins;
	}

	private int[] getActionButtons(){
		return actionButtons;
	}

	private int bankPins[] = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

	private int actionButtons[] = {58025, 58026, 58027, 58028, 58029, 58030, 58031, 58032, 58033, 58034};

	public String getFullPin(){
		return fullPin;
	}
}