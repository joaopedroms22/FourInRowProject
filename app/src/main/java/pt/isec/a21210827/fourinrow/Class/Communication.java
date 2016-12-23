package pt.isec.a21210827.fourinrow.Class;

import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

/**
 * Created by joaop on 12/20/2016.
 */

public class Communication extends Application {

    Game gameInstance;

    private static final int PORT = 8899;

    ProgressDialog pd = null;

    ServerSocket serverSocket = null;
    Socket socketGame = null;
    BufferedReader input;
    PrintWriter output;
    Handler procMsg = null;

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
                commThread.start();
            }
        });
        t.start();
    }

    public void server(final ProgressDialog pd) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(PORT);
                    socketGame = serverSocket.accept();
                    serverSocket.close();
                    serverSocket = null;
                    commThread.start();
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
                            Toast.makeText(getApplicationContext(),"Lancei o handler",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        //finish();
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

    public Game getGameInstance() {
        return gameInstance;
    }

    public void setGameInstance(Game gameInstance) {
        this.gameInstance = gameInstance;
    }
}
