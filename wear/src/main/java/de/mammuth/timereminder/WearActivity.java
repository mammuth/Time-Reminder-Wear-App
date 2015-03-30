package de.mammuth.timereminder;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.CompoundButton;
import android.widget.Switch;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.*;

import java.util.List;

public class WearActivity extends Activity {

    private GoogleApiClient mGoogleApiClient;
    private Switch toggleButton;

    public final static String ENABLED_KEY = "enabled";

    private boolean isEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);

        // Initialisiere ApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();


        // WatchViewStub wählt automatisch das passende Layout, je nachdem ob es eine runde oder rechteckige Uhr ist
        // Geladen wird dann eine der Layout-Dateien rect_activity_wear.xml oder round_activity_wear.xml
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                // Wenn das Layout geladen wird, initalisieren wir den Button
                toggleButton = (Switch) stub.findViewById(R.id.swt_enable);

                getEinstellungen(); // Frage Einstellungen ab und setze den Switch dementsprechend

                // Setze Listener auf den Button
                toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(final CompoundButton compoundButton, boolean b) {
                        if (mGoogleApiClient == null)
                            return;

                        // Erstelle eine Message, sodass unsere WearSchnittstelle auf dem Handy die Erinnerung de-/aktivieren kann
                        final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
                        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                            @Override
                            public void onResult(NodeApi.GetConnectedNodesResult result) {
                                final List<Node> nodes = result.getNodes();
                                if (nodes != null) {
                                    for (int i = 0; i < nodes.size(); i++) {
                                        final Node node = nodes.get(i);

                                        // Sende die Nachricht an die App auf dem Handy, dass der Alarm geändert werden soll
                                        if (compoundButton.isChecked())
                                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/START_ERINNERUNG", null);
                                        else
                                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/ENTFERNE_ERINNERUNG", null);
                                        // Das geschieht über die Message API, die Google mit Android Wear eingeführt hat.
                                        // Entgegen genommen wird diese Nachricht in dem mobile Modul in der Klasse "WearSchnittstelle"
                                        // Hier könnte man auch direkt einbauen, dass wenn die App auf beiden Geräten geöffnet ist,
                                        // der Status der Buttons in Echtzeit geändert wird. (Das passiert aktuell ja nur, wenn
                                        // die App beendet und wieder gestartet wird
                                    }
                                }
                            }
                        });

                        // Schreibe noch die Einstellungen der Änderung
                        schreibeEinstellungen(compoundButton.isChecked());
                    }
                });
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
                    toggleButton.setChecked(isEnabled); // Setze den Switch
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
