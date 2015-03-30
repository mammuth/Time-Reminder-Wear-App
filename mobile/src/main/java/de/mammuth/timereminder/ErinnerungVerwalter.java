package de.mammuth.timereminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Max Muth on 10. March 2015
 * kontakt@maxi-muth.de
 * <p/>
 * Diese Klasse dient als Schnittstelle zur Verwaltung Android AlarmManager. Hier wird die Erinnerung an das System
 * "in Auftrag gegeben" oder wieder gelöscht.
 */
public class ErinnerungVerwalter {

    private Context context;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private int erinnerungsIntervall;


    // Klassen Konstruktor
    public ErinnerungVerwalter(Context c) {
        this.context = c;

    }

    public void setzeErinnerung() {

        erinnerungsIntervall = 60;

        // Der Android AlarmManager ist eine System Komponente, die unsere App zu der von uns definierten Uhrzeit
        // "wecken" kann. Das ist NICHT der eigentliche Alarm/Notification, sondern ruft nur die Klasse "ErinnerungsReceiver" auf!
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ErinnerungReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // Nutze unsere Methode um den ersten Erinnerungszeitpunkt zu bekommen.
        Date ersteErinnerung = getErstenAlarm();
        // Toast Hinweis:
        Toast.makeText(context, "Erste Erinnerung kommt " + ersteErinnerung.getHours() + ":" + ersteErinnerung.getMinutes(), Toast.LENGTH_SHORT).show();
        calendar.set(Calendar.HOUR_OF_DAY, ersteErinnerung.getHours());
        calendar.set(Calendar.MINUTE, ersteErinnerung.getMinutes());

        // setRepeating() sorgt dafür, dass dieser Alarm wiederholt aufgerufen wird
        // Der Parameter ist in Millisekunden, daher 1000*60 und dann multipliziert mit unserem Intervall (30/60)
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000 * 60 * erinnerungsIntervall, alarmIntent);


        // alle 2000 sekunden zum testen [todo]
       /* alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                1000,
                10000, alarmIntent); */
    }

    public void entferneErinnerung() {
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }
    }

    /**
     * Diese Methode errechnet den ERSTEN Zeitpunkt, wann unsere Erinnerung starten soll. zB. ist gerade 14:05, dann
     * soll die erste Erinnerung um 15:00 kommen.
     */
    private Date getErstenAlarm() {
        Date zeit = new Date();

        // Bei stündlicher Erinnerung einfach die nächste Stunde nehmen
        zeit.setHours(zeit.getHours() + 1);
        zeit.setMinutes(0);


        return zeit;
    }

    /*
     * Wenn man noch die Option für halbstündliche Erinnerungen hinzugefügt hat, wäre diese Methode hilfreich
     */
   /* private Date getErstenAlarm() {
        Date zeit = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); // Definiere das Format als HH:mm, zB 15:42

        // Wenn das Intervall 30 Minuten ist, und die Minutenanzahl unter 30, dann ist der nächste Alarm zu einer halben Stunde
        if (erinnerungsIntervall == 30) {
            if (zeit.getMinutes() < 30) {
                zeit.setMinutes(30); // Mache zB. 15:23 zu 15:30
            } else {
                zeit.setHours(zeit.getHours() + 1);
                zeit.setMinutes(0);
            }
        } else if (erinnerungsIntervall == 60) {
            // Bei stündlicher Erinnerung einfach die nächste Stunde nehmen
            zeit.setHours(zeit.getHours() + 1);
            zeit.setMinutes(0);
        }

        return zeit;
    } */

}
