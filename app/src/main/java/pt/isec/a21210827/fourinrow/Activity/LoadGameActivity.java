package pt.isec.a21210827.fourinrow.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import pt.isec.a21210827.fourinrow.Class.Game;
import pt.isec.a21210827.fourinrow.Logic.GameEngine;
import pt.isec.a21210827.fourinrow.R;

public class LoadGameActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_game);

        try {
            loadGame(GameEngine.LASTGAME);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void loadGame(String filename) throws IOException, ClassNotFoundException {


        File f = getFileStreamPath(filename);

        if (f.length() == 0) {
            // empty or doesn't exist
            Toast.makeText(this, "There is no active game!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // exists and is not empty
        }

        FileInputStream fileStream = new FileInputStream(f);
        ObjectInputStream objectStream = new ObjectInputStream(fileStream);

        //Lê os objectos
        int[] list = (int[]) objectStream.readObject();
        int[] gameGrid  = (int[]) objectStream.readObject();
        Game game = (Game) objectStream.readObject();

        //Coloca no intent a informação para o GameActivity
        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
        intent.putExtra("list", list);
        intent.putExtra("Game", game);
        intent.putExtra("gameGrid", gameGrid);
        intent.putExtra("flag",-1);
        startActivity(intent);

        //Mata a atividade
        finish();
    }


}
