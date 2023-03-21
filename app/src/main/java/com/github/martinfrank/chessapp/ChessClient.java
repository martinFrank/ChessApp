package com.github.martinfrank.chessapp;

import android.util.Log;
import com.github.martinfrank.games.chessmodel.message.Message;
import com.github.martinfrank.games.chessmodel.message.MessageParser;
import com.github.martinfrank.games.chessmodel.message.login.FcLoginMessage;
import com.github.martinfrank.games.chessmodel.model.Player;
import com.github.martinfrank.tcpclientserver.ClientMessageReceiver;
import com.github.martinfrank.tcpclientserver.TcpClient;

public class ChessClient {

    private static final String LOG_TAG = "ChessClient";

    public static final int LOGIN_DELAY_IN_MILLIS = 500;
    private static final String CHESS_SERVER_ADDRESS = "elitegames.chickenkiller.com";
    public static final int CHESS_SERVER_PORT = 8100;

    public TcpClient tcpClient;
    public MessageParser messageParser;

    private final Player player;

    private Thread clientThread;


    public ChessClient(Player player, ChessMessageReceiver messageReceiver) {
        this.player = player;
        messageParser = new MessageParser();

//        tcpClient = new TcpClient(CHESS_SERVER_ADDRESS, CHESS_SERVER_PORT, new ClientMessageReceiver() {
//        tcpClient = new TcpClient( "192.168.0.65", CHESS_SERVER_PORT, new ClientMessageReceiver(){
//        tcpClient = new TcpClient( "192.168.0.60", CHESS_SERVER_PORT, new ClientMessageReceiver(){
//        tcpClient = new TcpClient( "192.168.56.1", CHESS_SERVER_PORT, new ClientMessageReceiver(){
//        tcpClient = new TcpClient("192.168.61.104", CHESS_SERVER_PORT, new ClientMessageReceiver() {
        tcpClient = new TcpClient("192.168.61.221", CHESS_SERVER_PORT, new ClientMessageReceiver() {


            @Override
            public void receive(String s) {
                Log.d(LOG_TAG, "receive: " + s);
                Message message = messageParser.fromJson(s);
                messageReceiver.receive(message);
            }

            @Override
            public void notifyDisconnect(Exception e) {
                Log.d(LOG_TAG, "disconnect: " + e);
            }

        });

    }


    void reConnectClient() {
        clientThread = new Thread(() -> tcpClient.start());
        clientThread.start();
        startLogin();
    }

    private void startLogin() {
        Log.d(LOG_TAG, "startLogin: " + player);
        FcLoginMessage loginMessage = new FcLoginMessage(player);
        String json = messageParser.toJson(loginMessage);
        Log.d(LOG_TAG, "sending json to server: " + json);
        new Thread(() -> {
            try {
                Thread.sleep(LOGIN_DELAY_IN_MILLIS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            tcpClient.send(json);
            Thread.currentThread().interrupt();
        }).start();
    }

    public void start() {
        reConnectClient();
    }

    public void stop() {
        Log.d(LOG_TAG, "stop");
        tcpClient.close();
        clientThread.interrupt();
    }

    public void sendMessage(Message message) {
        String json = messageParser.toJson(message);
        Log.d(LOG_TAG, "sending json to server: " + json);
        new Thread(() -> {
            try {
                tcpClient.send(json);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                Log.d(LOG_TAG, "Exception on send: {}", e);
            }
        }).start();
    }


}
