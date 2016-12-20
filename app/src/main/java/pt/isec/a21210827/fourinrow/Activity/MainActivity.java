package pt.isec.a21210827.fourinrow.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import pt.isec.a21210827.fourinrow.R;

public class MainActivity extends Activity implements Button.OnClickListener{

    LinearLayout mainActivty;
    ImageButton ibPortuguese, ibEnglish;
    Button btOnePlayer, btTwoPlayerLocal, btTwoPlayerOnline, btLoadGame, btHighscore, btCredits;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        setFindViewbyId();
    }

    private void setFindViewbyId() {

        //View do Linear Layout Pai
        mainActivty = (LinearLayout) findViewById(R.id.activity_main);
        mainActivty.getBackground().setAlpha(50);

        //View do imageButton para traslate para Portugues
        ibPortuguese = (ImageButton) findViewById(R.id.ibPortuguese);

        //View do imageButton para traslate para Inglês
        ibEnglish = (ImageButton) findViewById(R.id.ibEnglish);

        ibEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Traduz para Inglês", Toast.LENGTH_SHORT).show();
            }
        });

        //View do Button para Novo Jogo - 1 Jogador
        btOnePlayer = (Button) findViewById(R.id.btOnePlayer);
        btOnePlayer.setOnClickListener(this);

        //View do Button para Novo Jogo - 2 Jogador Local
        btTwoPlayerLocal = (Button) findViewById(R.id.btTwoPlayerLocal);
        btTwoPlayerLocal.setOnClickListener(this);

        //View do Button para Novo Jogo - 2 Jogador Online
        btTwoPlayerOnline = (Button) findViewById(R.id.btTwoPlayerOnline);
        btTwoPlayerOnline.setOnClickListener(this);

        //View do Button para o Carregar Jogo
        btLoadGame = (Button) findViewById(R.id.btLoadGame);

        //View do Button para o Histórico de Jogos realizados
        btHighscore = (Button) findViewById(R.id.btHighscore);

        //View do Button para os Créditos do Jogo
        btCredits = (Button) findViewById(R.id.btCredits);
        btCredits.setOnClickListener(this);
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btOnePlayer:
                intent = new Intent(getApplicationContext(), GameSettingsActivity.class);
                intent.putExtra("mode", GameSettingsActivity.SINGLE_PLAYER);
                startActivity(intent);
                break;

            case R.id.btTwoPlayerLocal:
                intent = new Intent(getApplicationContext(), GameSettingsActivity.class);
                intent.putExtra("mode", GameSettingsActivity.MULTIPLAYER_LOCAL);
                startActivity(intent);
                break;

            case R.id.btTwoPlayerOnline:
                intent = new Intent(getApplicationContext(), GameSettingsActivity.class);
                intent.putExtra("mode", GameSettingsActivity.MULTIPLAYER_ONLINE);
                startActivity(intent);
                break;

            case R.id.btCredits:
                intent = new Intent(getApplicationContext(), CreditosActivity.class);
                startActivity(intent);
        }
    }
}
