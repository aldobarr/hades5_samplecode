package server.model.players;

import server.Config;

public class QuickPrayerHandler{
	Client c = null;
	boolean prayersOn = false;

	public QuickPrayerHandler(Client c){
		this.c = c;
	}

	public void togglePrayer(int id){
		if(!canActivate((c.cursesActive ? id + 26 : id) - 630))
			return;
		c.quickPrayers[(c.cursesActive ? id + 26 : id) - 630] = !c.quickPrayers[(c.cursesActive ? id + 26 : id) - 630];
		c.getPA().sendConfig(id, c.quickPrayers[(c.cursesActive ? id + 26 : id) - 630]);
	}
	
	private void setPrayerButton(boolean on){
		if(c != null && c.getOutStream() != null){
			synchronized(c){
				c.getOutStream().createFrame(180);
				c.getOutStream().writeByte(on ? 1 : 0);
				c.flushOutStream();
			}
		}
	}
	
	public void togglePrayerButton(){
		prayersOn = false;
		setPrayerButton(prayersOn);
	}

	public void togglePrayer(){
		for(int i = 0; i < c.quickPrayers.length; i++){
			if(c.quickPrayers[i] && !c.prayerActive[i])
				c.getCombat().activatePrayer(i);
			if(prayersOn && c.prayerActive[i])
				c.getCombat().activatePrayer(i);
		}
		prayersOn = !prayersOn;
	}

	public void saveQuickPrayers(){
		c.setSidebarInterface(5, c.cursesActive ? 22500 : 5608);
		c.saveGame();
	}

	public void resetQuickPrayers(){
		for(int i = 0; i < c.quickPrayers.length; i++){
			c.quickPrayers[i] = false;
			c.getPA().sendConfig(i + 630, 0);
		}
	}

