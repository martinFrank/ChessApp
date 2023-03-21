package com.github.martinfrank.chessapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.github.martinfrank.chessapp.databinding.FragmentSettingsBinding;
import com.github.martinfrank.games.chessmodel.model.Player;

import java.util.UUID;

public class SettingsFragment extends Fragment {

    private static final String LOG_TAG = "com.github.martinfrank.chessapp.SettingsFragment";

    private FragmentSettingsBinding binding;

    private int colorRed;
    private int colorGreen;
    private int colorBlue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonBack.setOnClickListener(button -> {
                    savePreferences();
                    NavHostFragment.findNavController(SettingsFragment.this)
                            .navigate(R.id.action_SettingsFragment_to_StartFragment);
                }
        );

        binding.editTextPlayerId.setEnabled(false);

        binding.seekBarRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                colorRed = seekBar.getProgress();
                updateResult();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.seekBarGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                colorGreen = seekBar.getProgress();
                updateResult();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.seekBarBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                colorBlue = seekBar.getProgress();
                updateResult();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        loadFromPreferences();
    }

    private void savePreferences() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(MainActivity.SHARED_PREF_FILE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        String playerName = binding.editTextPlayerName.getText().toString();
        if(playerName.isEmpty() || playerName.length() < 4){
            playerName = "Player";
        }

        int color = caclucateArgb();
        color = color & 0xFFFFFF;

        editor.putString(MainActivity.SHARED_PREF_PLAYER_NAME, playerName);
        editor.putInt(MainActivity.SHARED_PREF_PLAYER_COLOR, color);
        editor.apply();
    }

    private void updateResult() {
        int rgb = caclucateArgb();
        Log.d(LOG_TAG, "argb: " + Integer.toHexString(rgb));
        binding.buttonResultColor.setBackgroundColor(rgb);
    }

    private int caclucateArgb() {
        int r = (colorRed << 16);
        int g = (colorGreen << 8);
        int b = colorBlue;
        Log.d(LOG_TAG, "r: " + Integer.toHexString(r));
        Log.d(LOG_TAG, "g: " + Integer.toHexString(g));
        Log.d(LOG_TAG, "b: " + Integer.toHexString(b));
        return r + g + b + 0xFF000000;
    }

    private void loadFromPreferences() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(MainActivity.SHARED_PREF_FILE_KEY, Context.MODE_PRIVATE);
        binding.editTextPlayerName.setText(sharedPref.getString(MainActivity.SHARED_PREF_PLAYER_NAME, "Player"));
        binding.editTextPlayerId.setText(sharedPref.getString(MainActivity.SHARED_PREF_PLAYER_ID, UUID.randomUUID().toString()));
        int color = sharedPref.getInt(MainActivity.SHARED_PREF_PLAYER_COLOR, 0xFF00FF);
        colorRed = (0xFF0000 & color) >> 16;
        colorGreen = (0x00FF00 & color) >> 8;
        colorBlue = (0x0000FF & color);
        Log.d(LOG_TAG, "color: " + Integer.toHexString(color));
        Log.d(LOG_TAG, "colorRed: " + Integer.toHexString(colorRed));
        Log.d(LOG_TAG, "colorGreen: " + Integer.toHexString(colorGreen));
        Log.d(LOG_TAG, "colorBlue: " + Integer.toHexString(colorBlue));
        binding.seekBarRed.setProgress(colorRed);
        binding.seekBarRed.setMax(0xff);
        binding.seekBarRed.setMin(0);
        binding.seekBarGreen.setProgress(colorGreen);
        binding.seekBarGreen.setMax(0xff);
        binding.seekBarGreen.setMin(0);
        binding.seekBarBlue.setProgress(colorBlue);
        binding.seekBarBlue.setMax(0xff);
        binding.seekBarBlue.setMin(0);
    }

}