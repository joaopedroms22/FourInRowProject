package pt.isec.a21210827.fourinrow.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Window;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;

import pt.isec.a21210827.fourinrow.Class.Communication;
import pt.isec.a21210827.fourinrow.Class.Game;
import pt.isec.a21210827.fourinrow.Class.Player;
import pt.isec.a21210827.fourinrow.Logic.GameEngine;
import pt.isec.a21210827.fourinrow.R;

public class GameActivity extends Activity {

    private GridView gvGame;
    private TextView tvGameMode, tvPlayerName, tvScore;
    private Chronometer gameChronometer;
    private Game gameInstance = null;

    private int[] list, gameGrid;
    private int flag = 0;

    private static final int PORT = 8899;
    private static final int PORTaux = 9988;
    Communication com;
    ServerSocket serverSocket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_game);

        com = (Communication) getApplication();

        //Processa todas as view da actividade
        setFindViewbyId();

        //Recebe os intents passados das outras activity
        receiveIntents();

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

        if(gameInstance == null){

            gameInstance = com.getGameInstance();

            Player client = (Player) getIntent().getSerializableExtra("Client");
            if(client != null){
                gameInstance.getPlayers().add(client);
                clientDlg();
            }

            Player server = (Player) getIntent().getSerializableExtra("Server");
            if(server != null){
                gameInstance.getPlayers().add(server);
                serverDlg();
            }
        }


        //Get 0, pq é o modo 1 Jogador, logo o index vai ser sempre o 1º
        //tvPlayerName.setText(gameInstance.getPlayers().get(0).getName());

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

    public void clientDlg() {

        final EditText edtIP = new EditText(this);
        edtIP.setText("192.168.1.117");
        AlertDialog ad = new AlertDialog.Builder(this).setTitle("Four In Row Client")
                .setMessage("Server IP").setView(edtIP)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        com.client(edtIP.getText().toString(), PORT); // to test with emulators: PORTaux);
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        //finish();
                    }
                }).create();
        ad.show();
    }

    public void serverDlg() {

        ProgressDialog pd;

        String ip = com.getLocalIpAddress();
        pd = new ProgressDialog(this);
        pd.setMessage("Waiting for a client..." + "\n(IP: " + ip + ")"); //TODO: Colocar no ficheiro de strings
        pd.setTitle("Four in Row Server!");

        //setOnCancel é chamado sempre que é feito um back, ou o um toque fora da dialogue box
        pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });

        pd.show();

        com.server(pd);
    }

}
