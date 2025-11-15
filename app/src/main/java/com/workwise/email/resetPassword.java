package com.workwise.email;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.workwise.R;
import com.workwise.authentication;
import com.workwise.models.resetPasswordIn;
import com.workwise.models.resetPasswordOut;
import com.workwise.network.apiClient;
import com.workwise.network.apiConfig;
import com.workwise.network.apiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class resetPassword extends AppCompatActivity {

    private ImageButton backButton;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;
    private TextInputEditText inputPassword;
    private TextInputEditText inputConfirmPassword;
    private MaterialButton resetButton;
    
    private apiService api;
    private String email;
    private String code;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resetpassword);

        try {
            api = apiClient.get().create(apiService.class);
        } catch (Exception e) {
            Log.e(TAG, "Error creating API service", e);
            Toast.makeText(this, "Error initializing API. Please restart.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        bindViews();
        setupListeners();
    }

    private void bindViews() {
        backButton = findViewById(R.id.backButton);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        resetButton = findViewById(R.id.resetButton);

        if (backButton == null) Log.e(TAG, "backButton not found");
        if (passwordLayout == null) Log.e(TAG, "passwordLayout not found");
        if (confirmPasswordLayout == null) Log.e(TAG, "confirmPasswordLayout not found");
        if (resetButton == null) Log.e(TAG, "resetButton not found");
    }

    private void setupListeners() {
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        if (resetButton != null) {
            resetButton.setOnClickListener(v -> resetPassword());
        }
    }

    private void resetPassword() {
        String password = text(inputPassword);
        String confirmPassword = text(inputConfirmPassword);

        // Clear previous errors
        if (passwordLayout != null) {
            passwordLayout.setError(null);
        }
        if (confirmPasswordLayout != null) {
            confirmPasswordLayout.setError(null);
        }

        // Validate password
        if (!isStrongPassword(password)) {
            if (passwordLayout != null) {
                passwordLayout.setError("Password must be at least 8 characters");
            }
            return;
        }

        // Validate passwords match
        if (!password.equals(confirmPassword)) {
            if (confirmPasswordLayout != null) {
                confirmPasswordLayout.setError("Passwords don't match");
            }
            return;
        }

        setLoading(true);

        resetPasswordIn body = new resetPasswordIn(email, code, password);

        if (api == null) {
            Toast.makeText(this, "API Error", Toast.LENGTH_SHORT).show();
            setLoading(false);
            return;
        }

        api.resetPassword(body, apiConfig.tokenResetPassword, "application/json")
                .enqueue(new Callback<resetPasswordOut>() {
                    @Override
                    public void onResponse(Call<resetPasswordOut> call, Response<resetPasswordOut> res) {
                        setLoading(false);
                        if (res.isSuccessful() && res.body() != null) {
                            resetPasswordOut data = res.body();
                            
                            if (data.success) {
                                // Show success dialog
                                new MaterialAlertDialogBuilder(resetPassword.this)
                                        .setTitle("Success!")
                                        .setMessage("Your password has been reset successfully. You can now log in with your new password.")
                                        .setPositiveButton("Go to Login", (dialog, which) -> {
                                            navigateToLogin();
                                        })
                                        .setCancelable(false)
                                        .show();
                            } else {
                                showDialog("Error", "Failed to reset password");
                            }
                            
                        } else {
                            handleHttpError(res);
                        }
                    }

                    @Override
                    public void onFailure(Call<resetPasswordOut> call, Throwable t) {
                        setLoading(false);
                        showDialog("Network error", t.getMessage());
                    }
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(resetPassword.this, authentication.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        if (resetButton != null) {
            resetButton.setEnabled(!loading);
            resetButton.setText(loading ? "Resetting..." : "Reset Password");
            resetButton.setAlpha(loading ? 0.6f : 1f);
        }
    }

    private String text(TextInputEditText et) {
        return (et == null || et.getText() == null)
                ? ""
                : et.getText().toString().trim();
    }

    private boolean isStrongPassword(String password) {
        return password != null && password.length() >= 8;
    }

    private void showDialog(String title, String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message == null ? "" : message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void handleHttpError(Response<?> response) {
        String errorMessage = "Unexpected error";

        try {
            if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                if (raw != null && !raw.isEmpty()) {
                    JsonObject obj = JsonParser.parseString(raw).getAsJsonObject();
                    if (obj.has("message")) {
                        errorMessage = obj.get("message").getAsString();
                    } else if (obj.has("detail")) {
                        errorMessage = obj.get("detail").getAsString();
                    }
                }
            }
        } catch (Exception ignored) {
        }

        showDialog("Error " + response.code(), errorMessage);
    }
}
