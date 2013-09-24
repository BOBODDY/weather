package com.boboddy.weather;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.boboddy.weather.util.LocalWeather;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.internal.ad;
import com.google.android.gms.location.LocationClient;
import android.location.Location;

import java.io.IOError;
import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener{

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    TextView mAddress;
    ProgressBar mProgressBar;

    LocationClient mLocationClient;
    Location mLastLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationClient = new LocationClient(this, this, this);

        mAddress = (TextView) findViewById(R.id.text);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);

    }

    public void onConnect(Bundle connectionHint) {
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT);

        mLastLocation = mLocationClient.getLastLocation();
    }

    protected void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    protected void onStop() {
        mLocationClient.disconnect();
        super.onStop();
    }

    public void getWeather(View v) {
        mProgressBar.setVisibility(View.VISIBLE);
        if(mLastLocation == null) {
            Log.d("weather", "Don't have location");
            Toast.makeText(this, "Don't have location", Toast.LENGTH_SHORT).show();
        }

        (new GetWeatherTask(this)).execute(mLastLocation);
    }

    public void getAddress(View v) {
        // Make sure the Geocoder exists
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
                && Geocoder.isPresent()) {
            mProgressBar.setVisibility(View.VISIBLE);
            /*
             * Reverse geocoding is long-running and synchronous.
             * Run it on a background thread.
             * Pass the current location to the background task.
             * When the task finishes,
             * onPostExecute() displays the address.
             */
            if(mLastLocation == null) {
                Log.e("weather", "Last location is null, uh oh");
                Toast.makeText(this, "Location is null", Toast.LENGTH_SHORT).show();
            } else {
                Log.i("weather", "Location: " + mLastLocation.toString());
            }

            (new GetAddressTask(this)).execute(mLastLocation);
        } else {
            Toast.makeText(this, "Geocoder not present", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                switch(resultCode) {
                    case Activity.RESULT_OK:
                        /* Try the connection again */
                        break;
                }
        }
    }

    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if(ConnectionResult.SUCCESS == resultCode) {
            Log.d("weather", "Connection ok");
            return true;
        } else {
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if(errorDialog != null) {
                errorDialog.show();
            }
            return false;
        }
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        mLastLocation = mLocationClient.getLastLocation();
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected. Please re-connect", Toast.LENGTH_SHORT).show();
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            //showErrorDialog(connectionResult.getErrorCode());
        }
    }

    private class GetWeatherTask extends AsyncTask<Location, Void, String> {
        Context mContext;
        public GetWeatherTask(Context c) {
            super();
            mContext = c;
        }

        protected String doInBackground(Location... locations) {
            Location loc = locations[0];
            String q = Double.toString(loc.getLatitude()) + "," + Double.toString(loc.getLongitude());

            LocalWeather lw = new LocalWeather();
            String query = (lw.new Params(lw.getKey())).setQ(q).getQueryString(LocalWeather.Params.class);
            LocalWeather.Data wCond = lw.callAPI(query);

            return wCond.getWeather().getWeatherDesc();
        }

        protected void onPostExecute(String weather) {
            //Make the progress bar go away
            mProgressBar.setVisibility(View.GONE);
            //Set the final result
            mAddress.setText(weather);
        }
    }

    private class GetAddressTask extends AsyncTask<Location, Void, String> {
        Context mContext;
        public GetAddressTask(Context c) {
            super();
            mContext = c;
        }

        protected String doInBackground(Location... locations) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            //Get the current location
            Location loc = locations[0];
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            } catch(IOException e1) {
                Log.e("weather", "IOException in getFromLocation");
                e1.printStackTrace();
                return "IO Exception trying to get address";
            } catch(IllegalArgumentException e2) {
                String errorString = "Illegal arguments " +
                        Double.toString(loc.getLatitude()) + ", " +
                        Double.toString(loc.getLongitude()) +
                        " passed to address service";
                Log.e("weather", errorString);
                e2.printStackTrace();
                return errorString;
            }

            //If the lookup returned an address
            if(addresses != null && addresses.size() > 0) {
                //Get the first address
                Address address = addresses.get(0);
                /*
                 * Format the first line of the address
                 */
                return String.format(
                        "%s, %s, %s",
                        //If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0): "",
                        address.getLocality(),
                        address.getCountryName());
            } else {
                return "Address not found";
            }
        }

        /**
         * A method that's called once doInBackground() completes. Turn
         * off the indeterminate activity indicator and set
         * the text of the UI element that shows the address. If the
         * lookup failed, display the error message.
         */
        @Override
        protected void onPostExecute(String address) {
            //Turn off the progress bar
            mProgressBar.setVisibility(View.GONE);
            //Set the text
            mAddress.setText(address);
        }
    }
}
