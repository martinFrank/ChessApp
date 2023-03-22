package com.github.martinfrank.chessapp;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.github.martinfrank.chessapp.databinding.FragmentChessBoardBinding;
import com.github.martinfrank.games.chessmodel.message.Message;
import com.github.martinfrank.games.chessmodel.message.creategame.FcCreateGameMessage;
import com.github.martinfrank.games.chessmodel.message.creategame.FsSubmitCreatedGameMessage;
import com.github.martinfrank.games.chessmodel.message.disconnect.FsSubmitDisconnectMessage;
import com.github.martinfrank.games.chessmodel.message.getgamecontent.FsSubmitGameContentMessage;
import com.github.martinfrank.games.chessmodel.message.joingame.FcJoinGameMessage;
import com.github.martinfrank.games.chessmodel.message.joingame.FsConfirmJoinGamesMessage;
import com.github.martinfrank.games.chessmodel.message.movefigure.FcMoveFigureMessage;
import com.github.martinfrank.games.chessmodel.message.movefigure.FsSubmitMoveFigureMessage;
import com.github.martinfrank.games.chessmodel.message.selectcolor.FcSelectColorMessage;
import com.github.martinfrank.games.chessmodel.message.selectcolor.FsSubmitSelectColorMessage;
import com.github.martinfrank.games.chessmodel.message.selectfield.FcSelectFieldMessage;
import com.github.martinfrank.games.chessmodel.message.selectfield.FsSubmitSelectFieldMessage;
import com.github.martinfrank.games.chessmodel.message.startgame.FcStartGameMessage;
import com.github.martinfrank.games.chessmodel.message.startgame.FsSubmitStartGameMessage;
import com.github.martinfrank.games.chessmodel.model.Game;
import com.github.martinfrank.games.chessmodel.model.ModelParser;
import com.github.martinfrank.games.chessmodel.model.Player;
import com.github.martinfrank.games.chessmodel.model.chess.Color;
import com.github.martinfrank.games.chessmodel.model.chess.Field;
import com.github.martinfrank.games.chessmodel.model.chess.Participant;

public class ChessBoardFragment extends Fragment implements ChessMessageReceiver {

    private static final String LOG_TAG = "ChessBoardFragment";
    private FragmentChessBoardBinding binding;
    private Game game;

    private ChessClient client;
    private Field previousSelection;

    private Console console;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChessBoardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(LOG_TAG, "here we are...");
        console = new Console();
        ModelParser modelParser = new ModelParser();

        binding.buttonReturn.setOnClickListener(button -> NavHostFragment.findNavController(ChessBoardFragment.this)
                .navigate(R.id.action_ChessBoardFragment_to_StartFragment));

        binding.buttonSelectColor.setOnClickListener(view1 -> {
            Color myColor = game.chessGame.getThisParticipant(getPlayer()).color;
            client.sendMessage(new FcSelectColorMessage(getPlayer(), game.gameId, myColor.getOpposite()));
        });

        binding.buttonStartGame.setOnClickListener(button -> client.sendMessage(new FcStartGameMessage(getPlayer(), game.gameId)));

        binding.chessBoard.setOnTouchListener((board, motionEvent) -> {
            ChessBoardView chessBoardView = (ChessBoardView) board;
            Field f = chessBoardView.getFieldAt(motionEvent.getX(), motionEvent.getY());
            Log.d(LOG_TAG, "touched field: " + f);
            selectField(f);
            return false;
        });

        binding.console.setVerticalScrollBarEnabled(true);
        binding.console.setMovementMethod(new ScrollingMovementMethod());

