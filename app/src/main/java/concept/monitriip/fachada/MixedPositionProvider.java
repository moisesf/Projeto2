/*
 * Copyright 2015 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package concept.monitriip.fachada;

import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class MixedPositionProvider extends PositionProvider implements LocationListener, GpsStatus.Listener, GpsStatus.NmeaListener {

    private static int FIX_TIMEOUT = 30 * 1000;

    private LocationListener backupListener;
    private long lastFixTime;
    private static final String GPGSA = "$GPGSA";
    private static final String NMEA_SEP = ",";
    private String pdop;

    public MixedPositionProvider(Context context, PositionListener listener) {
        super(context, listener);
    }

    public void startUpdates() {
        lastFixTime = System.currentTimeMillis();
        locationManager.addGpsStatusListener(this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, period, 0, this);
        locationManager.addNmeaListener(this);
    }

    public void stopUpdates() {
        locationManager.removeUpdates(this);
        locationManager.removeGpsStatusListener(this);
        locationManager.removeNmeaListener(this);
    }



    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "provider location");
//        stopBackupProvider();
        lastFixTime = System.currentTimeMillis();
        updateLocation(location, pdop);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, "provider enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, "provider disabled");
        updateLocationLast();
    }

    @Override
    public void onGpsStatusChanged(int event) {
        if (backupListener == null && System.currentTimeMillis() - (lastFixTime + period) > FIX_TIMEOUT) {
//            startBackupProvider();
        }
    }

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        if (nmea.startsWith(GPGSA)) {
            int checksumIndex = nmea.lastIndexOf("*");
            String[] values;
            if (checksumIndex > 0) {
                values = nmea.substring(0, checksumIndex).split(NMEA_SEP);
            } else {
                return;
            }

            if (values.length > 15) {
                pdop = values[15];
                Log.d("NMEA", "PDOP = " + pdop);
            }
        }
    }
}
