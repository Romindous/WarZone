package ru.romindous.wz.Game;

public enum GameState {
	
	WAITING("LOBBY_WAIT"),
	RUNNING("RUNNING"), 
	END("END");
	
	public final String state;
	
	GameState(String state) {
		this.state = state;
	}
}
