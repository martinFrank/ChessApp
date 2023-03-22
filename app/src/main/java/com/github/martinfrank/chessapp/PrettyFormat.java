package com.github.martinfrank.chessapp;

import com.github.martinfrank.games.chessmodel.model.Game;
import com.github.martinfrank.games.chessmodel.model.Player;
import com.github.martinfrank.games.chessmodel.model.chess.Field;
import com.github.martinfrank.games.chessmodel.model.chess.Figure;
import com.github.martinfrank.games.chessmodel.model.chess.Participant;

import java.util.UUID;

public class PrettyFormat {

    public static String prettyGame(Game game ){
        String host = game.getHostPlayer().playerName;
        String vs = game.getGuestPlayer() == null ? "n/a" : game.getGuestPlayer().playerName;
        return host+" vs. "+vs;
    }

    public static String prettyPlayer(Player player ){
        if(player == null){
            return "?";
        }
        return player.playerName;
    }

    public static String playerName(Participant participant) {
        if(participant == null){
            return "";
        }
        Player player = participant.player;
        return player == null?"":player.playerName;
    }

    public static String prettyField(Field field) {
        if(field == null){
            return "?";
        }
        return field.column+field.row;
    }

    public static String prettyFigure(Figure figure) {
        if(figure == null){
            return "?";
        }
        //return figure.type.name();
        return figure.symbol;
    }
}
