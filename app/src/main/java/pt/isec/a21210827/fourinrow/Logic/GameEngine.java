package pt.isec.a21210827.fourinrow.Logic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import pt.isec.a21210827.fourinrow.Activity.MainActivity;
import pt.isec.a21210827.fourinrow.Class.Game;
import pt.isec.a21210827.fourinrow.Class.GameGridViewAdapter;
import pt.isec.a21210827.fourinrow.R;
import pt.isec.a21210827.fourinrow.Activity.GameSettingsActivity;

public class GameEngine {

    private int[][] gameGrid;
    private int[] list;
    private int posx, posy, posyFinal, activePlayer, size, turns = 0;

    private static GameEngine instance;
    private Context contextApplication;
    private Game game;
    private GridView gridViewGame;
    private Chronometer mChronometer;
    private TextView tvPlayerName, tvScore;

    public static GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }
        return instance;
    }

    public void startGame(final Context context, final GridView gvGame, final Game gameInstance, final Chronometer mChrono, final TextView tvPlayer, final TextView score) { //TODO: Modificar este cronometro para dentro do gameInstance

        gridViewGame = gvGame;
        game = gameInstance;
        mChronometer = mChrono;
        tvPlayerName = tvPlayer;
        tvScore = score;
        size = gameInstance.getSize();

        gameGrid = new int[size][size];
        list = new int[size * size];
        contextApplication = context;

        //INICIALIZA A GAME GRID TODA A -1
        for (int i = 0; i < gameGrid.length; i++) {
            for (int j = 0; j < gameGrid[i].length; j++) {
                gameGrid[i][j] = -1;
            }
        }

        for (int i = 0; i < list.length; i++) {
            list[i] = R.drawable.circle_shape;
        }

        //Cria cria a grid o numero de colunas especificados no size
        gvGame.setNumColumns(size);
        gvGame.setAdapter(new GameGridViewAdapter(list, context));

        //Dá inicio á contagem do cronometro
        mChronometer.start();

        //Inicia os Scores!
        tvScore.setText(game.getPlayers().get(0).getName() + ": " + game.getPlayers().get(0).getScore() + "   &&   " + game.getPlayers().get(1).getName() + ": " + game.getPlayers().get(1).getScore());

        //Sortei um valor entre '0' ou '1', para descobrir que jogador começa o jogo primeiro e coloca na textView
        Random random = new Random();
        int r = random.nextInt(2);
        game.getPlayers().get(r).setActivePlayer(true);
        tvPlayerName.setText(game.getPlayers().get(r).getName());

        //Verifica se o primeiro a jogar é o BOT Roberto, se for faz a jogada dele e troca de jogador!
        checkIfBotIsFirst();

        gvGame.setOnItemClickListener(new GridView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int pos, long l) {

                posx = (pos % size);
                posy = (pos / size);

                whoIsActive();

                if (putPiece(gameGrid, posx)) {
                    if (game.getPlayers().get(0).isActivePlayer()) {
                        list[(size * posyFinal) + posx] = R.drawable.circle_shape_red;
                    } else {
                        list[(size * posyFinal) + posx] = R.drawable.circle_shape_yellow;
                    }
                    gvGame.setAdapter(new GameGridViewAdapter(list, context));
                }

                if (checkEndGame(posx, posyFinal)) {//todo: fazer esta verificação só à 4 iteração!
                    saveScore();
                    saveElapsedTime(mChronometer);
                    winnerDialogBox(context, "Ganhou o Jogador: " + game.getPlayers().get(activePlayer).getName().toString());
                } else {
                    switchPlayer();
                }
            }
        });
    }

    private void saveScore() {
        int score = game.getPlayers().get(activePlayer).getScore();

        //Incrementa o Score dos jogadores sempre que vencerem um jogo!
        game.getPlayers().get(activePlayer).setScore(score + 1);

        tvScore.setText(game.getPlayers().get(0).getName() + ": " + game.getPlayers().get(0).getScore() + "   &&   " + game.getPlayers().get(1).getName() + ": " + game.getPlayers().get(1).getScore());
    }

    private void winnerDialogBox(final Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle("Fim do Jogo")
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);  //TODO: DAR FINISH A ESTA ACTIVITY!
                    }
                })
                .setNegativeButton("Restart", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        restartGame();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void switchPlayer() {
        switch (game.getGameMode()) {

            case GameSettingsActivity.S_SINGLE_PLAYER:
                if (game.getPlayers().get(0).isActivePlayer()) {
                    tvPlayerName.setText(game.getPlayers().get(1).getName());
                    game.getPlayers().get(0).setActivePlayer(false);
                    botMove();
                } else {
                    tvPlayerName.setText(game.getPlayers().get(0).getName());
                    game.getPlayers().get(0).setActivePlayer(true);
                }

            case GameSettingsActivity.S_MULTIPLAYER_LOCAL:
                if (game.getPlayers().get(0).isActivePlayer()) {
                    tvPlayerName.setText(game.getPlayers().get(1).getName());
                    game.getPlayers().get(0).setActivePlayer(false);
                } else {
                    tvPlayerName.setText(game.getPlayers().get(0).getName());
                    game.getPlayers().get(0).setActivePlayer(true);
                }
                break;

            case GameSettingsActivity.S_MULTIPLAYER_ONLINE:
                break;
        }
    }

    private boolean checkEndGame(int posx, int posy) {
        if (checkDiagonalRight(posx, posy) || checkDiagonalLeft(posx, posy) || checkHorizontal(posx, posy) || checkVertical(posx, posy))
            return true;

        return false;
    }

    private boolean putPiece(int[][] gameGrid, int posx) {

        //Se o tabuleiro já se encontrar preenchido acaba o jogo, com um empate!
        if (turns == (size * size)) {
            winnerDialogBox(contextApplication, "Empate!");
            return false;
        }

        //Vai verificar todos os y, daquele x, o ultimo que tenha o valor -1, para poder adicionar o valor.
        for (int i = 0; i < gameGrid.length; i++) {

            //Se o array já esitver cheio não deixa inserir mais nenhum valor.
            if (gameGrid[posx][0] != -1) {
                Toast.makeText(contextApplication, "Move Not Allowed", Toast.LENGTH_SHORT).show();
                return false;
            }
            // Verifica se a posição onde está se encontra vazia, se sim continua a procura até encotrar uma que não esteja.
            if (gameGrid[posx][i] == -1) {
                continue;
            }
            //Quando encontrar uma que tenha um valor, diferente de -1, coloca lá um valor novo e guarda a posição do Y, onde foi inserida
            else {
                gameGrid[posx][i - 1] = activePlayer;
                posyFinal = i - 1;
                turns++;
                return true;
            }
        }
        //Se percorrer o array inteiro e não encontrar nenhum valor inserido, estamos perante um array vazio, logo vamos adicionar a peça na ultima posição do array.
        gameGrid[posx][gameGrid.length - 1] = activePlayer; // todo: Modificar o "0", para a valor respectivo de cada jogador.
        posyFinal = gameGrid.length - 1;
        turns++;
        return true;
    }

    private boolean checkVertical(int posx, int posy) {
        int pieceCounter = 1;

        //Se a posição Y, onde foi inserida a peça for o fundo do tabuleiro, é pq não existe ainda fim de jogo, pois não existem peças abaixo dela para o fim se verificar!
        if (posy == gameGrid.length - 1) return false;

        for (int i = 0; i < 3; i++) {

            //Se a posição Y, onde foi inserida a peça + 1 posição a baixo for maior que o bottom do tabuleiro, return false, pq senão passamos as margens do array!
            if (posy + pieceCounter > gameGrid.length - 1) return false;

            //Se a peça inserida na posição Y, for igual ao valor do active player, incrementa o valor do pieceCounter e torna a verificar se a peça embaixo ainda é igual ao valor do active player. Quando não for e ainda se encontrar dentro do ciclo for, é pq a condição de fim de jogo verticalmete não se verifica!
            if (gameGrid[posx][posy + pieceCounter] == activePlayer) { // TODO: 11/28/2016 - ALTERAR O PARA O VALOR DO JOGADOR ACTIVO
                pieceCounter++;
            } else {
                return false;
            }

            if (pieceCounter == 4) {
                Toast.makeText(contextApplication, "Game Ended Vertically", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    private boolean checkHorizontal(int posx, int posy) {
        int pieceCounter = 1, lastRight = 1;

        if (posx + lastRight > gameGrid.length - 1) lastRight = 0;

        while (gameGrid[posx + lastRight][posy] == activePlayer) {
            lastRight++;
            if (posx + lastRight > gameGrid.length - 1)
                break;
        }

        for (int i = 0; i < 4; i++) {
            if ((posx + lastRight) - pieceCounter < 0) return false;

            if (gameGrid[(posx + lastRight) - pieceCounter][posy] == activePlayer) { // TODO: 11/28/2016 - ALTERAR O PARA O VALOR DO JOGADOR ACTIVO
                pieceCounter++;
            } else {
                return false;
            }

            if (pieceCounter == 5) {
                Toast.makeText(contextApplication, "Game Ended Horizontal", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    private boolean checkDiagonalRight(int posx, int posy) {
        int pieceCounter = 1, lastRight = 1;

        if (posx + lastRight > gameGrid.length - 1 || posy - lastRight < 0) lastRight = 0;

        while (gameGrid[posx + lastRight][posy - lastRight] == activePlayer) { // TODO: 11/28/2016 - ALTERAR '0' PARA O VALOR DO JOGADOR ACTIVO
            lastRight++;
            if (posx + lastRight > gameGrid.length - 1 || posy - lastRight < 0)
                break;
        }

        for (int i = 0; i < 4; i++) {

            if ((posx + lastRight) - pieceCounter < 0 || (posy - lastRight) + pieceCounter > gameGrid.length - 1)
                return false; // VERIFICA LIMITES DO ARRAY

            if (gameGrid[(posx + lastRight) - pieceCounter][(posy - lastRight) + pieceCounter] == activePlayer) { // TODO: 11/28/2016 - ALTERAR O '0' PARA O VALOR DO JOGADOR ACTIVO
                pieceCounter++;
            } else {
                return false;
            }

            if (pieceCounter == 5) {
                Toast.makeText(contextApplication, "Game Ended Diagonal Right", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    private boolean checkDiagonalLeft(int posx, int posy) {
        int pieceCounter = 1, lastLeft = 1;

        if (posx - lastLeft < 0 || posy - lastLeft < 0) lastLeft = 0;

        while (gameGrid[posx - lastLeft][posy - lastLeft] == activePlayer) { // TODO: 11/28/2016 - ALTERAR '0' PARA O VALOR DO JOGADOR ACTIVO
            lastLeft++;
            if (posx - lastLeft < 0 || posy - lastLeft < 0)
                break;
        }

        for (int i = 0; i < 4; i++) {

            if ((posx - lastLeft) + pieceCounter < 0 || (posx - lastLeft) + pieceCounter > gameGrid.length - 1 || (posy - lastLeft) + pieceCounter > gameGrid.length - 1)
                return false; //TODO: (posx - lastLeft) + pieceCounter < 0 - verify if this is needed

            if (gameGrid[(posx - lastLeft) + pieceCounter][(posy - lastLeft) + pieceCounter] == activePlayer) {
                pieceCounter++;
            } else {
                return false;
            }

            if (pieceCounter == 5) {
                Toast.makeText(contextApplication, "Game Ended Diagonal Left", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    private void saveElapsedTime(Chronometer mChronometer) {
        mChronometer.stop();
        game.setGameTime(SystemClock.elapsedRealtime() - mChronometer.getBase());
    }

    private void restartGame() {

        turns = 0;

        for (int i = 0; i < gameGrid.length; i++) {
            for (int j = 0; j < gameGrid[i].length; j++) {
                gameGrid[i][j] = -1;
            }
        }

        for (int i = 0; i < list.length; i++) {
            list[i] = R.drawable.circle_shape;
        }

        //Reinicia o chronometro
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();

        //Reinicia o activePlayer = false
        game.getPlayers().get(0).setActivePlayer(false);
        game.getPlayers().get(1).setActivePlayer(false);

        //Sortei um valor entre '0' ou '1', para descobrir que jogador começa o jogo primeiro
        Random random = new Random();
        int r = random.nextInt(2);
        game.getPlayers().get(r).setActivePlayer(true);
        tvPlayerName.setText(game.getPlayers().get(r).getName());

        //Se o modo de jogo for Single Player e for o Bot Roberto que esteja activo como o 1º a jogar, então vai fazer a jogada do bot!
        checkIfBotIsFirst();

        gridViewGame.setAdapter(new GameGridViewAdapter(list, contextApplication));
    }

    private void botMove() {

        //Make a random move on Board
        Random r = new Random();
        int posx = r.nextInt(game.getSize() - 1);

        //int posx = chooseMove();

        //Verica qual é o jogador que está activo e coloca na textView o nome do mesmo, e dá set no value do player a começar o jogo!
        whoIsActive();

        if (putPiece(gameGrid, posx)) {
            if (game.getPlayers().get(0).isActivePlayer()) {
                list[(size * posyFinal) + posx] = R.drawable.circle_shape_red;
            } else {
                list[(size * posyFinal) + posx] = R.drawable.circle_shape_yellow;
            }
            gridViewGame.setAdapter(new GameGridViewAdapter(list, contextApplication));
        }

        if (checkEndGame(posx, posyFinal)) { //todo: fazer esta verificação só à 4 iteração!
            saveScore();
            saveElapsedTime(mChronometer);
            winnerDialogBox(contextApplication, "Ganhou o Jogador: " + game.getPlayers().get(activePlayer).getName().toString());
        }
    }

    private int chooseMove() {

        //Vai percorrer a a tabela toda á procura de uma jogada realizada do adversário!
        for (int i = 0; i < (size - 1); i++) {
            for (int j = 0; j < (size - 1); j++) {
                if(verifyBotVertical(i, j) != 0){
                    return verifyBotVertical(i,j);
                }
            }
        }

        //Senão encontrar nenhuma jogada do adversário sorteia um valor!
        Random r = new Random();
        return r.nextInt(game.getSize() - 1);
    }

    private int verifyBotVertical(int i, int j) {
        int counter = 1;

        for (int k = 1; k < 3; k++) {
            if (gameGrid[i + k][j] == 0) {
                counter++;
            } else return 0;
        }
        return counter;
    }

    private void whoIsActive() {

        if (game.getPlayers().get(0).isActivePlayer()) {
            tvPlayerName.setText(game.getPlayers().get(0).getName().toString());
            activePlayer = 0;
        } else {
            tvPlayerName.setText(game.getPlayers().get(1).getName().toString());
            activePlayer = 1;
        }
    }

    private void checkIfBotIsFirst() {

        //Verifica se o primeiro a jogar é o BOT Roberto, se for faz a jogada dele e troca de jogador!
        if (game.getGameMode().equals(GameSettingsActivity.S_SINGLE_PLAYER) && game.getPlayers().get(1).isActivePlayer()) {
            whoIsActive();
            botMove();
            game.getPlayers().get(1).setActivePlayer(false);
            game.getPlayers().get(0).setActivePlayer(true);
        }
    }

}
