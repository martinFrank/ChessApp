package com.github.martinfrank.chessapp;

import com.github.martinfrank.games.chessmodel.model.Game;
import com.github.martinfrank.games.chessmodel.model.Player;

import java.util.UUID;

public class PrettyFormat {

    public static String prettyGame(Game game ){
        String host = game.hostPlayer.playerName;
        String vs = game.getGuestPlayer() == null ? "n/a" : game.getGuestPlayer().playerName;
        return host+" vs. "+vs;
    }


    public static String playerName(Player guestPlayer) {
        return guestPlayer == null?"":guestPlayer.playerName;
    }
}
