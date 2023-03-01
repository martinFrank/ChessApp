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
import com.github.martinfrank.games.chessmodel.message.FcGetOpenGamesMessage;
import com.github.martinfrank.games.chessmodel.message.FsSubmitOpenGamesMessage;
import com.github.martinfrank.games.chessmodel.message.Message;
import com.github.martinfrank.games.chessmodel.model.Game;
import com.github.martinfrank.games.chessmodel.model.Player;

import java.util.List;

public class StartFragment extends Fragment implements ChessMessageReceiver {

    private static final String LOG_TAG = "StartFragment";
    private FragmentStartBinding binding;
    private ChessServerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStartBinding.inflate(inflater, container, false);
        mainActivity().setChessMessageReceiver(this);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        RecyclerView chessServerView = binding.chessServers;

//        games = new Games();
        // Create adapter passing in the sample user data
//        adapter = new ChessServerAdapter(games, this);
                adapter = new ChessServerAdapter( this);
        // Attach the adapter to the recyclerview to populate items
        chessServerView.setAdapter(adapter);
        // Set layout manager to position the items
        chessServerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
    public void connect(Game game) {
        Log.d(LOG_TAG, "connect to " + game);

        Bundle bundle = new Bundle();
        String gameString = mainActivity().modelParser.gameToJson(game);

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
            int size = games.size();
            adapter.setItems(games, getPlayer());
            adapter.notifyItemRangeRemoved(0, size);
            adapter.notifyItemRangeInserted(0, games.size());
        });
    }


    private void sendMessage(Message m) {
        Log.d(LOG_TAG, "sendMessage:"+m);
        mainActivity().sendMessage(m);
    }

    private Player getPlayer() {
        return mainActivity().player;
    }

    private MainActivity mainActivity() {
        return ((MainActivity) getActivity());
    }
}