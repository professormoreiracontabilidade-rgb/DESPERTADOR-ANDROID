package com.professormoreira.despertador;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS = "alarm_prefs";
    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_HOUR = "hour";
    public static final String KEY_MINUTE = "minute";
    public static final int ALARM_REQUEST_CODE = 1001;

    private TimePicker timePicker;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();
        requestNotificationPermissionIfNeeded();

        timePicker = findViewById(R.id.timePicker);
        statusText = findViewById(R.id.statusText);
        Button activateButton = findViewById(R.id.activateButton);
        Button cancelButton = findViewById(R.id.cancelButton);
        Button testButton = findViewById(R.id.testButton);

        timePicker.setIs24HourView(true);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        int savedHour = prefs.getInt(KEY_HOUR, 5);
        int savedMinute = prefs.getInt(KEY_MINUTE, 0);
        timePicker.setHour(savedHour);
        timePicker.setMinute(savedMinute);
        updateStatus();

        activateButton.setOnClickListener(v -> scheduleAlarm(true));
        cancelButton.setOnClickListener(v -> cancelAlarm());
        testButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AlarmActivity.class);
            intent.putExtra("test", true);
            startActivity(intent);
        });
    }

    private void scheduleAlarm(boolean fromUser) {
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            if (fromUser) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                Toast.makeText(this,
                        "Autorize 'Alarmes e lembretes' e depois toque novamente em Ativar alarme.",
                        Toast.LENGTH_LONG).show();
            }
            return;
        }

        Calendar now = Calendar.getInstance();
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, hour);
        next.set(Calendar.MINUTE, minute);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() <= now.getTimeInMillis()) {
            next.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent receiverIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, ALARM_REQUEST_CODE, receiverIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(
                next.getTimeInMillis(),
                PendingIntent.getActivity(
                        this, 2001,
                        new Intent(this, MainActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                )
        );
        alarmManager.setAlarmClock(info, pendingIntent);

        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putBoolean(KEY_ENABLED, true)
                .putInt(KEY_HOUR, hour)
                .putInt(KEY_MINUTE, minute)
                .apply();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM 'às' HH:mm", new Locale("pt", "BR"));
        statusText.setText("✅ Próximo alarme: " + sdf.format(next.getTime()));
        Toast.makeText(this, "Alarme ativado.", Toast.LENGTH_SHORT).show();
    }

    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent receiverIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, ALARM_REQUEST_CODE, receiverIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putBoolean(KEY_ENABLED, false)
                .apply();
        updateStatus();
        Toast.makeText(this, "Alarme desativado.", Toast.LENGTH_SHORT).show();
    }

    public static void scheduleNextDay(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_ENABLED, false)) return;

        int hour = prefs.getInt(KEY_HOUR, 5);
        int minute = prefs.getInt(KEY_MINUTE, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) return;

        Calendar next = Calendar.getInstance();
        next.add(Calendar.DAY_OF_YEAR, 1);
        next.set(Calendar.HOUR_OF_DAY, hour);
        next.set(Calendar.MINUTE, minute);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);

        Intent receiverIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, ALARM_REQUEST_CODE, receiverIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(
                next.getTimeInMillis(),
                PendingIntent.getActivity(
                        context, 2001,
                        new Intent(context, MainActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                )
        );
        alarmManager.setAlarmClock(info, pendingIntent);
    }

    private void updateStatus() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean enabled = prefs.getBoolean(KEY_ENABLED, false);
        if (enabled) {
            statusText.setText(String.format(Locale.getDefault(),
                    "✅ Alarme diário ativado para %02d:%02d",
                    prefs.getInt(KEY_HOUR, 5),
                    prefs.getInt(KEY_MINUTE, 0)));
        } else {
            statusText.setText("Alarme desativado");
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            NotificationChannel channel = new NotificationChannel(
                    AlarmReceiver.CHANNEL_ID,
                    "Alarmes",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Alarmes do Despertador Professor Moreira");
            channel.enableVibration(true);
            channel.setBypassDnd(true);
            manager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    3001
            );
        }
    }
}
