package com.github.martinfrank.chessapp;

import com.github.martinfrank.games.chessmodel.model.Game;
import com.github.martinfrank.games.chessmodel.model.Player;
import com.github.martinfrank.games.chessmodel.model.chess.Participant;

import java.util.UUID;

public class PrettyFormat {

    public static String prettyGame(Game game ){
        String host = game.hostPlayer.playerName;
        String vs = game.getGuestPlayer() == null ? "n/a" : game.getGuestPlayer().playerName;
        return host+" vs. "+vs;
    }

//
//    public static String playerName(Player guestPlayer) {
//        return guestPlayer == null?"":guestPlayer.playerName;
//    }

    public static String playerName(Participant participant) {
        if(participant == null){
            return "";
        }
        Player player = participant.player;
        return player == null?"":player.playerName;
    }
}
