package pt.isec.a21210827.fourinrow.Class;

import java.io.Serializable;

/**
 * Created by joaop on 11/5/2016.
 */

public class Player implements Serializable {
    private String name;
    private int score;
    private String colorPart;
    private int clientMode;
    private boolean activePlayer;

    public Player(){
        this.name = "IA -ROBERTO";
        this.score = 0;
        this.activePlayer = false;
    }

    public Player(String name) {
        this.name = name;
        this.score = 0;
        this.activePlayer = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getColorPart() {
        return colorPart;
    }

    public void setColorPart(String colorPart) {
        this.colorPart = colorPart;
    }

    public boolean isActivePlayer() {
        return activePlayer;
    }

    public void setActivePlayer(boolean activePlayer) {
        this.activePlayer = activePlayer;
    }

    public int getClientMode() {
        return clientMode;
    }

    public void setClientMode(int clientMode) {
        this.clientMode = clientMode;
    }
}
