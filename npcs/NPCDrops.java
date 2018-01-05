package server.model.npcs;

import java.io.File;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Scanner;

/**
 * @author Sanity
 */

public class NPCDrops{

	public NPCDrops(){
		loadDrops();
	}

	public static HashMap<Integer, int[][]> normalDrops = new HashMap<Integer, int[][]>();
	public static HashMap<Integer, int[][]> rareDrops = new HashMap<Integer, int[][]>();
	public static HashMap<Integer, int[]> constantDrops = new HashMap<Integer, int[]>();
	public static HashMap<Integer, Integer> dropRarity = new HashMap<Integer, Integer>();

	public void loadDrops(){
		File f = new File("./Data/cfg/NPCDrops.TSM");
		try(Scanner s = new Scanner(f)){
			while(s.hasNextLine()){
				String line = s.nextLine();
				if(line.startsWith("#"))
					continue;
				StringTokenizer normalTok = new StringTokenizer(line, "\t");
				line = s.nextLine();
				if(line.startsWith("#"))
					continue;
				StringTokenizer rareTok = new StringTokenizer(line, "\t");
				String[] information = normalTok.nextToken().split(":");
				int npcId = Integer.parseInt(information[0]);
				dropRarity.put(npcId, Integer.parseInt(information[1]) - 1);
				int tempNorm[][] = new int[normalTok.countTokens()][2];
				int tempRare[][] = new int[rareTok.countTokens()][2];
				int count = 0;
				while(normalTok.hasMoreTokens()){
					String temp[] = normalTok.nextToken().split(":");
					tempNorm[count][0] = Integer.parseInt(temp[0]);
					tempNorm[count][1] = Integer.parseInt(temp[1]);
					count++;
				}
				count = 0;
				while(rareTok.hasMoreTokens()){
					String temp[] = rareTok.nextToken().split(":");
					tempRare[count][0] = Integer.parseInt(temp[0]);
					tempRare[count][1] = Integer.parseInt(temp[1]);
					count++;
				}
				normalDrops.put(npcId, tempNorm);
				rareDrops.put(npcId, tempRare);
			}
			loadConstants();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void loadConstants(){
		File f = new File("./Data/cfg/NpcConstants.TSM");
		try(Scanner s = new Scanner(f)){
			while(s.hasNextLine()){
				String line = s.nextLine();
				if(line.startsWith("#"))
					continue;
				StringTokenizer constantTok = new StringTokenizer(line, "\t");
				int npcId = Integer.parseInt(constantTok.nextToken());
				int count = 0;
				int[] temp = new int[constantTok.countTokens()];
				while(constantTok.hasMoreTokens()){
					temp[count] = Integer.parseInt(constantTok.nextToken());
					count++;
				}
				constantDrops.put(npcId, temp);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}