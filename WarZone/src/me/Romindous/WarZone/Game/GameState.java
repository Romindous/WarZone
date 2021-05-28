package me.Romindous.WarZone.Game;

public enum GameState {
	
	LOBBY_WAIT("LOBBY_WAIT"), 
	RUNNING("RUNNING"), 
	END("END");
	
	public String state;
	
	private GameState(String state) {
		this.state = state;
	}
}
