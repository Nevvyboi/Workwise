package com.workwise.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.workwise.R;
import com.workwise.utils.networkMonitor;

public abstract class baseNetworkCheck extends AppCompatActivity {

    private networkMonitor networkMonitor;
    private View networkOverlay;
    private boolean wasDisconnected = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        networkMonitor = new networkMonitor(this, new networkMonitor.NetworkCallback() {
            @Override
            public void onNetworkAvailable() {
                runOnUiThread(() -> {
                    hideNetworkOverlay();
                    if (wasDisconnected) {
                        onNetworkRestored();
                        wasDisconnected = false;
                    }
                });
            }

            @Override
            public void onNetworkLost() {
                runOnUiThread(() -> {
                    showNetworkOverlay();
                    wasDisconnected = true;
                    onNetworkLost();
                });
            }
        });
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setupNetworkOverlay();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        setupNetworkOverlay();
    }

    private void setupNetworkOverlay() {
        ViewGroup rootView = findViewById(android.R.id.content);

        if (rootView != null && rootView.getChildCount() > 0) {
            LayoutInflater inflater = LayoutInflater.from(this);
            networkOverlay = inflater.inflate(R.layout.netorkloading, rootView, false);

            rootView.addView(networkOverlay);

            if (!networkMonitor.isNetworkAvailable()) {
                showNetworkOverlay();
            }
        }
    }

    private void showNetworkOverlay() {
        if (networkOverlay != null) {
            networkOverlay.setVisibility(View.VISIBLE);
            networkOverlay.setAlpha(0f);
            networkOverlay.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
        }
    }

    private void hideNetworkOverlay() {
        if (networkOverlay != null && networkOverlay.getVisibility() == View.VISIBLE) {
            networkOverlay.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> networkOverlay.setVisibility(View.GONE))
                    .start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (networkMonitor != null) {
            networkMonitor.startMonitoring();

            // Check current state
            if (!networkMonitor.isNetworkAvailable()) {
                showNetworkOverlay();
            } else {
                hideNetworkOverlay();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (networkMonitor != null) {
            networkMonitor.stopMonitoring();
        }
    }

    protected void onNetworkRestored() {
        // Override in child activities if needed
    }

    protected void onNetworkLost() {
        // Override in child activities if needed
    }

    protected boolean isNetworkAvailable() {
        return networkMonitor != null && networkMonitor.isNetworkAvailable();
    }
}