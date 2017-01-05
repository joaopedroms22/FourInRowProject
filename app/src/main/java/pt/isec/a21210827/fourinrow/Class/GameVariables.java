package pt.isec.a21210827.fourinrow.Class;

import java.io.Serializable;

/**
 * Created by joaop on 1/4/2017.
 */

public class GameVariables implements Serializable{
    private int[][] gameGrid;
    private int[] list;
    private Game game;

    public GameVariables() {
        this.gameGrid = null;
        this.list = null;
    }

    public int[][] getGameGrid() {
        return gameGrid;
    }

    public void setGameGrid(int[][] gameGrid) {
        this.gameGrid = gameGrid;
    }

    public int[] getList() {
        return list;
    }

    public void setList(int[] list) {
        this.list = list;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
