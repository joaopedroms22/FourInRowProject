package pt.isec.a21210827.fourinrow.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import pt.isec.a21210827.fourinrow.Class.Communication;
import pt.isec.a21210827.fourinrow.Class.Game;
import pt.isec.a21210827.fourinrow.Class.GameGridViewAdapter;
import pt.isec.a21210827.fourinrow.Class.Player;
import pt.isec.a21210827.fourinrow.Logic.GameEngine;
import pt.isec.a21210827.fourinrow.R;

public class GameActivity extends Activity implements Serializable{

    private GridView gvGame;
    private TextView tvGameMode, tvPlayerName, tvScore;
    private Chronometer gameChronometer;
    private Game gameInstance;

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

    }

    private void receiveIntents() {
        gameInstance = (Game) getIntent().getSerializableExtra("Game");

        //Get 0, pq é o modo 1 Jogador, logo o index vai ser sempre o 1º
        tvPlayerName.setText(gameInstance.getPlayers().get(0).getName());

        //Adicona o tipo de Jogo ao ecra
        tvGameMode.setText(gameInstance.getGameMode().toString());
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

        //Problema a serializar o chronometro
        outState.putSerializable("game", GameEngine.getInstance());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //GameEngine saved = (GameEngine) savedInstanceState.getSerializable("game");
        //GameEngine.changeGameInstance(saved);

    }
}
