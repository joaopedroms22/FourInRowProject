package pt.isec.a21210827.fourinrow.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import pt.isec.a21210827.fourinrow.Class.Communication;
import pt.isec.a21210827.fourinrow.Class.Game;
import pt.isec.a21210827.fourinrow.Class.Player;
import pt.isec.a21210827.fourinrow.R;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;

public class GameSettingsActivity extends Activity {

    public static int SINGLE_PLAYER = 0;
    public static int MULTIPLAYER_LOCAL = 1;
    public static int MULTIPLAYER_ONLINE = 2;

    final public static String S_SINGLE_PLAYER = "Modo - Um Jogador";
    final public static String S_MULTIPLAYER_LOCAL = "Modo - Dois Jogadores (LOCAL)";
    final public static String S_MULTIPLAYER_ONLINE = "Modo - Dois Jogadores (ONLINE)";

    TextView tvHeader, tvPlayerName2, tvGridSize, tvColorPiece, tvMode;
    EditText etPlayerName, etPlayerName2;
    Button btStartGame;
    RadioGroup rgGridSize, rgColorPiece, rgMode;

    private static final int PORT = 8899;
    private static final int PORTaux = 9988;
    Communication com;
    ServerSocket serverSocket = null;

    Intent intent;
    int mode;
    Game gameInstance = new Game();
    String permissions[] = {ACCESS_NETWORK_STATE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_one_player);

        //Recebe por intent o modo escolhido na main Activity
        mode = getIntent().getIntExtra("mode", 0);

        setFindViewbyId();

