package de.mammuth.timereminder;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Max Muth on 10. March 2015
 * kontakt@maxi-muth.de
 * <p/>
 * Diese Klasse wird aufgerufen, sobald der AlarmManger von Android, den wir in ErinnerungVerwalter eingestellt haben
 * und verarbeitet diesen indem die Notification an die Geräte geschickt wird.
 */
public class ErinnerungReceiver extends BroadcastReceiver {

    private Context context;

    /*
     * Wird aufgerufen, wenn Androids AlarmManager unsere App aufweckt.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        sendeBenachrichtigung(context);
    }


    /*
     * Erstellt die Benachrichtigung, die an das Handy und Wear geschickt wird.
     */
    private void sendeBenachrichtigung(Context context) {

        // Bekomme die aktuelle Uhrzeit
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); // Definiere das Format als HH:mm, zB 15:42
        String time = sdf.format(new Date()); // Wende die Formatvorlage auf den aktuellen Zeitpunkt ( = new Date()) an

        int notificationId = 001; // Die Id, die unsere Notification eindeutig identifizieren soll
        // Öffne beim Klick auf die Benachrichtigung unsere EinstellungsActivity
        Intent viewIntent = new Intent(context, EinstellungsActivity.class);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(context, 0, viewIntent, 0);

        // Vibrationspattern
        long[] pattern = {0, 200, 100, 200, 100, 200, 100, 200};

        // Erstelle die Notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Zeit Erinnerung")
                        .setContentText("Es ist " + time + " Uhr.") // Schreibe die aktuelle Uhrzeit in die Beschreibung
                        .setDefaults(NotificationCompat.DEFAULT_ALL) // Somit wird auch die Uhr aufgeweckt bei der Notification
                        .setContentIntent(viewPendingIntent)
                        .setVibrate(pattern);

        // Sende die Notification an das System. Dieses verteilt es automatisch an das Smartphone und Android Wear
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }


}
