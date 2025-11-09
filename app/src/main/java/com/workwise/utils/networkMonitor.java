package com.workwise.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import androidx.annotation.NonNull;

public class networkMonitor {

    public interface NetworkCallback {
        void onNetworkAvailable();
        void onNetworkLost();
    }

    private final Context context;
    private final NetworkCallback callback;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean isMonitoring = false;

    public networkMonitor(Context context, NetworkCallback callback) {
        this.context = context.getApplicationContext();
        this.callback = callback;
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public void startMonitoring() {
        if (isMonitoring) return;

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                if (callback != null) {
                    callback.onNetworkAvailable();
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                if (callback != null) {
                    callback.onNetworkLost();
                }
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                boolean hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                
                if (hasInternet && callback != null) {
                    callback.onNetworkAvailable();
                }
            }
        };

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        isMonitoring = true;
    }

    public void stopMonitoring() {
        if (!isMonitoring || networkCallback == null) return;

        try {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
        isMonitoring = false;
    }

    public boolean isNetworkAvailable() {
        if (connectivityManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && 
                   capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                   capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } else {
            android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }
}