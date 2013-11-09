package de.redlion.qb;

public class GameInstance {

	public static GameInstance instance;

	public static GameInstance getInstance() {
		if (instance == null) {
			instance = new GameInstance();
		}
		return instance;
	}

	public void resetGame() {
	}
}