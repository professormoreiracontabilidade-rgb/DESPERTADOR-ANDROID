package com.professormoreira.despertador;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        SharedPreferences prefs = context.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(MainActivity.KEY_ENABLED, false)) return;

        // Após reiniciar, agenda para o próximo horário configurado.
        int hour = prefs.getInt(MainActivity.KEY_HOUR, 5);
        int minute = prefs.getInt(MainActivity.KEY_MINUTE, 0);

        android.app.AlarmManager alarmManager =
                (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
                && !alarmManager.canScheduleExactAlarms()) return;

        java.util.Calendar next = java.util.Calendar.getInstance();
        next.set(java.util.Calendar.HOUR_OF_DAY, hour);
        next.set(java.util.Calendar.MINUTE, minute);
        next.set(java.util.Calendar.SECOND, 0);
        next.set(java.util.Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() <= System.currentTimeMillis()) {
            next.add(java.util.Calendar.DAY_OF_YEAR, 1);
        }

        Intent receiverIntent = new Intent(context, AlarmReceiver.class);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                context, MainActivity.ALARM_REQUEST_CODE, receiverIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
        );

        android.app.AlarmManager.AlarmClockInfo info =
                new android.app.AlarmManager.AlarmClockInfo(
                        next.getTimeInMillis(),
                        android.app.PendingIntent.getActivity(
                                context, 2001,
                                new Intent(context, MainActivity.class),
                                android.app.PendingIntent.FLAG_UPDATE_CURRENT |
                                        android.app.PendingIntent.FLAG_IMMUTABLE
                        )
                );

        alarmManager.setAlarmClock(info, pendingIntent);
    }
}
