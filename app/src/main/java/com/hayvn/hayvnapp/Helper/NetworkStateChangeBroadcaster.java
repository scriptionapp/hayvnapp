package com.hayvn.hayvnapp.Helper;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import static android.content.Context.CONNECTIVITY_SERVICE;

public abstract class NetworkStateChangeBroadcaster {//extends BroadcastReceiver {

    private static ConnectivityManager connectivityManager;
    private static ConnectivityManager.NetworkCallback ncb;
    private static Context context;
    private static boolean send_broadcast = false;
    private static boolean is_network_connected = true;
    private static boolean is_network_metered = true;


    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void launchNetworkBroadcaster(Context context_) {
        if (connectivityManager == null) {
            context = context_;
            connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
            update_connected();
            scanAndSend();
            monitorNetworks();
        }
    }

    public static void killMonitor(){
        if(ncb != null && connectivityManager != null){
            connectivityManager.unregisterNetworkCallback(ncb);
        }
    }

    public static boolean getIsConnected(){
        if (connectivityManager != null) {
            return is_network_connected;
        }else{
            Log.d(TAG, "Connectivity manager not live");
            return false;
        }
    }

    public static boolean getIsMetered(){
        if (connectivityManager != null) {
            return is_network_metered;
        }else{
            return false;
        }
    }

    public static final String IS_NETWORK_AVAILABLE = "isNetworkAvailable";
    public static final String IS_NETWORK_METERED = "isNetworkMetered";
    public static final String CONNECTIVITY_ACTION = "detectChanges";
    final static String TAG = "NETWORK_CHANGE";

    private static void monitorNetworks(){
        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        connectivityManager.registerNetworkCallback(
            builder.build(),
            ncb = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    update_connected();
                    scanAndSend();
                }

                @Override
                public void onLost(Network network) {
                    update_connected();
                    scanAndSend();
                }

                @Override
                public void onUnavailable(){
                    update_connected();
                    scanAndSend();
                }

                @Override
                public void onCapabilitiesChanged (Network network,
                                                   NetworkCapabilities networkCapabilities){
                    boolean metered = !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
                    update_metered(metered);
                    sendMetered(metered);
                }
            }
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static void update_connected(){
        if(connectivityManager.getActiveNetwork()==null) is_network_connected = false;
        else is_network_connected = true;
    }
    private static void update_metered(boolean val){
        is_network_metered = val;
    }
    private static void scanAndSend(){
        if(send_broadcast)   sendIntent(IS_NETWORK_AVAILABLE, is_network_connected);
    }

    private static void sendMetered(boolean metered) {
        if(send_broadcast)  sendIntent(IS_NETWORK_METERED, is_network_metered);
    }

    private static void sendIntent(String val_name, boolean val) {
        Intent intent = new Intent();
        intent.setAction(CONNECTIVITY_ACTION);
        intent.putExtra(val_name, val);
        context.sendBroadcast(intent);
    }

}

