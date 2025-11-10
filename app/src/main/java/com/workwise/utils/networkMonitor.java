package com.workwise.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
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
    private boolean wasConnected = false;
    private final Handler handler;

    public networkMonitor(Context context, NetworkCallback callback) {
        this.context = context.getApplicationContext();
        this.callback = callback;
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.handler = new Handler(Looper.getMainLooper());
        this.wasConnected = isNetworkAvailable();
    }

    public void startMonitoring() {
        if (isMonitoring) return;

        // Check initial state
        final boolean initialState = isNetworkAvailable();
        wasConnected = initialState;

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                handler.post(() -> {
                    if (!wasConnected && callback != null) {
                        wasConnected = true;
                        callback.onNetworkAvailable();
                    }
                });
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                // Add a small delay to avoid false positives during network switching
                handler.postDelayed(() -> {
                    if (!isNetworkAvailable() && wasConnected && callback != null) {
                        wasConnected = false;
                        callback.onNetworkLost();
                    }
                }, 500);
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);

                boolean hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                boolean validated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);

                handler.post(() -> {
                    if (hasInternet && validated) {
                        if (!wasConnected && callback != null) {
                            wasConnected = true;
                            callback.onNetworkAvailable();
                        }
                    } else {
                        if (wasConnected && callback != null) {
                            wasConnected = false;
                            callback.onNetworkLost();
                        }
                    }
                });
            }
        };

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .build();

        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
            isMonitoring = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopMonitoring() {
        if (!isMonitoring || networkCallback == null) return;

        try {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }

        isMonitoring = false;
        handler.removeCallbacksAndMessages(null);
    }

    public boolean isNetworkAvailable() {
        if (connectivityManager == null) return false;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = connectivityManager.getActiveNetwork();
                if (network == null) return false;

                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                if (capabilities == null) return false;

                return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            } else {
                android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.isConnected();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isMonitoring() {
        return isMonitoring;
    }
}