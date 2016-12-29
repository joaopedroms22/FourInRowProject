package pt.isec.a21210827.fourinrow.Activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import pt.isec.a21210827.fourinrow.Class.Communication;
import pt.isec.a21210827.fourinrow.Class.Game;
import pt.isec.a21210827.fourinrow.Logic.GameEngine;
import pt.isec.a21210827.fourinrow.R;

public class GameActivity extends Activity {

    private GridView gvGame;
    private TextView tvGameMode, tvPlayerName, tvScore;
    private Chronometer gameChronometer;
    private Game gameInstance;

    int[] list, gameGrid;
    int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_game);

        //Processa todas as view da actividade
        setFindViewbyId();

        //Recebe os intents passados das outras activity
        receiveIntents();

        Communication com = (Communication) getApplication();

        //Desenha o Tabuleiro
        GameEngine.getInstance().startGame(this, gvGame, gameInstance, gameChronometer, tvPlayerName, tvScore);

        //Se for -1 é porque esta atividade nasceu de um loadGame
        if (flag == -1) {
            GameEngine.getInstance().setList(list);
            GameEngine.getInstance().setGameGrid(gameGrid, gameInstance.getSize());
        }

    }

    private void receiveIntents() {

        gameInstance = (Game) getIntent().getSerializableExtra("Game");

        //Get 0, pq é o modo 1 Jogador, logo o index vai ser sempre o 1º
        tvPlayerName.setText(gameInstance.getPlayers().get(0).getName());

        //Adicona o tipo de Jogo ao ecra
        tvGameMode.setText(gameInstance.getGameMode());

        if (getIntent().getIntExtra("flag", 0) == -1) {
            flag = getIntent().getIntExtra("flag", 0);
            list = getIntent().getIntArrayExtra("list");
            gameGrid = getIntent().getIntArrayExtra("gameGrid");
        }
    }

    private void setFindViewbyId() {

        //View referente à GridView do Jogo
        gvGame = (GridView) findViewById(R.id.gvGame);

        //VIew referente ao tipo de jogo
        tvGameMode = (TextView) findViewById(R.id.tvGameMode);

        //View da TextView - Modo de Jogo
        tvPlayerName = (TextView) findViewById(R.id.tvPlayerName);

        //View da TextView - Score
        tvScore = (TextView) findViewById(R.id.tvScore);

        //View do cronometro
        gameChronometer = (Chronometer) findViewById(R.id.gameChronometer);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Todo: guardar o jogador que está ativo de momento

        //Guarda a lista que contem os drawables, da gridView
        outState.putIntArray("list", GameEngine.getInstance().getList());

        //Guarda o arrayList[][] que contem onde as peças foram colocadas!
        outState.putIntArray("gameGrid", GameEngine.getInstance().getGameGrid());

        outState.putSerializable("game", GameEngine.getInstance().getActiveGame());

        //Guarda o tamanho do do tabuleiro
        outState.putInt("size", gameInstance.getSize());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //Aqui restora a Grid com a informação referente ao jogo
        int[] gameGrid = savedInstanceState.getIntArray("gameGrid");
        int size = savedInstanceState.getInt("size");
        GameEngine.getInstance().setGameGrid(gameGrid, size);

        //Aqui guarda a informação referente ao desenho presente na gridView
        int[] saved = savedInstanceState.getIntArray("list");
        GameEngine.getInstance().setList(saved);

        //Restora a instancia de jogo, onde contem o toda a informação relativa ao jogo
        Game game = (Game) savedInstanceState.getSerializable("game");
        GameEngine.getInstance().setActiveGame(game);
    }

    @Override
    protected void onStop() {
        super.onStop();

        Game game = GameEngine.getInstance().getActiveGame();

        if (!game.isFinished()) {

            String filename = "lastGame";

            try {
                FileOutputStream fileStream = openFileOutput(filename, Context.MODE_PRIVATE);
                ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);

                objectStream.writeObject(GameEngine.getInstance().getList());
                objectStream.writeObject(GameEngine.getInstance().getGameGrid());
                objectStream.writeObject(GameEngine.getInstance().getActiveGame());

                objectStream.close();
                fileStream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
