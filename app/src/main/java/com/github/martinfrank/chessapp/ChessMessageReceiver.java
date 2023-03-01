package com.github.martinfrank.chessapp;

import com.github.martinfrank.games.chessmodel.message.Message;

public interface ChessMessageReceiver {
    void receive(Message message);
}
