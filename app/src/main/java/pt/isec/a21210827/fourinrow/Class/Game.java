package pt.isec.a21210827.fourinrow.Class;

import java.io.Serializable;
import java.util.ArrayList;


public class Game implements Serializable{
    private ArrayList<Player> players; //Array que contém 2 objectos de Player, onde contem as especificações de cada jogador
    private String gameMode; //Tipo de Jogo: 0 - SinglePlayer, 1 - MultiplayerLocal, 2 - MultiplayerTCP
    private int winner; //Nome do jogador vencedor
    private long gameTime; //Tempo que demorou a terminar um jogo
    private int size; //Tamanho do Board
    private int turns; //Turnos Jogados
    private boolean finished; //Jogo terminado

    public Game(){
        this.finished = false;
        this.players = new ArrayList<>();
        this.gameTime = 0;
        this.turns = 0;
        this.winner = -1;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getGameTime() {
        return gameTime;
    }

    public void setGameTime(long gameTime) {
        this.gameTime = gameTime;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public int getWinner() {
        return winner;
    }

    public void setWinner(int winner) {
        this.winner = winner;
    }

    public int getTurns() {
        return turns;
    }

    public void addTurn() {
        this.turns++;
    }

    public void setTurn(int t) {
        this.turns = t;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