        if (mode == MULTIPLAYER_ONLINE) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, 0);
            }

            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnected()) {
                Toast.makeText(this, "No Network connection", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            rgMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    RadioButton rb = (RadioButton) radioGroup.findViewById(i);

                    if (rb.getText().equals("Cliente")) {
                        tvGridSize.setVisibility(View.GONE);
                        rgGridSize.setVisibility(View.GONE);

                        tvColorPiece.setVisibility(View.GONE);
                        rgColorPiece.setVisibility(View.GONE);
                    } else {
                        tvGridSize.setVisibility(View.VISIBLE);
                        rgGridSize.setVisibility(View.VISIBLE);

                        tvColorPiece.setVisibility(View.VISIBLE);
                        rgColorPiece.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        verifyGameMode();
    }

    private void verifyGameMode() {

        if (mode == SINGLE_PLAYER) {

            tvHeader.setText(S_SINGLE_PLAYER);

            etPlayerName2.setVisibility(View.GONE);
            tvPlayerName2.setVisibility(View.GONE);

            tvMode.setVisibility(View.GONE);
            rgMode.setVisibility(View.GONE);

            btStartGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (etPlayerName.getText().length() == 0) {
                        Toast.makeText(getApplicationContext(), "Insira o seu nome para continuar!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    setPlayerData(etPlayerName.getText().toString());
                    setPlayerData("BOT Roberto");

                    setGameData();

                    gameInstance.setGameMode(S_SINGLE_PLAYER);

                    Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                    intent.putExtra("Game", gameInstance);
                    startActivity(intent);
                    finish();
                }
            });
        }

        if (mode == MULTIPLAYER_LOCAL) {

            tvHeader.setText(S_MULTIPLAYER_LOCAL);

            tvMode.setVisibility(View.GONE);
            rgMode.setVisibility(View.GONE);

            btStartGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (etPlayerName.getText().length() == 0 || etPlayerName2.getText().length() == 0) {
                        Toast.makeText(getApplicationContext(), "Preencha o nome dos Jogadores, para continuar!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //Coloca a informação dos jogadores na gameInstance
                    setPlayerData(etPlayerName.getText().toString());
                    setPlayerData(etPlayerName2.getText().toString());

                    //Coloca a informação referente ao jogo na gameInstance
                    setGameData();

                    gameInstance.setGameMode(S_MULTIPLAYER_LOCAL);

                    Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                    intent.putExtra("Game", gameInstance);
                    startActivity(intent);
                    finish();
                }
            });

            //TODO: *** MULTIPLAYER ONLINE HERE! ***
        }

        if (mode == MULTIPLAYER_ONLINE) {

            tvHeader.setText(S_MULTIPLAYER_ONLINE);

            etPlayerName2.setVisibility(View.GONE);
            tvPlayerName2.setVisibility(View.GONE);

            btStartGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (etPlayerName.getText().length() == 0) {
                        Toast.makeText(getApplicationContext(), "Insira o seu nome para continuar!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    com = (Communication) getApplication();

                    //Coloca a informação referente ao tabuleiro na gameInstance
                    setGameData();

                    //Coloca o tipo de jogo em Multiplayer_Online
                    gameInstance.setGameMode(S_MULTIPLAYER_ONLINE);

                    //Copia a instancia criada anteriormente para dentro da class comunicação, para a mesma instacia do jogo ser partilhada pelos 2 players
                    com.setGameInstance(gameInstance);

                    switch (getValueRadioGroup(rgMode)) {
                        case 0:
                            //client();
                            com.getGameInstance().getPlayers().add(new Player(etPlayerName.getText().toString()));
                            clientDlg();

                            /*intent = new Intent(getApplicationContext(), GameActivity.class);
                            intent.putExtra("Game", com.getGameInstance());
                            startActivity(intent);
                            finish();*/

                            break;

                        case 1:
                            //server();
                            com.getGameInstance().getPlayers().add(new Player(etPlayerName.getText().toString()));
                            serverDlg();

                            /*intent = new Intent(getApplicationContext(), GameActivity.class);
                            intent.putExtra("Game", com.getGameInstance());
                            startActivity(intent);
                            finish();*/

                            break;

                    }
                }
            });
        }
    }

    private void setFindViewbyId() {

        //View do Nome do Jogo, colocado no header da activity
        tvHeader = (TextView) findViewById(R.id.tvHeader);

        //View da Edit Text que contém o nome do Jogador
        etPlayerName = (EditText) findViewById(R.id.etPlayerName);

        //View da textView do segundo jogador
        tvPlayerName2 = (TextView) findViewById(R.id.tvPlayerName2);

        //View da Edit Text que contém o nome do Jogador
        etPlayerName2 = (EditText) findViewById(R.id.etPlayerName2);

        //View do Button que vai dar incio á partida
        btStartGame = (Button) findViewById(R.id.btStartGame);

        //View do TextView do tamanho do tabuleiro
        tvGridSize = (TextView) findViewById(R.id.tvGridSize);

        //View do RadioGroup do tamanho do tabuleiro
        rgGridSize = (RadioGroup) findViewById(R.id.rgGridSize);

        //View do TextView que representa a escolha da cor das peças
        tvColorPiece = (TextView) findViewById(R.id.tvColorPiece);

        //View do RadioGroup da cor das peças
        rgColorPiece = (RadioGroup) findViewById(R.id.rgColorPiece);

        //View do TextView do Modo Online
        tvMode = (TextView) findViewById(R.id.tvMode);

        //View do RadioGroup do modo de Jogador(Cliente/Servidor)
        rgMode = (RadioGroup) findViewById(R.id.rgMode);

    }

    private int getValueRadioGroup(RadioGroup rg) {
        int radioButtonID = rg.getCheckedRadioButtonId();
        View radioButton = rg.findViewById(radioButtonID);
        int idx = rg.indexOfChild(radioButton);

        return idx;
    }

    private void setPlayerData(String playerName){
        Player player = new Player(playerName);

        switch (getValueRadioGroup(rgColorPiece)) { //TODO: ARRANJAR FORMA DE CONTORNAR ESTE PROBLEMA DA COR DAS PEÇAS QUE ESCOLEHEM QUANDO ESTÃO 2 JOGADORES!
            case 0:
                player.setColorPart("RED");
                break;
            case 1:
                player.setColorPart("YELLOW");
                break;
        }

        gameInstance.getPlayers().add(player);
    }

    private void setGameData() {

        switch (getValueRadioGroup(rgGridSize)) {
            case 0:
                gameInstance.setSize(7);
                break;
            case 1:
                gameInstance.setSize(8);
                break;
            case 2:
                gameInstance.setSize(9);
                break;
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
