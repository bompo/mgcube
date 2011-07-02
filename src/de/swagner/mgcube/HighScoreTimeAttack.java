package de.swagner.mgcube;

public class HighScoreTimeAttack implements Comparable<HighScoreTimeAttack> {
	
	public int usedTime = 0;
	public int level = 0;
	
	public HighScoreTimeAttack(int usedTime, int level) {
		this.usedTime = usedTime;
		this.level = level;
	}
	
	@Override
	public int compareTo(HighScoreTimeAttack o) {
		if(o.level>this.level) return -1;
		if(o.usedTime<this.level) return -1;
		return 1;
	}
	
}
