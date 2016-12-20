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

    private static final int PORT = 8899;

    ProgressDialog pd = null;

    ServerSocket serverSocket = null;
    Socket socketGame = null;
    BufferedReader input;
    PrintWriter output;
    Handler procMsg = null;

    TextView tvHeader, tvPlayerName2, tvGridSize, tvColorPiece, tvMode;
    EditText etPlayerName, etPlayerName2;
    Button btStartGame;
    RadioGroup rgGridSize, rgColorPiece, rgMode;
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

                    setGameData(etPlayerName.getText().toString());
                    setGameData("BOT Roberto");

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

                    setGameData(etPlayerName.getText().toString());
                    setGameData(etPlayerName2.getText().toString());

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

                    setGameData(etPlayerName.getText().toString());

                    switch (getValueRadioGroup(rgMode)) {
                        case 0:
                            //client();
                            clientDlg();
                            break;
                        case 1:
                            //No caso de ser Server fica a espera que algum cliente de ligue a ele, e só depois entra no jogo!
                            server();
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

    private void setGameData(String playerName) {

        Player player = new Player(playerName);

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

    void server() {
        // WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        // String ip = Formatter.formatIpAddress(wm.getConnectionInfo()
        // .getIpAddress());
        String ip = getLocalIpAddress();
        pd = new ProgressDialog(this);
        pd.setMessage("Waiting for a client..." + "\n(IP: " + ip + ")"); //TODO: Colocar no ficheiro de strings
        pd.setTitle("Four in Row Server!");

        //setOnCancel é chamado sempre que é feito um back, ou o um toque fora da dialogue box
        pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                    }
                    serverSocket = null;
                }
            }
        });
        pd.show();

    }

    void clientDlg() {
        final EditText edtIP = new EditText(this);
        edtIP.setText("10.0.2.2");
        AlertDialog ad = new AlertDialog.Builder(this).setTitle("Four In Row Client")
                .setMessage("Server IP").setView(edtIP)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        client(edtIP.getText().toString(), PORT); // to test with emulators: PORTaux);
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                }).create();
        ad.show();
    }

    void client(final String strIP, final int Port) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("RPS", "Connecting to the server  " + strIP);
                    socketGame = new Socket(strIP, Port);
                } catch (Exception e) {
                    socketGame = null;
                }
                if (socketGame == null) {
                    procMsg.post(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                    return;
                }
                commThread.start();
            }
        });
        t.start();
    }

    Thread commThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                input = new BufferedReader(new InputStreamReader(socketGame.getInputStream()));
                output = new PrintWriter(socketGame.getOutputStream());
                while (!Thread.currentThread().isInterrupted()) {
                    String read = input.readLine();
                    //final int move = Integer.parseInt(read);
                    //Log.d("RPS", "Received: " + move);
                    procMsg.post(new Runnable() {
                        @Override
                        public void run() {
                            //moveOtherPlayer(move);
                        }
                    });
                }
            } catch (Exception e) {
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        Toast.makeText(getApplicationContext(), "Fim do Jogo", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    });

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}