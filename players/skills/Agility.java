package server.model.players.skills;

import server.Config;
import server.model.players.Client;

public class Agility{

	private Client c;

	public Agility(Client c){
		this.c = c;
	}

	private boolean[] gnomeCourse = new boolean[6];
	private final int[] EXP = {800, 800, 500, 800, 500, 800, 800, 3900};

	public static boolean inProperTopObstacle(int absX, int absY){
		return ((absX == 2472 && absY >= 3418 && absY <= 3421)
				|| (absX == 2473 && absY >= 3418 && absY <= 3420)
				|| (absX >= 2474 && absX <= 2475 && absY >= 3418 && absY <= 3421)
				|| (absX >= 2476 && absX <= 2477 && absY >= 3419 && absY <= 3420)
				|| (absX >= 2478 && absX <= 2482 && absY == 3420)
				|| (absX >= 2483 && absX <= 2484 && absY >= 3419 && absY <= 3420)
				|| (absX >= 2485 && absX <= 2488 && absY >= 3418 && absY <= 3421));
	}
	
	public static boolean inTopObstacle(int absX, int absY, int absZ){
		return (absX >= 2463 && absX <= 2500 && absY >= 3408 && absY <= 3431 && absZ == 2);
	}
	
	public void handleGnomeCourse(int object, int objectX, int objectY){
		if(System.currentTimeMillis() - c.lastAgil < 2000)
			return;
		if(object == 2286 && objectY > c.getY() && c.absX >= 2483 && c.absX <= 2488){ // net
			c.startAnimation(844);
			c.getPA().movePlayer(c.getX(), c.getY() + 2, 0);
			gnomeCourse[4] = true;
			c.lastAgil = System.currentTimeMillis();
			c.getPA().addSkillXP(EXP[5] * Config.AGILITY_EXPERIENCE, c.playerAgility);
		}else if((object == 154 || object == 4058) && c.absY <= 3430 && c.absY >= 3428 && c.absX >= 2483 && c.absX <= 2488){ // tube
			c.startAnimation(844);
			c.getPA().walkTo(0, 7);
			gnomeCourse[5] = true;
			if(isDoneGnome())
				giveReward(1);
			c.lastAgil = System.currentTimeMillis();
			c.getPA().addSkillXP(EXP[6] * Config.AGILITY_EXPERIENCE, c.playerAgility);

		}else if(object == 2295 && c.absX == 2474){
			c.playerSE = 0x328;// walk
			c.playerSEW = 762;// walk
			c.isRunning = false;
			if(objectX > c.getX())
				c.getPA().walkTo(1, 0);
			else if(objectX < c.getX())
				c.getPA().walkTo(-1, 0);
			c.lastAgil = System.currentTimeMillis();
			c.getPA().walkTo(0, -7);
			c.ignoreClipTick = 7;
			gnomeCourse[0] = true;
			c.lastAgil = System.currentTimeMillis();
			c.getPA().addSkillXP(EXP[0] * Config.AGILITY_EXPERIENCE, c.playerAgility);
		}else if(object == 2285 && c.heightLevel == 0 && c.absX >= 2471 && c.absX <= 2476){
			c.startAnimation(828);
			c.getPA().movePlayer(c.getX(), c.getY() - 2, 1);
			gnomeCourse[1] = true;
			c.lastAgil = System.currentTimeMillis();
			c.getPA().addSkillXP(EXP[1] * Config.AGILITY_EXPERIENCE, c.playerAgility);
		}else if(object == 2313 && c.heightLevel == 1){
			c.startAnimation(828);
			c.getPA().movePlayer(2473, 3420, 2);
			gnomeCourse[2] = true;
			c.lastAgil = System.currentTimeMillis();
			c.getPA().addSkillXP(EXP[2] * Config.AGILITY_EXPERIENCE, c.playerAgility);
		}else if(object == 2312 && c.absY == 3420){
			c.getPA().walkTo(6, 0);
			c.lastAgil = System.currentTimeMillis();
			c.getPA().addSkillXP(EXP[3] * Config.AGILITY_EXPERIENCE, c.playerAgility);
		}else if(object == 2314){
			c.getPA().movePlayer(c.getX(), c.getY(), 0);
			gnomeCourse[3] = true;
			c.lastAgil = System.currentTimeMillis();
			c.getPA().addSkillXP(EXP[4] * Config.AGILITY_EXPERIENCE, c.playerAgility);
		}
	}

	private void giveReward(int level){
		c.sendMessage("You have completed the course and have been given " + level + " tickets.");
		c.inventory.addItem(2996, level, -1);
		if(level == 1)
			c.getPA().addSkillXP(EXP[EXP.length - 1] * Config.AGILITY_EXPERIENCE, c.playerAgility);
		for(int j = 0; j < gnomeCourse.length; j++)
			gnomeCourse[j] = false;
	}

	private boolean isDoneGnome(){
		// return gnomeCourse[0] && gnomeCourse[1] && gnomeCourse[2] &&
		// gnomeCourse[3] && gnomeCourse[4] && gnomeCourse[5];
		return false;
	}
}