        Bundle bundle = getArguments();
        if (bundle != null) {
            String gameJson = bundle.getString(MainActivity.BUNDLE_GAME_JSON);
            if (("" + gameJson).isEmpty()) {
                this.game = null;
            } else {
                this.game = modelParser.gameFromJson(gameJson);
            }
        }

    }

    private void selectField(Field f) {
        if (f == null) {
            return;
        }
        boolean isValidMove = game.chessGame.isValidMove(previousSelection, f, getPlayer());
        boolean hasSelection = game.chessGame.getThisParticipant(getPlayer()).hasSelection();
        Log.d(LOG_TAG, "field="+f);
        Log.d(LOG_TAG, "previous="+previousSelection);
        Log.d(LOG_TAG, "isValidMove="+isValidMove);
        Log.d(LOG_TAG, "hasSelection="+hasSelection);
        if (isValidMove && hasSelection) {
            Log.d(LOG_TAG, "i could move :-)");
            //FIXME confirm move
            FcMoveFigureMessage moveFigureMessage = new FcMoveFigureMessage(getPlayer(), game.gameId, previousSelection, f);
            client.sendMessage(moveFigureMessage);
        } else {
            FcSelectFieldMessage fieldMessage = new FcSelectFieldMessage(getPlayer(), game.gameId, f);
            client.sendMessage(fieldMessage);
        }
        previousSelection = f;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void receive(Message message) {
        Log.d(LOG_TAG, "receive message: " + message);
        switch (message.msgType) {
            case FS_SUBMIT_LOGIN: {
                if (game == null) { // habe kein spiel vom vorgänger, d.h. ich starte ein neues //FIXME use a proper field to transfer this information
                    client.sendMessage(new FcCreateGameMessage(getPlayer()));
                } else {
                    Log.d(LOG_TAG, "continue with game id from bundle");
                    client.sendMessage(new FcJoinGameMessage(getPlayer(), game.gameId));
                }
                break;
            }
            case FS_SUBMIT_CREATED_GAME: {
                Log.d(LOG_TAG, "FS_SUBMIT_CREATED_GAME");
                FsSubmitCreatedGameMessage createdGameMessage = (FsSubmitCreatedGameMessage) message;
                Log.d(LOG_TAG, "createdGameMessage=" + createdGameMessage);
                updateGame(createdGameMessage.game);
                Log.d(LOG_TAG, "updateGame");
                break;
            }
            case FS_SUBMIT_START_GAME: {
                FsSubmitStartGameMessage createdGameMessage = (FsSubmitStartGameMessage) message;
                addConsole("the game has started!");
                updateGame(createdGameMessage.game);
                break;
            }
            case FS_CONFIRM_JOIN_GAME: {
                FsConfirmJoinGamesMessage joinGamesMessage = (FsConfirmJoinGamesMessage) message;
                if(!getPlayer().equals(joinGamesMessage.player)){
                    addConsole("player " + PrettyFormat.prettyPlayer(joinGamesMessage.player) + " joined");
                }
                updateGame(joinGamesMessage.game);
                break;
            }
            case FS_SUBMIT_GAME_CONTENT: {
                FsSubmitGameContentMessage submitGameContentMessage = (FsSubmitGameContentMessage) message;
                updateGame(submitGameContentMessage.game);
                break;
            }
            case FS_SUBMIT_SELECT_COLOR: {
                FsSubmitSelectColorMessage selectColorMessage = (FsSubmitSelectColorMessage) message;
                String color = game.chessGame.getThisParticipant(getPlayer()).color.toString();
                addConsole("color has changed, your color is now " + color);
                updateGame(selectColorMessage.game);
                break;
            }
            case FS_SUBMIT_SELECT_FIELD: {
                FsSubmitSelectFieldMessage selectFieldMessage = (FsSubmitSelectFieldMessage) message;
                String field = PrettyFormat.prettyField(selectFieldMessage.field);
                String player = PrettyFormat.prettyPlayer(selectFieldMessage.player);
                addConsole("Player "+player+" clicked on field "+field);
                updateGame(selectFieldMessage.game);
                break;
            }
            case FS_SUBMIT_MOVE_FIGURE: {
                FsSubmitMoveFigureMessage moveFigureMessage = (FsSubmitMoveFigureMessage) message;
                String from = PrettyFormat.prettyField(moveFigureMessage.from);
                String to = PrettyFormat.prettyField(moveFigureMessage.to);
                String figure = PrettyFormat.prettyFigure(moveFigureMessage.game.chessGame.board.findFigure(moveFigureMessage.to));
                String name = PrettyFormat.prettyPlayer(moveFigureMessage.player);
                addConsole("player " + name + " moved figure " + figure + " from " + from + " to " + to);
                updateGame(moveFigureMessage.game);
                break;
            }
            case FS_SUBMIT_DISCONNECT: {
                FsSubmitDisconnectMessage submitDisconnectMessage = (FsSubmitDisconnectMessage) message;
                addConsole("player " + PrettyFormat.prettyPlayer(submitDisconnectMessage.player) + " has left the game");
                break;
            }
        }
    }

    private void addConsole(String s) {
        console.add(s);
        getActivity().runOnUiThread(() -> {
            Log.d(LOG_TAG, "runOnUiStarted, added text to console: " + s);
            binding.console.setText(console.getText());
            binding.console.scrollTo(0, 0);
            Log.d(LOG_TAG, "runOnUiFinished");
        });

    }

    private void updateGame(Game game) {
        this.game = game;
        updateGui();
    }


    private void updateGui() {
        Log.d(LOG_TAG, "update Board GUI");
        binding.chessBoard.updateBoard(game);
        binding.chessBoard.invalidate();
        Participant me = game.chessGame.getThisParticipant(getPlayer());
        Participant other = game.chessGame.getOtherParticipant(getPlayer());
        if (me.color == Color.BLACK) {
            binding.textBlackPlayerName.setText(PrettyFormat.playerName(me));
            binding.textWhitePlayerName.setText(PrettyFormat.playerName(other));
        }
        if (me.color == Color.WHITE) {
            binding.textWhitePlayerName.setText(PrettyFormat.playerName(me));
            binding.textBlackPlayerName.setText(PrettyFormat.playerName(other));
        }
        if (game.chessGame.isStarted()) {
            binding.textCurrentPlayerName.setText(PrettyFormat.playerName(game.chessGame.getCurrentParticipant()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        client = new ChessClient(getPlayer(), this);
        client.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        client.stop();
    }

    private Player getPlayer() {
        return ((MainActivity) getActivity()).getPlayer();
    }


}