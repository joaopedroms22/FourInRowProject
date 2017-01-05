package pt.isec.a21210827.fourinrow.Class;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;

import pt.isec.a21210827.fourinrow.Activity.GameActivity;
import pt.isec.a21210827.fourinrow.Activity.MainActivity;
import pt.isec.a21210827.fourinrow.Logic.GameEngine;
import pt.isec.a21210827.fourinrow.R;

/**
 * Created by joaop on 12/20/2016.
 */

public class Communication extends Application {

    //Game Class
    Game gameInstance;

    private static final int PORT = 8899;
    private static final int PORTaux = 9988;

    //View
    ProgressDialog pd = null;

    //Communication
    ServerSocket serverSocket = null;
    Socket socketGame = null;
    BufferedReader input;
    PrintWriter output;
    ObjectOutputStream outToServer;
    ObjectInputStream inFromServer;
    Handler procMsg = null;

    //Variável para controlar qual jogador é cliente ou servidor
    boolean canMove;

    @Override
    public void onCreate() {
        super.onCreate();

        procMsg = new Handler();
    }

    public void client(final String strIP, final int Port) {
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
                            //finish();
                        }
                    });
                    return;
                }
                commThreadClient.start();
            }
        });
        t.start();
    }

    Thread commThreadClient = new Thread(new Runnable() {

        @Override
        public void run() {
            try {

                input = new BufferedReader(new InputStreamReader(socketGame.getInputStream()));
                output = new PrintWriter(socketGame.getOutputStream());
                outToServer = new ObjectOutputStream(socketGame.getOutputStream());
                inFromServer = new ObjectInputStream(socketGame.getInputStream());

                //Envia o nome do jogador para o servidor, de modo a criar a instancia de jogo com os 2 jogadores
                output.println(gameInstance.getPlayers().get(0).getName());
                output.flush();

                //Recece a instancia já completa
                gameInstance = (Game) inFromServer.readObject();

                //Inicia o jogo nos 2 Devices simultaneamente
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                        intent.putExtra("Game", gameInstance);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });

                //GameEngine.getInstance().setActiveGame(gameInstance);

                while (!Thread.currentThread().isInterrupted()) {

                    procMsg.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "À espera das variaveis a atualizar mas já dentro do while:", Toast.LENGTH_SHORT).show();
                        }
                    });

                    final GameVariables gameUpdated = (GameVariables) inFromServer.readObject();

                    setCanMove(true);

                    procMsg.post(new Runnable() {
                        @Override
                        public void run() {
                            GameEngine.getInstance().setActiveGame(gameUpdated.getGame());
                            GameEngine.getInstance().setGameGrid(gameUpdated.getGameGrid());
                            GameEngine.getInstance().setList(gameUpdated.getList());
                            //Para verificar a condição de jogo quando reponho os valores

                            if(gameUpdated.getWinner() != null){
                                GameEngine.getInstance().winnerDialogBox("Ganhou o jogador: " + gameUpdated.getWinner());
                            }
                        }
                    });

                }
            } catch (Exception e) {
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        //finish();
                        Toast.makeText(getApplicationContext(), "Fim do Jogo", Toast.LENGTH_LONG).show();
                        socketGame = null;
                    }
                });
            }
        }
    });

    public void server(final ProgressDialog pd) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(PORT);
                    socketGame = serverSocket.accept();
                    serverSocket.close();
                    serverSocket = null;
                    commThreadServer.start();

                    procMsg.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "iniciei a thread Server", Toast.LENGTH_SHORT).show();
                            Log.w("MSG", "iniciei a thread Server");
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    socketGame = null;
                }
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {

                        if (socketGame == null) {
                        } //todo: fechar actividade
                        //finish();
                        pd.dismiss();
                    }
                });
            }
        });
        t.start();
    }

    Thread commThreadServer = new Thread(new Runnable() {
        @Override
        public void run() {
            try {

                input = new BufferedReader(new InputStreamReader(socketGame.getInputStream()));
                output = new PrintWriter(socketGame.getOutputStream());
                outToServer = new ObjectOutputStream(socketGame.getOutputStream());
                inFromServer = new ObjectInputStream(socketGame.getInputStream());

                //Recebe o nome do jogador para criar a instancia de jogo
                final String clientName = input.readLine();
                gameInstance.getPlayers().add(new Player(clientName));

                //Faz o random a ver quem começa
                /*Random random = new Random();
                int r = random.nextInt(2);
                gameInstance.getPlayers().get(r).setActivePlayer(true);*/

                //Envia a instancia para o cliente já completa
                outToServer.writeObject(gameInstance);
                outToServer.flush();

                //Inicia o jogo nos 2 Devices simultaneamente
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                        intent.putExtra("Game", gameInstance);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });


                while (!Thread.currentThread().isInterrupted()) {

                    procMsg.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "À espera das variaveis a atualizar mas já dentro do while:", Toast.LENGTH_SHORT).show();
                        }
                    });

                    final GameVariables gameUpdated = (GameVariables) inFromServer.readObject();

                    setCanMove(true);

                    procMsg.post(new Runnable() {
                        @Override
                        public void run() {
                            GameEngine.getInstance().setActiveGame(gameUpdated.getGame());
                            GameEngine.getInstance().setGameGrid(gameUpdated.getGameGrid());
                            GameEngine.getInstance().setList(gameUpdated.getList());

                            if(gameUpdated.getWinner() != null){
                                GameEngine.getInstance().winnerDialogBox("Ganhou o jogador: " + gameUpdated.getWinner());
                            }
                        }
                    });

                }
            } catch (Exception e) {
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Fim do Jogo + Exception e", Toast.LENGTH_LONG).show();
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

    public Game getGameInstance() {
        return gameInstance;
    }

    public void setGameInstance(Game gameInstance) {
        this.gameInstance = gameInstance;
    }

    public void sendObjecttoServer(GameVariables gameVariables){
        try {
            outToServer.writeObject(gameVariables);
            outToServer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isCanMove() {
        return canMove;
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

}
