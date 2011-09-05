package de.redlion.qb;

import com.badlogic.gdx.utils.Array;

public class HighScoreManager {

	public Array<HighScore> highscores = new Array<HighScore>(true,Resources.getInstance().levelcount);
	public Array<HighScoreTimeAttack> timeAttackhighscores = new Array<HighScoreTimeAttack>(true,5);

	public static HighScoreManager instance;

	public static HighScoreManager getInstance() {
		if (instance == null) {
			instance = new HighScoreManager();
		}
		return instance;
	}
	
	private HighScoreManager() {
		for(int i=1; i<= Resources.getInstance().levelcount; ++i) {
			HighScore highScore = new HighScore();
			highScore.level = i;
			highScore.first = Resources.getInstance().prefs.getInteger("score_first_level_"+i);
			highScore.second = Resources.getInstance().prefs.getInteger("score_second_level_"+i);
			highScore.third = Resources.getInstance().prefs.getInteger("score_third_level_"+i);
			highscores.add(highScore);
		}
		
		for(int i=1; i<= 5; ++i) {
			HighScoreTimeAttack timeAttackHighscore = new HighScoreTimeAttack(0,0);
			timeAttackHighscore.usedTime = Resources.getInstance().prefs.getInteger("score_timeattack_time_"+i);
			timeAttackHighscore.level = Resources.getInstance().prefs.getInteger("score_timeattack_level_"+i);
			timeAttackhighscores.add(timeAttackHighscore);
		}
		
	}
	
	public void newHighScore(int score, int level) {
		for(HighScore highScore:highscores) {
			if(highScore.level==level) {
				// update score
				if(score<highScore.first || highScore.first == 0) {
					highScore.third = highScore.second;
					highScore.second = highScore.first;
					highScore.first = score;
				} else if(score<highScore.second  || highScore.second == 0) {
					highScore.third = highScore.second;
					highScore.second = score;
				} else if(score<highScore.third  || highScore.third == 0) {
					highScore.third = score;
				}				
				
				// write persistence
				Resources.getInstance().prefs.putInteger("score_first_level_"+level, highScore.first);
				Resources.getInstance().prefs.putInteger("score_second_level_"+level, highScore.second);
				Resources.getInstance().prefs.putInteger("score_third_level_"+level, highScore.third);
				Resources.getInstance().prefs.flush();
				
				break;
			}
		}
	}
	
	public HighScore getHighScore(int level) {
		for(HighScore highScore:highscores) {
			if(highScore.level==level) {
				return highScore;
			}
		}
		return null;
	}
	
	public void newTimeAttackHighScore(int score, int level) {
		timeAttackhighscores.add(new HighScoreTimeAttack(score,level));
		timeAttackhighscores.pop();
		
		int i = 1;
		for(HighScoreTimeAttack highScoreTimeAttack:timeAttackhighscores) {
			Resources.getInstance().prefs.putInteger("score_timeattack_time_"+i, highScoreTimeAttack.usedTime);
			Resources.getInstance().prefs.putInteger("score_timeattack_level_"+i, highScoreTimeAttack.level);
			++i;
		}
		Resources.getInstance().prefs.flush();
	}
	
	public String formatHighscore(int score) {
		int seconds = score % 60;
		int minutes = score / 60;
		
		String s = "";
		
		if(seconds > 9 && minutes > 9)
			s = minutes + ":" + seconds;
		else if(seconds > 9 && minutes < 10)
			s = "0" + minutes + ":" + seconds;
		else if(seconds < 10 && minutes > 9)
			s = minutes + ":0" + seconds;
		else
			s = "0" + minutes + ":0" + seconds;
		
		return s;
	}
	
}
