package com.github.martinfrank.chessapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.github.martinfrank.chessapp.databinding.ActivityMainBinding;
import com.github.martinfrank.games.chessmodel.model.Player;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "com.github.martinfrank.chessapp.MainActivity";
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private static final String INITIAL_UUID_STRING = "4918ab4d-87ac-488e-bc5f-a387f12563e6";
    public static final String SHARED_PREF_FILE_KEY = "com.github.martinfrank.chessapp.shared.pref";
    public static final String SHARED_PREF_PLAYER_ID = "com.github.martinfrank.chessapp.shared.pref.player.id";
    public static final String SHARED_PREF_PLAYER_NAME = "com.github.martinfrank.chessapp.shared.pref.player.name";
    public static final String SHARED_PREF_PLAYER_COLOR = "com.github.martinfrank.chessapp.shared.pref.player.color";
    public static final String BUNDLE_GAME_JSON = "com.github.martinfrank.chessapp.bundle.gameJson";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        createUniquePlayerId();
    }

    private void createUniquePlayerId() {
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF_FILE_KEY, Context.MODE_PRIVATE);

        String idString = sharedPref.getString(SHARED_PREF_PLAYER_ID, INITIAL_UUID_STRING);
        if(INITIAL_UUID_STRING.equals(idString)){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(MainActivity.SHARED_PREF_PLAYER_ID, UUID.randomUUID().toString());
            editor.apply();
        }

    }

    private Player loadPlayer() {
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF_FILE_KEY, Context.MODE_PRIVATE);
        String idString = sharedPref.getString(SHARED_PREF_PLAYER_ID, UUID.randomUUID().toString());
        String name = sharedPref.getString(SHARED_PREF_PLAYER_NAME, "Player");
        int color = sharedPref.getInt(SHARED_PREF_PLAYER_COLOR, 0xFF00FF);
        return new Player(UUID.fromString(idString), name, color);
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

    public Player getPlayer() {
        return loadPlayer();
    }

}