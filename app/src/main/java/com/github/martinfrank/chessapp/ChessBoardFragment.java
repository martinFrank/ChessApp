package com.github.martinfrank.chessapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.github.martinfrank.chessapp.databinding.FragmentChessBoardBinding;
import com.github.martinfrank.games.chessmodel.message.FcCreateGameMessage;
import com.github.martinfrank.games.chessmodel.message.FcGetGameContentMessage;
import com.github.martinfrank.games.chessmodel.message.FcJoinGameMessage;
import com.github.martinfrank.games.chessmodel.message.FsConfirmJoinGamesMessage;
import com.github.martinfrank.games.chessmodel.message.FsSubmitCreatedGameMessage;
import com.github.martinfrank.games.chessmodel.message.FsSubmitGameContentMessage;
import com.github.martinfrank.games.chessmodel.message.Message;
import com.github.martinfrank.games.chessmodel.model.Game;
import com.github.martinfrank.games.chessmodel.model.GameContent;
import com.github.martinfrank.games.chessmodel.model.Player;
import com.github.martinfrank.games.chessmodel.model.chess.Color;

public class ChessBoardFragment extends Fragment implements ChessMessageReceiver {

    private static final String LOG_TAG = "ChessBoardFragment";
    private FragmentChessBoardBinding binding;
    private Game game;
    private GameContent gameContent;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChessBoardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(LOG_TAG, "here we are...");

        binding.buttonReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(ChessBoardFragment.this)
                        .navigate(R.id.action_ChessBoardFragment_to_StartFragment);
            }
        });


        ((MainActivity) getActivity()).setChessMessageReceiver(this);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String gameJson = bundle.getString(MainActivity.BUNDLE_GAME_JSON);
            if (("" + gameJson).isEmpty()) {
                sendMessage(new FcCreateGameMessage(getPlayer()));
            } else {
                this.game = ((MainActivity) getActivity()).modelParser.gameFromJson(gameJson);
                Log.d(LOG_TAG, "continue with game id from bundle");
                if (!game.isParticipant(getPlayer())) {
                    sendMessage(new FcJoinGameMessage(getPlayer(), game.gameId));
                } else {
                    sendMessage(new FcGetGameContentMessage(getPlayer(), game));
                }
            }
        }


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
            case FS_SUBMIT_CREATED_GAME: {
                FsSubmitCreatedGameMessage createdGameMessage = (FsSubmitCreatedGameMessage) message;
                this.game = createdGameMessage.game;
                sendMessage(new FcGetGameContentMessage(getPlayer(), game));
                break;
            }
            case FS_CONFIRM_JOIN_GAME: {
                FsConfirmJoinGamesMessage createdGameMessage = (FsConfirmJoinGamesMessage) message;
                this.game = createdGameMessage.game;
                sendMessage(new FcGetGameContentMessage(getPlayer(), game));
                break;
            }
            case FS_SUBMIT_GAME_CONTENT: {
                FsSubmitGameContentMessage submitGameContentMessage = (FsSubmitGameContentMessage) message;
                this.gameContent = submitGameContentMessage.content;
                updateBoard();
                break;
            }

        }
    }

    private void updateBoard() {
        binding.chessBoard.updateBoard(gameContent);
        binding.chessBoard.invalidate();
        if (game.gameContent.getHostColor() == Color.BLACK){
            binding.textBlackPlayerName.setText(PrettyFormat.playerName(game.hostPlayer));
            binding.textWhitePlayerName.setText(PrettyFormat.playerName(game.getGuestPlayer()));
        }

        if (game.gameContent.getHostColor() == Color.WHITE){
            binding.textWhitePlayerName.setText(PrettyFormat.playerName(game.hostPlayer));
            binding.textBlackPlayerName.setText(PrettyFormat.playerName(game.getGuestPlayer()));
        }
    }

    private void sendMessage(Message m) {
        mainActivity().sendMessage(m);
    }

    private Player getPlayer() {
        return mainActivity().player;
    }

    private MainActivity mainActivity() {
        return ((MainActivity) getActivity());
    }

}