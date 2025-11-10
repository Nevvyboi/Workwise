package com.workwise.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

import com.workwise.R;
import com.workwise.community;
import com.workwise.home;
import com.workwise.setting;
import com.workwise.nearme;

public abstract class bottomNav extends baseNetworkCheck {

    private FrameLayout contentFrame;
    private String currentNavItem = "home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        View baseLayout = getLayoutInflater().inflate(R.layout.bottomnav, null);
        contentFrame = baseLayout.findViewById(R.id.contentFrame);

        getLayoutInflater().inflate(layoutResID, contentFrame, true);

        super.setContentView(baseLayout);

        setupBottomNavigation();

        setActiveNavItem(getCurrentNavItem());
    }

    protected abstract String getCurrentNavItem();

    private void setupBottomNavigation() {
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navNearMe = findViewById(R.id.navNearMe);
        LinearLayout navCommunity = findViewById(R.id.navCommunity);
        LinearLayout navSettings = findViewById(R.id.navSettings);

        if (navHome != null) {
            navHome.setOnClickListener(v -> navigateToPage("home", home.class));
        }

        if (navNearMe != null) {
            navNearMe.setOnClickListener(v -> navigateToPage("nearme", nearme.class));
        }

        if (navCommunity != null) {
            navCommunity.setOnClickListener(v -> navigateToPage("community", community.class));
        }

        if (navSettings != null) {
            navSettings.setOnClickListener(v -> navigateToPage("setting", setting.class));
        }
    }

    private void navigateToPage(String navItem, Class<?> activityClass) {
        if (currentNavItem.equals(navItem)) {
            return;
        }

        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);

        finish();

        overridePendingTransition(0, 0);
    }

    protected void setActiveNavItem(String item) {
        currentNavItem = item;

        int primaryColor = ContextCompat.getColor(this, R.color.colorPrimary);
        int inactiveColor = ContextCompat.getColor(this, android.R.color.darker_gray);

        ImageView homeIcon = findViewById(R.id.homeIcon);
        ImageView nearMeIcon = findViewById(R.id.nearMeIcon);
        ImageView communityIcon = findViewById(R.id.communityIcon);
        ImageView settingsIcon = findViewById(R.id.settingsIcon);

        TextView homeText = findViewById(R.id.homeText);
        TextView nearMeText = findViewById(R.id.nearMeText);
        TextView communityText = findViewById(R.id.communityText);
        TextView settingsText = findViewById(R.id.settingsText);

        if (homeIcon != null) homeIcon.setColorFilter(inactiveColor);
        if (nearMeIcon != null) nearMeIcon.setColorFilter(inactiveColor);
        if (communityIcon != null) communityIcon.setColorFilter(inactiveColor);
        if (settingsIcon != null) settingsIcon.setColorFilter(inactiveColor);

        if (homeText != null) {
            homeText.setTextColor(inactiveColor);
            homeText.setTypeface(null, Typeface.NORMAL);
        }
        if (nearMeText != null) {
            nearMeText.setTextColor(inactiveColor);
            nearMeText.setTypeface(null, Typeface.NORMAL);
        }
        if (communityText != null) {
            communityText.setTextColor(inactiveColor);
            communityText.setTypeface(null, Typeface.NORMAL);
        }
        if (settingsText != null) {
            settingsText.setTextColor(inactiveColor);
            settingsText.setTypeface(null, Typeface.NORMAL);
        }

        switch (item) {
            case "home":
                if (homeIcon != null) homeIcon.setColorFilter(primaryColor);
                if (homeText != null) {
                    homeText.setTextColor(primaryColor);
                    homeText.setTypeface(null, Typeface.BOLD);
                }
                break;
            case "nearme":
                if (nearMeIcon != null) nearMeIcon.setColorFilter(primaryColor);
                if (nearMeText != null) {
                    nearMeText.setTextColor(primaryColor);
                    nearMeText.setTypeface(null, Typeface.BOLD);
                }
                break;
            case "community":
                if (communityIcon != null) communityIcon.setColorFilter(primaryColor);
                if (communityText != null) {
                    communityText.setTextColor(primaryColor);
                    communityText.setTypeface(null, Typeface.BOLD);
                }
                break;
            case "setting":
                if (settingsIcon != null) settingsIcon.setColorFilter(primaryColor);
                if (settingsText != null) {
                    settingsText.setTextColor(primaryColor);
                    settingsText.setTypeface(null, Typeface.BOLD);
                }
                break;
        }
    }

    @Override
    protected void onNetworkRestored() {
        super.onNetworkRestored();
        // Show a toast when network is restored
        Toast.makeText(this, "✓ Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void handleNetworkLost() {
        super.handleNetworkLost();
        // Network overlay will show automatically
    }
}