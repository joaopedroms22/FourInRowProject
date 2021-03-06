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

import java.io.FileOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import pt.isec.a21210827.fourinrow.Activity.MainActivity;
import pt.isec.a21210827.fourinrow.Class.Communication;
import pt.isec.a21210827.fourinrow.Class.Game;
import pt.isec.a21210827.fourinrow.Class.GameGridViewAdapter;
import pt.isec.a21210827.fourinrow.Class.GameVariables;
import pt.isec.a21210827.fourinrow.R;
import pt.isec.a21210827.fourinrow.Activity.GameSettingsActivity;

public class GameEngine implements Serializable {

    final public static String LASTGAME = "lastGame";

    private static GameEngine instance;

    //Generic Variables
    private int[][] gameGrid;
    private int[] list;
    private int posx = 0;
    private int posy;
    private int posyFinal = 0;
    private int activePlayer;
    private int size;

    //Class Objects
    private Game game;
    private Context context;

    //Views
    private GridView gvGame;
    private Chronometer mChrono;
    private TextView tvPlayer, tvScore;

    //Communication
    private Communication com;
    private GameVariables gameVar;
    private String winner = null;

    public static GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }
        return instance;
    }

    public int[] getList() {
        return list;
    }

    public int[] getGameGrid() {

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < gameGrid.length; i++) {
            // tiny change 1: proper dimensions
            for (int j = 0; j < gameGrid[i].length; j++) {
                // tiny change 2: actually store the values
                list.add(gameGrid[j][i]);
            }
        }
        // now you need to find a mode in the list.

        // tiny change 3, if you definitely need an array
        int[] vector = new int[list.size()];
        for (int i = 0; i < vector.length; i++) {
            vector[i] = list.get(i);
        }

        return vector;
    }

    public void setGameGrid(int [][] gameGrid){
        this.gameGrid = gameGrid;
    }

    public void setGameGrid(int[] gameGrid, int size) {

        int[][] aux = new int[size][size];

        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                aux[i][j] = gameGrid[(j * size) + i];

        this.gameGrid = aux;
    }

    public void setList(int[] list) {
        this.list = list;
        gvGame.setAdapter(new GameGridViewAdapter(list, context));
    }

    public Game getActiveGame() {
        return game;
    }

    public void setActiveGame(Game game) {
        this.game = game;
        whoIsActive();
    }

    public void startGame(final Context context, final GridView gvGame, final Game gameInstance, final Chronometer mChrono, final TextView tvPlayer, final TextView tvScore, final Communication com) {

        this.gvGame = gvGame;
        this.context = context;
        this.game = gameInstance;
        this.mChrono = mChrono;
        this.tvPlayer = tvPlayer;
        this.tvScore = tvScore;
        this.com = com;

        gameVar = new GameVariables();
        size = gameInstance.getSize();
        gameGrid = new int[size][size];
        list = new int[size * size];

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
        mChrono.start();

        //Inicia os Scores!
        tvScore.setText(game.getPlayers().get(0).getName() + ": " + game.getPlayers().get(0).getScore() + "   &&   " + game.getPlayers().get(1).getName() + ": " + game.getPlayers().get(1).getScore());

        if(!game.getGameMode().equals(GameSettingsActivity.S_MULTIPLAYER_ONLINE)) {
            //Sortei um valor entre '0' ou '1', para descobrir que jogador começa o jogo primeiro e coloca na textView
            Random random = new Random();
            int r = random.nextInt(2);
            game.getPlayers().get(r).setActivePlayer(true);
        }

        whoIsActive();

        //Verifica se o primeiro a jogar é o BOT Roberto, se for faz a jogada dele e troca de jogador!
        checkIfBotIsFirst();

        gvGame.setOnItemClickListener(new GridView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int pos, long l) {

                posx = (pos % size);
                posy = (pos / size);

                whoIsActive();

                if (game.getGameMode().equals(GameSettingsActivity.S_MULTIPLAYER_ONLINE)) {

                    if(!com.isCanMove()){
                        Toast.makeText(context, "Nao podes mover!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (putPiece(gameGrid, posx)) {
                    if (game.getPlayers().get(0).isActivePlayer()) {
                        list[(size * posyFinal) + posx] = R.drawable.circle_shape_red;
                    } else {
                        list[(size * posyFinal) + posx] = R.drawable.circle_shape_yellow;
                    }
                    gvGame.setAdapter(new GameGridViewAdapter(list, context));
                }

                if (checkEndGame(posx, posyFinal)) {
                    saveScore();
                    saveElapsedTime(mChrono);
                    winnerDialogBox("Ganhou o Jogador: " + game.getPlayers().get(activePlayer).getName());
                }
                else {

                    if (!game.getGameMode().equals(GameSettingsActivity.S_MULTIPLAYER_ONLINE)) {
                        switchPlayer();
                    }
                }

                if (game.getGameMode().equals(GameSettingsActivity.S_MULTIPLAYER_ONLINE)) {
                    if(winner == null) sendMove();
                }
            }
        });
    }

    private void sendMove() {

        gameVar = new GameVariables();
        gameVar.setList(list);
        gameVar.setGameGrid(gameGrid);
        switchPlayer();
        gameVar.setGame(game);
        com.setCanMove(false);
        com.sendObjecttoServer(gameVar);

    }

    private void saveScore() {
        int score = game.getPlayers().get(activePlayer).getScore();

        //Incrementa o Score dos jogadores sempre que vencerem um jogo!
        game.getPlayers().get(activePlayer).setScore(score + 1);
        game.setWinner(activePlayer);

        saveOnFile();

        tvScore.setText(game.getPlayers().get(0).getName() + ": " + game.getPlayers().get(0).getScore() + "   &&   " + game.getPlayers().get(1).getName() + ": " + game.getPlayers().get(1).getScore());

        if (game.getGameMode().equals(GameSettingsActivity.S_MULTIPLAYER_ONLINE)) {
            gameVar = new GameVariables();
            gameVar.setList(list);
            gameVar.setGameGrid(gameGrid);
            gameVar.setWinner(game.getPlayers().get(activePlayer).getName());
            gameVar.setGame(game);
            com.setCanMove(false);
            com.sendObjecttoServer(gameVar);
        }

    }

    private void saveOnFile() {

        String timeStamp = new SimpleDateFormat("yyyy.MM.dd").format(new Date());
        int winner = game.getWinner();
        int loser;

        if (winner == 0) {
            loser = 1;
        } else {
            loser = 0;
        }

        String filename = "gameHistoric.txt";
        String string = "(" + timeStamp + ")" + " - " + "Ganhou: " + game.getPlayers().get(winner).getName() + "  Perdeu: " + game.getPlayers().get(loser).getName() + "\n\n";
        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(filename, Context.MODE_APPEND);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void winnerDialogBox(String message) {
        new AlertDialog.Builder(context)
                .setTitle("Fim do Jogo")
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        game.setFinished(true);
                        context.deleteFile(LASTGAME);
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        ((Activity) context).finish();
                    }
                })
                .setNegativeButton("Restart", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        restartGame();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        ((Activity) context).finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void switchPlayer() {
        switch (game.getGameMode()) {

            case GameSettingsActivity.S_SINGLE_PLAYER:
                if (game.getPlayers().get(0).isActivePlayer()) {
                    tvPlayer.setText(game.getPlayers().get(1).getName());
                    game.getPlayers().get(0).setActivePlayer(false);
                    botMove();
                } else {
                    tvPlayer.setText(game.getPlayers().get(0).getName());
                    game.getPlayers().get(0).setActivePlayer(true);
                }

            case GameSettingsActivity.S_MULTIPLAYER_LOCAL:
                if (game.getPlayers().get(0).isActivePlayer()) {
                    tvPlayer.setText(game.getPlayers().get(1).getName());
                    game.getPlayers().get(0).setActivePlayer(false);
                } else {
                    tvPlayer.setText(game.getPlayers().get(0).getName());
                    game.getPlayers().get(0).setActivePlayer(true);
                }
                break;

            case GameSettingsActivity.S_MULTIPLAYER_ONLINE:
                if (game.getPlayers().get(0).isActivePlayer()) {
                    tvPlayer.setText(game.getPlayers().get(1).getName());
                    game.getPlayers().get(0).setActivePlayer(false);
                } else {
                    tvPlayer.setText(game.getPlayers().get(0).getName());
                    game.getPlayers().get(0).setActivePlayer(true);
                }
                break;
        }
    }

    private boolean checkEndGame(int posx, int posy) {

        return checkDiagonalRight(posx, posy) || checkDiagonalLeft(posx, posy) || checkHorizontal(posx, posy) || checkVertical(posx, posy);

    }

    private boolean putPiece(int[][] gameGrid, int posx) {

        //Se o tabuleiro já se encontrar preenchido acaba o jogo, com um empate!
        if (game.getTurns() == (size * size)) {
            winnerDialogBox("Empate!");
            return false;
        }

        //Vai verificar todos os y, daquele x, o ultimo que tenha o valor -1, para poder adicionar o valor.
        for (int i = 0; i < gameGrid.length; i++) {

            //Se o array já estiver cheio não deixa inserir mais nenhum valor.
            if (gameGrid[posx][0] != -1) {
                Toast.makeText(context, "Move Not Allowed", Toast.LENGTH_SHORT).show();
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
                game.addTurn();
                return true;
            }
        }
        //Se percorrer o array inteiro e não encontrar nenhum valor inserido, estamos perante um array vazio, logo vamos adicionar a peça na ultima posição do array.
        gameGrid[posx][gameGrid.length - 1] = activePlayer; // todo: Modificar o "0", para a valor respectivo de cada jogador.
        posyFinal = gameGrid.length - 1;
        game.addTurn();
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
                Toast.makeText(context, "Game Ended Vertically", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, "Game Ended Horizontal", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, "Game Ended Diagonal Right", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, "Game Ended Diagonal Left", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    private void saveElapsedTime(Chronometer mChrono) {
        mChrono.stop();
        game.setGameTime(SystemClock.elapsedRealtime() - mChrono.getBase());
    }

    private void restartGame() {

        game.setTurn(0);

        for (int i = 0; i < gameGrid.length; i++) {
            for (int j = 0; j < gameGrid[i].length; j++) {
                gameGrid[i][j] = -1;
            }
        }

        for (int i = 0; i < list.length; i++) {
            list[i] = R.drawable.circle_shape;
        }

        //Reinicia o chronometro
        mChrono.setBase(SystemClock.elapsedRealtime());
        mChrono.start();

        //Reinicia o activePlayer = false
        game.getPlayers().get(0).setActivePlayer(false);
        game.getPlayers().get(1).setActivePlayer(false);

        //Sortei um valor entre '0' ou '1', para descobrir que jogador começa o jogo primeiro
        Random random = new Random();
        int r = random.nextInt(2);
        game.getPlayers().get(r).setActivePlayer(true);
        tvPlayer.setText(game.getPlayers().get(r).getName());

        //Se o modo de jogo for Single Player e for o Bot Roberto que esteja activo como o 1º a jogar, então vai fazer a jogada do bot!
        checkIfBotIsFirst();

        gvGame.setAdapter(new GameGridViewAdapter(list, context));
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
            gvGame.setAdapter(new GameGridViewAdapter(list, context));
        }

        if (checkEndGame(posx, posyFinal)) { //todo: fazer esta verificação só à 4 iteração!
            saveScore();
            saveElapsedTime(mChrono);
            winnerDialogBox( "Ganhou o Jogador: " + game.getPlayers().get(activePlayer).getName());
        }
    }

    private int chooseMove() {

        //Vai percorrer a a tabela toda á procura de uma jogada realizada do adversário!
        for (int i = 0; i < (size - 1); i++) {
            for (int j = 0; j < (size - 1); j++) {
                if (verifyBotVertical(i, j) != 0) {
                    return verifyBotVertical(i, j);
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
            tvPlayer.setText(game.getPlayers().get(0).getName());
            activePlayer = 0;
        } else {
            tvPlayer.setText(game.getPlayers().get(1).getName());
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

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }
}