	public boolean canActivate(int id){
		int defPray[] = {0, 5, 13, 24, 25};
		int strPray[] = {1, 6, 14, 24, 25};
		int atkPray[] = {2, 7, 15, 24, 25};
		int rangePray[] = {3, 11, 19};
		int magePray[] = {4, 12, 20};
		int turmoil = 45;
		int curseSap[] = {27, 28, 29, 30, turmoil};
		int curseLeech[] = {36, 37, 38, 39, 40, 41, 42, turmoil};
		int deflectPray[] = {32, 33, 34, 35, 43, 44};
		if(c.getPA().getLevelForXP(c.playerXP[5]) >= c.PRAYER_LEVEL_REQUIRED[id] || !Config.PRAYER_LEVEL_REQUIRED){
			switch(id){
				case 0:
				case 5:
				case 13:
					if(!c.quickPrayers[id]){
						for(int j = 0; j < defPray.length; j++){
							if(defPray[j] != id){
								c.quickPrayers[defPray[j]] = false;
								c.getPA().sendConfig(defPray[j] + 630, 0);
							}
						}
					}
					break;
				case 27:
				case 28:
				case 29:
				case 30:
					if(!c.quickPrayers[id]){
						for(int j : curseLeech){
							c.quickPrayers[j] = false;
							c.getPA().sendConfig((j - 26) + 630, 0);
						}
					}
					break;
				case 32:
				case 33:
				case 34:
				case 35:
				case 43:
				case 44:
					if(!c.quickPrayers[id]){
						for(int j : deflectPray){
							if(j != id){
								c.quickPrayers[j] = false;
								c.getPA().sendConfig((j - 26) + 630, 0);
							}
						}
					}
					break;
				case 36:
				case 37:
				case 38:
				case 39:
				case 40:
				case 41:
				case 42:
					if(!c.quickPrayers[id]){
						for(int j : curseSap){
							c.quickPrayers[j] = false;
							c.getPA().sendConfig((j - 26) + 630, 0);
						}
					}
					break;
				case 45:
					if(!c.quickPrayers[id]){
						for(int j : curseSap){
							if(j == turmoil)
								continue;
							c.quickPrayers[j] = false;
							c.getPA().sendConfig((j - 26) + 630, 0);
						}
						for(int j : curseLeech){
							if(j == turmoil)
								continue;
							c.quickPrayers[j] = false;
							c.getPA().sendConfig((j - 26) + 630, 0);
						}
					}
					break;
				case 1:
				case 6:
				case 14:
					if(!c.quickPrayers[id]){
						for(int j = 0; j < strPray.length; j++){
							if(strPray[j] != id){
								c.quickPrayers[strPray[j]] = false;
								c.getPA().sendConfig(strPray[j] + 630, 0);
							}
						}
						for(int j = 0; j < rangePray.length; j++){
							if(rangePray[j] != id){
								c.quickPrayers[rangePray[j]] = false;
								c.getPA().sendConfig(rangePray[j] + 630, 0);
							}
						}
						for(int j = 0; j < magePray.length; j++){
							if(magePray[j] != id){
								c.quickPrayers[magePray[j]] = false;
								c.getPA().sendConfig(magePray[j] + 630, 0);
							}
						}
					}
					break;
				case 2:
				case 7:
				case 15:
					if(!c.quickPrayers[id]){
						for(int j = 0; j < atkPray.length; j++){
							if(atkPray[j] != id){
								c.quickPrayers[atkPray[j]] = false;
								c.getPA().sendConfig(atkPray[j] + 630, 0);
							}
						}
						for(int j = 0; j < rangePray.length; j++){
							if(rangePray[j] != id){
								c.quickPrayers[rangePray[j]] = false;
								c.getPA().sendConfig(rangePray[j] + 630, 0);
							}
						}
						for(int j = 0; j < magePray.length; j++){
							if(magePray[j] != id){
								c.quickPrayers[magePray[j]] = false;
								c.getPA().sendConfig(magePray[j] + 630, 0);
							}
						}
					}
					break;
				case 3:// range prays
				case 11:
				case 19:
					if(!c.quickPrayers[id]){
						for(int j = 0; j < atkPray.length; j++){
							if(atkPray[j] != id){
								c.quickPrayers[atkPray[j]] = false;
								c.getPA().sendConfig(atkPray[j] + 630, 0);
							}
						}
						for(int j = 0; j < strPray.length; j++){
							if(strPray[j] != id){
								c.quickPrayers[strPray[j]] = false;
								c.getPA().sendConfig(strPray[j] + 630, 0);
							}
						}
						for(int j = 0; j < rangePray.length; j++){
							if(rangePray[j] != id){
								c.quickPrayers[rangePray[j]] = false;
								c.getPA().sendConfig(rangePray[j] + 630, 0);
							}
						}
						for(int j = 0; j < magePray.length; j++){
							if(magePray[j] != id){
								c.quickPrayers[magePray[j]] = false;
								c.getPA().sendConfig(magePray[j] + 630, 0);
							}
						}
					}
					break;
				case 4:
				case 12:
				case 20:
					if(!c.quickPrayers[id]){
						for(int j = 0; j < atkPray.length; j++){
							if(atkPray[j] != id){
								c.quickPrayers[atkPray[j]] = false;
								c.getPA().sendConfig(atkPray[j] + 630, 0);
							}
						}
						for(int j = 0; j < strPray.length; j++){
							if(strPray[j] != id){
								c.quickPrayers[strPray[j]] = false;
								c.getPA().sendConfig(strPray[j] + 630, 0);
							}
						}
						for(int j = 0; j < rangePray.length; j++){
							if(rangePray[j] != id){
								c.quickPrayers[rangePray[j]] = false;
								c.getPA().sendConfig(rangePray[j] + 630, 0);
							}
						}
						for(int j = 0; j < magePray.length; j++){
							if(magePray[j] != id){
								c.quickPrayers[magePray[j]] = false;
								c.getPA().sendConfig(magePray[j] + 630, 0);
							}
						}
					}
					break;
				case 16:
				case 17:
				case 18:
				case 21:
				case 22:
				case 23:
					for(int p = 16; p < 24; p++){
						if(id != p && p != 19 && p != 20){
							c.quickPrayers[p] = false;
							c.getPA().sendConfig(p + 630, 0);
						}
					}
					break;
				case 24:
					if(!c.quickPrayers[id]){
						for(int j = 0; j < atkPray.length; j++){
							if(atkPray[j] != id){
								c.quickPrayers[atkPray[j]] = false;
								c.getPA().sendConfig(atkPray[j] + 630, 0);
							}
						}
						for(int j = 0; j < strPray.length; j++){
							if(strPray[j] != id){
								c.quickPrayers[strPray[j]] = false;
								c.getPA().sendConfig(strPray[j] + 630, 0);
							}
						}
						for(int j = 0; j < rangePray.length; j++){
							if(rangePray[j] != id){
								c.quickPrayers[rangePray[j]] = false;
								c.getPA().sendConfig(rangePray[j] + 630, 0);
							}
						}
						for(int j = 0; j < magePray.length; j++){
							if(magePray[j] != id){
								c.quickPrayers[magePray[j]] = false;
								c.getPA().sendConfig(magePray[j] + 630, 0);
							}
						}
						for(int j = 0; j < defPray.length; j++){
							if(defPray[j] != id){
								c.quickPrayers[defPray[j]] = false;
								c.getPA().sendConfig(defPray[j] + 630, 0);
							}
						}
					}
					break;
				case 25:
					if(!c.quickPrayers[id]){ // turmoil
						for(int j = 0; j < atkPray.length; j++){
							if(atkPray[j] != id){
								c.quickPrayers[atkPray[j]] = false;
								c.getPA().sendConfig(atkPray[j] + 630, 0);
							}
						}
						for(int j = 0; j < strPray.length; j++){
							if(strPray[j] != id){
								c.quickPrayers[strPray[j]] = false;
								c.getPA().sendConfig(strPray[j] + 630, 0);
							}
						}
						for(int j = 0; j < rangePray.length; j++){
							if(rangePray[j] != id){
								c.quickPrayers[rangePray[j]] = false;
								c.getPA().sendConfig(rangePray[j] + 630, 0);
							}
						}
						for(int j = 0; j < magePray.length; j++){
							if(magePray[j] != id){
								c.quickPrayers[magePray[j]] = false;
								c.getPA().sendConfig(magePray[j] + 630, 0);
							}
						}
						for(int j = 0; j < defPray.length; j++){
							if(defPray[j] != id){
								c.quickPrayers[defPray[j]] = false;
								c.getPA().sendConfig(defPray[j] + 630, 0);
							}
						}
					}
					break;
			}
		}else{
			c.sendMessage("You need a @blu@Prayer level of " + c.PRAYER_LEVEL_REQUIRED[id] + " to use " + c.PRAYER_NAME[id] + ".");
			return false;
		}
		return true;
	}

	public void openQuickPrayersMenu(){
		c.setSidebarInterface(5, c.cursesActive ? 17500 : 17200);
		c.getPA().sendFrame106(5);
	}
}