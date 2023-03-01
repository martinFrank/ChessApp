package com.github.martinfrank.chessapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.github.martinfrank.chessapp.databinding.ActivityMainBinding;
import com.github.martinfrank.games.chessmodel.message.FcLoginMessage;
import com.github.martinfrank.games.chessmodel.message.Message;
import com.github.martinfrank.games.chessmodel.message.MessageParser;
import com.github.martinfrank.games.chessmodel.model.ModelParser;
import com.github.martinfrank.games.chessmodel.model.Player;
import com.github.martinfrank.tcpclientserver.ClientMessageReceiver;
import com.github.martinfrank.tcpclientserver.TcpClient;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "com.github.martinfrank.chessapp.MainActivity";
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    public static final String SHARED_PREF_FILE_KEY = "com.github.martinfrank.chessapp.shared.pref";
    public static final String SHARED_PREF_PLAYER_ID = "com.github.martinfrank.chessapp.shared.pref.player.id";
    public static final String SHARED_PREF_PLAYER_NAME = "com.github.martinfrank.chessapp.shared.pref.player.name";
    public static final String BUNDLE_GAME_JSON = "com.github.martinfrank.chessapp.bundle.gameJson";
    private static final int LOGIN_DELAY_IN_MILLIS = 500;
    private static final String CHESS_SERVER_ADRESS = "elitegames.chickenkiller.com";
    private static final int CHESS_SERVER_PORT = 8100;

    public TcpClient tcpClient;
    public MessageParser messageParser;

    public ModelParser modelParser;

    public Player player;
    private ChessMessageReceiver chessMessageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        player = createPlayer();

        messageParser = new MessageParser();
        modelParser = new ModelParser();

//        tcpClient = new TcpClient(CHESS_SERVER_ADRESS, CHESS_SERVER_PORT, new ClientMessageReceiver(){
        tcpClient = new TcpClient("192.168.0.65", CHESS_SERVER_PORT, new ClientMessageReceiver(){

            @Override
            public void receive(String s) {
                handleServerMessage(s);
            }

            @Override
            public void notifyDisconnect() {
                Log.d(LOG_TAG, "disconnect!");
//                if(chessMessageReceiver != null){
//                    chessMessageReceiver.notifyDisconnect();
//                }
//                reStartClient();
            }

        });

        reStartClient();

    }

    private void reStartClient() {
        new Thread(() -> tcpClient.start()).start();
        startLogin();
    }

    private void startLogin() {
        FcLoginMessage loginMessage = new FcLoginMessage(player);
        String json = messageParser.toJson(loginMessage);
        Log.d(LOG_TAG, "sending json to server: "+json);
        new Thread(() -> {
            try {
                Thread.sleep(LOGIN_DELAY_IN_MILLIS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            tcpClient.send(json);
        }).start();
    }

    private void handleServerMessage(String s) {
        Log.d(LOG_TAG, "receive: "+s);
        Message message = messageParser.fromJson(s);
        if(chessMessageReceiver != null){
            chessMessageReceiver.receive(message);
        }
    }

    private Player createPlayer() {
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF_FILE_KEY, Context.MODE_PRIVATE);
        String idString = sharedPref.getString(SHARED_PREF_PLAYER_ID, UUID.randomUUID().toString());
        String name = sharedPref.getString(SHARED_PREF_PLAYER_NAME, "Player");
        return new Player(UUID.fromString(idString), name);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    public void sendMessage(Message message) {
        String json = messageParser.toJson(message);
        Log.d(LOG_TAG, "sending json to server: "+json);
        new Thread(() -> tcpClient.send(json)).start();
    }

    public void setChessMessageReceiver(ChessMessageReceiver chessMessageReceiver){
        this.chessMessageReceiver = chessMessageReceiver;
    }
}