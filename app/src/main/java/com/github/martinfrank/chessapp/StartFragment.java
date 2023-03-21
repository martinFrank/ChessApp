package com.github.martinfrank.chessapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.martinfrank.chessapp.databinding.FragmentStartBinding;
import com.github.martinfrank.games.chessmodel.message.Message;
import com.github.martinfrank.games.chessmodel.message.deletegame.FcDeleteGameMessage;
import com.github.martinfrank.games.chessmodel.message.getopengames.FcGetOpenGamesMessage;
import com.github.martinfrank.games.chessmodel.message.getopengames.FsSubmitOpenGamesMessage;
import com.github.martinfrank.games.chessmodel.model.Game;
import com.github.martinfrank.games.chessmodel.model.ModelParser;
import com.github.martinfrank.games.chessmodel.model.Player;

import java.util.List;

public class StartFragment extends Fragment implements ChessMessageReceiver {

    private static final String LOG_TAG = "StartFragment";
    private FragmentStartBinding binding;
    private ChessServerAdapter adapter;
    private ChessClient client;
    private ModelParser modelParser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        modelParser = new ModelParser();


        binding.buttonCreateServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(MainActivity.BUNDLE_GAME_JSON, "");
                NavHostFragment.findNavController(StartFragment.this)
                        .navigate(R.id.action_StartFragment_to_ChessBoardFragment, bundle);
            }
        });

        binding.buttonRefresh.setOnClickListener(view1 -> updateServerList());

        binding.buttonSettings.setOnClickListener(button ->
            NavHostFragment.findNavController(StartFragment.this)
                    .navigate(R.id.action_StartFragment_to_SettingsFragment)
        );

        RecyclerView chessServerView = binding.chessServers;

        adapter = new ChessServerAdapter(this);
        chessServerView.setAdapter(adapter);
        chessServerView.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
        client = new ChessClient(getPlayer(), this);
        client.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
        client.stop();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateServerList() {
        //get open games
        FcGetOpenGamesMessage fcGetOpenGamesMessage = new FcGetOpenGamesMessage(getPlayer());
        sendMessage(fcGetOpenGamesMessage);
    }


    //called from ChessServerAdapter
    public void connectToGame(Game game) {
        Log.d(LOG_TAG, "connect to " + game);

        Bundle bundle = new Bundle();
        String gameString = modelParser.gameToJson(game);

        bundle.putString(MainActivity.BUNDLE_GAME_JSON, gameString);
        NavHostFragment.findNavController(StartFragment.this)
                .navigate(R.id.action_StartFragment_to_ChessBoardFragment, bundle);
    }

    @Override
    public void receive(Message message) {
        switch (message.msgType) {
            case FS_SUBMIT_OPEN_GAMES: {
                handleGetOpenGames((FsSubmitOpenGamesMessage) message);
                return;
            }

        }
    }

    private void handleGetOpenGames(FsSubmitOpenGamesMessage message) {
        List<Game> games = message.games;
        getActivity().runOnUiThread(() -> {
            Log.d(LOG_TAG,"runOnUiStarted");
            int size = games.size();
            adapter.setItems(games, getPlayer());
            adapter.notifyItemRangeRemoved(0, size);
            adapter.notifyItemRangeInserted(0, games.size());
            Log.d(LOG_TAG,"runOnUiFinished");
        });
    }


    private void sendMessage(Message m) {
        Log.d(LOG_TAG, "sendMessage:" + m);
        client.sendMessage(m);
    }

    private Player getPlayer() {
        return ((MainActivity) getActivity()).getPlayer();
    }


    public void deleteGame(Game game) {
        Log.d(LOG_TAG, "deleting game");
        FcDeleteGameMessage deleteGameMessage = new FcDeleteGameMessage(getPlayer(), game.gameId);
        sendMessage(deleteGameMessage);
    }
}