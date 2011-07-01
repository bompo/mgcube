package de.swagner.mgcube;

import com.badlogic.gdx.utils.Array;

public class HighScoreManager {

	Array<HighScore> highscores = new Array<HighScore>();

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
	
}
