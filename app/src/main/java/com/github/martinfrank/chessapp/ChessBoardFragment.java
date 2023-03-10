package com.github.martinfrank.chessapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.github.martinfrank.chessapp.databinding.FragmentChessBoardBinding;
import com.github.martinfrank.games.chessmodel.message.Message;
import com.github.martinfrank.games.chessmodel.message.creategame.FcCreateGameMessage;
import com.github.martinfrank.games.chessmodel.message.creategame.FsSubmitCreatedGameMessage;
import com.github.martinfrank.games.chessmodel.message.getgamecontent.FcGetGameContentMessage;
import com.github.martinfrank.games.chessmodel.message.getgamecontent.FsSubmitGameContentMessage;
import com.github.martinfrank.games.chessmodel.message.joingame.FcJoinGameMessage;
import com.github.martinfrank.games.chessmodel.message.joingame.FsConfirmJoinGamesMessage;
import com.github.martinfrank.games.chessmodel.message.selectColor.FsSubmitSelectColorMessage;
import com.github.martinfrank.games.chessmodel.model.Game;
import com.github.martinfrank.games.chessmodel.model.GameContent;
import com.github.martinfrank.games.chessmodel.model.ModelParser;
import com.github.martinfrank.games.chessmodel.model.Player;
import com.github.martinfrank.games.chessmodel.model.chess.Color;
import com.github.martinfrank.games.chessmodel.model.chess.Field;

public class ChessBoardFragment extends Fragment implements ChessMessageReceiver {

    private static final String LOG_TAG = "ChessBoardFragment";
    private FragmentChessBoardBinding binding;
    private Game game;

    private ChessClient client;
    private ModelParser modelParser;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChessBoardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(LOG_TAG, "here we are...");

        modelParser = new ModelParser();

        binding.buttonReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(ChessBoardFragment.this)
                        .navigate(R.id.action_ChessBoardFragment_to_StartFragment);
            }
        });

        binding.chessBoard.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                ChessBoardView chessBoardView = (ChessBoardView) view;
                Field f = chessBoardView.getFieldAt(motionEvent.getX(), motionEvent.getY());
                Log.d(LOG_TAG, "touched field: "+f);
                selectField(f);
                return false;
            }
        });

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
//        new FcSelectFigureMessage();
//        client.sendMessage(new FcJoinGameMessage(getPlayer(), game.gameId));
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
                    if (!game.isParticipant(getPlayer())) {
                        client.sendMessage(new FcJoinGameMessage(getPlayer(), game.gameId));
                    } else {
                        client.sendMessage(new FcGetGameContentMessage(getPlayer(), game));
                    }
                }
                break;
            }
            case FS_SUBMIT_CREATED_GAME: {
                FsSubmitCreatedGameMessage createdGameMessage = (FsSubmitCreatedGameMessage) message;
                this.game = createdGameMessage.game;
                client.sendMessage(new FcGetGameContentMessage(getPlayer(), game));
                break;
            }
            case FS_CONFIRM_JOIN_GAME: {
                FsConfirmJoinGamesMessage createdGameMessage = (FsConfirmJoinGamesMessage) message;
                this.game = createdGameMessage.game;
                client.sendMessage(new FcGetGameContentMessage(getPlayer(), game));
                break;
            }
            case FS_SUBMIT_GAME_CONTENT: {
                FsSubmitGameContentMessage submitGameContentMessage = (FsSubmitGameContentMessage) message;
                updateBoardContent(submitGameContentMessage.content);
                break;
            }
            case FS_SUBMIT_SELECT_COLOR: {
//                FsSubmitSelectColorMessage selectColorMessage = (FsSubmitSelectColorMessage) message;
//                game.gameContent = selectColorMessage.;
//                updateBoardContent(submitGameContentMessage.content);
            }

        }
    }

    private void updateBoardContent(GameContent content) {
        game.gameContent = content;
        updateGui();
    }

    private void updateGui() {
        Log.d(LOG_TAG, "update Board GUI");
        binding.chessBoard.updateBoard(game.gameContent);
        binding.chessBoard.invalidate();
        Log.d(LOG_TAG, "game = "+game);
        Log.d(LOG_TAG, "game.gameContent = "+game.gameContent);
        Log.d(LOG_TAG, "game.gameContent.getHostColor() = "+game.gameContent.getHostColor());
        if (game.gameContent.getHostColor() == Color.BLACK) {
            binding.textBlackPlayerName.setText(PrettyFormat.playerName(game.hostPlayer));
            binding.textWhitePlayerName.setText(PrettyFormat.playerName(game.getGuestPlayer()));
        }
        if (game.gameContent.getHostColor() == Color.WHITE) {
            binding.textWhitePlayerName.setText(PrettyFormat.playerName(game.hostPlayer));
            binding.textBlackPlayerName.setText(PrettyFormat.playerName(game.getGuestPlayer()));
        }
        Log.d(LOG_TAG, "game.gameContent.getCurrentPlayer() = "+game.gameContent.getCurrentPlayer());
        binding.textCurrentPlayerName.setText(PrettyFormat.playerName(game.gameContent.getCurrentPlayer()));
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