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

        binding.seekBarRed.setMax(0xff);
        binding.seekBarRed.setMin(0);
        binding.seekBarGreen.setMax(0xff);
        binding.seekBarGreen.setMin(0);
        binding.seekBarBlue.setMax(0xff);
        binding.seekBarBlue.setMin(0);

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

        int color = calculateArgb();
        color = color & 0xFFFFFF;

        editor.putString(MainActivity.SHARED_PREF_PLAYER_NAME, playerName);
        editor.putInt(MainActivity.SHARED_PREF_PLAYER_COLOR, color);
        editor.apply();
    }

    private void updateResult() {
        int argb = calculateArgb();
        Log.d(LOG_TAG, "argb: " + Integer.toHexString(argb));
        binding.buttonResultColor.setBackgroundColor(argb);
    }

    private int calculateArgb() {
        int rgb = ColorConverter.rgb(colorRed, colorGreen, colorBlue);
        return rgb + 0xFF000000;
    }

    private void loadFromPreferences() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(MainActivity.SHARED_PREF_FILE_KEY, Context.MODE_PRIVATE);
        binding.editTextPlayerName.setText(sharedPref.getString(MainActivity.SHARED_PREF_PLAYER_NAME, "Player"));
        binding.editTextPlayerId.setText(sharedPref.getString(MainActivity.SHARED_PREF_PLAYER_ID, UUID.randomUUID().toString()));
        int color = sharedPref.getInt(MainActivity.SHARED_PREF_PLAYER_COLOR, 0xFF00FF);
        colorRed = ColorConverter.red(color);
        colorGreen = ColorConverter.green(color);
        colorBlue = ColorConverter.blue(color);
        binding.seekBarRed.setProgress(colorRed);
        binding.seekBarGreen.setProgress(colorGreen);
        binding.seekBarBlue.setProgress(colorBlue);
    }

}