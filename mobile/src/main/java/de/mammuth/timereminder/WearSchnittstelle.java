package de.mammuth.timereminder;

import android.widget.Toast;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.*;

/**
 * Created by Max Muth on 28. March 2015
 * kontakt@maxi-muth.de
 * <p/>
 * Diese Klasse empfängt Nachrichten, die die Uhr über die Messaging API schickt. Dies geschieht in unserem Fall
 * in der WearActivity (im wear Modul), wenn man in der App die Erinnerung aktiviert oder deaktiviert.
 */

public class WearSchnittstelle extends WearableListenerService {

    public final static String ENABLED_KEY = "enabled";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        // super.onMessageReceived(messageEvent);

        // Initialisiere unseren ErinnerungVerwalter (aus der gleichnamigen Java-Klasse)
        ErinnerungVerwalter erinnerung = new ErinnerungVerwalter(this);

        // Stelle fest, ob in der Uhr auf aktivieren oder deaktivieren gestellt wurde
        switch (messageEvent.getPath()) {
            case ("/START_ERINNERUNG"):
                Toast.makeText(this, "Erinnerung aktiviert", Toast.LENGTH_SHORT).show();
                schreibeEinstellungen(true);
                erinnerung.setzeErinnerung(); // Aktiviere die Erinnerung in unserem ErinnerungVerwalter
                break;
            case ("/ENTFERNE_ERINNERUNG"):
                erinnerung.entferneErinnerung(); // Lasse den Verwalter die Erinnerung entfernen
                Toast.makeText(this, "Erinnerung deaktiviert", Toast.LENGTH_SHORT).show();
                schreibeEinstellungen(false);
                break;
        }

    }

    private void schreibeEinstellungen(boolean enabled) {
        // Initialisiere ApiClient
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        // Erstelle eine DataMap für die Einstellungen
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/EINSTELLUNGEN");
        // Schreibe die Einstellung via DataApi
        putDataMapReq.getDataMap().putBoolean(ENABLED_KEY, enabled);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }
}