package com.github.martinfrank.chessapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.github.martinfrank.games.chessmodel.model.Game;
import com.github.martinfrank.games.chessmodel.model.Player;

import java.util.ArrayList;
import java.util.List;

public class ChessServerAdapter extends RecyclerView.Adapter<ChessServerAdapter.ViewHolder> {

    private final List<Game> myGames = new ArrayList<>();
    private final StartFragment startFragment;
    private Player player;

    // Pass in the contact array into the constructor
    public ChessServerAdapter(StartFragment startFragment) {
        this.startFragment = startFragment;
    }

    @Override
    public ChessServerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.server_list_item, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ChessServerAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        Game game = myGames.get(position);

        // Set item views based on your views and data model
        TextView textView = holder.gameNameTextView;
        textView.setText("     " + PrettyFormat.prettyGame(game));
        boolean isMine = player != null && player.equals(game.getHostPlayer());
        holder.deleteGameButton.setEnabled(isMine);
        holder.isMineCheckBox.setChecked(isMine);
        boolean isPart = player != null && player.equals(game.getGuestPlayer());
        holder.isParticipantCheckBox.setChecked(isPart);
        Button joinGameButton = holder.joinGameButton;
        joinGameButton.setEnabled(true);
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return myGames.size();
    }

    public void setItems(List<Game> games, Player player) {
        this.myGames.clear();
        this.myGames.addAll(games);
        this.player = player;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
//        public TextView serverNameTextView;
        public TextView gameNameTextView;

        public CheckBox isMineCheckBox;
        public CheckBox isParticipantCheckBox;
        public Button joinGameButton;
        public Button deleteGameButton;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            gameNameTextView = itemView.findViewById(R.id.text_game_name);
            joinGameButton = itemView.findViewById(R.id.button_join_game);
            joinGameButton.setOnClickListener(view -> handleConnectClick());
            deleteGameButton = itemView.findViewById(R.id.button_delete_game);
            deleteGameButton.setOnClickListener(view -> handleDeleteClick());
            isMineCheckBox = itemView.findViewById(R.id.checkbox_isMine);
            isParticipantCheckBox = itemView.findViewById(R.id.checkbox_isParticipant);


        }

        private void handleConnectClick() {
            int position = getAdapterPosition(); // gets item position
            if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
                Game chessGame = myGames.get(position);
                startFragment.connectToGame(chessGame);
            }
        }

        private void handleDeleteClick() {
            int position = getAdapterPosition(); // gets item position
            if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
                Game chessGame = myGames.get(position);
                startFragment.deleteGame(chessGame);
            }
        }
    }
}
