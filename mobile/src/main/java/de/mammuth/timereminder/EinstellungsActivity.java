package de.mammuth.timereminder;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.CompoundButton;
import android.widget.Switch;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.*;

/**
 * Dies ist die Haupt-Activity der App. Diese wird gestartet, wenn wir die App auf dem Smartphone starten.
 */

public class EinstellungsActivity extends ActionBarActivity {

    // Deklariere den Switch, die wir im Layout der Acitivty (activity_einstellungen) festgelegt haben.
    private Switch switch_enable;

    // Keys / Flags, unter denen wir die Einstellungen in die DataAPI speichern.
    public final static String ENABLED_KEY = "enabled";

    private GoogleApiClient mGoogleApiClient; // Client zum Verbinden mit der DataAPI
    // Die Einstellungen für unsere 3 Buttons, der Übersichtlichkeit halber mal hier deklariert
    private boolean isEnabled, isHalbeStunde, isVolleStunde;

    // Unser ErinnerungsVerwalter
    private ErinnerungVerwalter erinnerung;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_einstellungen); // Setze unser Layout als ContentView für die Activity

        // Initialisiere den Switch
        switch_enable = (Switch) findViewById(R.id.swt_enable);

        // Initalisiere den GoogleAPIClient. Über den können wir auf die DataAPI zugreifen, die wir
        // für das Syncen der Einstellungen ("Sind Benachrichtigungen de-/aktiviert?") mit der Wear benötigen.
        // Initialisiere ApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        getEinstellungen(); // Setze den Switch, je nach gespeicherten Einstellungen

        // Initialisiere unseren ErinnerungVerwalter (aus der gleichnamigen Java-Klasse)
        erinnerung = new ErinnerungVerwalter(this);


        // Erstelle einen Listener für den Switch, um auf De-Aktivieren reagieren zu können.
        switch_enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            // Diese Methode wird aufgerufen, wenn du am Handy auf den Switch klickst
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                schreibeEinstellungen(isChecked);

                // Entferne alle Erinnerungen
                erinnerung.entferneErinnerung();

                // Rufe unseren Erinnerungsmanager auf und lassen ihn entscheiden was zu tun ist
                if (isChecked)
                    erinnerung.setzeErinnerung();


            }
        });


    }


    private void getEinstellungen() {
        // Frage Einstellungen mittels DataAPI ab
        PendingResult<DataItemBuffer> results = Wearable.DataApi.getDataItems(mGoogleApiClient);
        results.setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                if (dataItems.getCount() != 0) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItems.get(0));

                    isEnabled = dataMapItem.getDataMap().getBoolean(ENABLED_KEY);
                    switch_enable.setChecked(isEnabled); // Setze den Switch
                }

                dataItems.release();
            }
        });
    }

    private void schreibeEinstellungen(boolean enabled) {
        // Erstelle eine DataMap für die Einstellungen
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/EINSTELLUNGEN");
        // Schreibe die Einstellung via DataApi
        putDataMapReq.getDataMap().putBoolean(ENABLED_KEY, enabled);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

}
