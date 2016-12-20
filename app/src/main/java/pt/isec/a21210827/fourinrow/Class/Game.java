package pt.isec.a21210827.fourinrow.Class;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by joaop on 11/5/2016.
 */

public class Game implements Serializable{
    ArrayList<Player> players; //Array que contém 2 objectos de Player, onde contem as especificações de cada jogador
    String gameMode; //Tipo de Jogo: 0 - SinglePlayer, 1 - MultiplayerLocal, 2 - MultiplayerTCP
    String winner; //Nome do jogador vencedor
    long gameTime; //Tempo que demorou a terminar um jogo
    int size; //Tamanho do Board


    public Game(){
        players = new ArrayList<Player>();
        gameTime = 0;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
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
}
