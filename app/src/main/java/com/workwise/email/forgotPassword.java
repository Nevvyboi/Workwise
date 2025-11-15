package com.workwise.email;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
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
import com.workwise.models.forgotPasswordIn;
import com.workwise.models.forgotPasswordOut;
import com.workwise.network.apiClient;
import com.workwise.network.apiConfig;
import com.workwise.network.apiService;
import com.workwise.email.verifyResetCode;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class forgotPassword extends AppCompatActivity {

    private static final String TAG = "ForgotPassword";

    private ImageButton backButton;
    private TextInputLayout emailLayout;
    private TextInputEditText inputEmail;
    private MaterialButton sendCodeButton;
    private MaterialButton backToLoginButton;
    
    private apiService api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotpassword);

        try {
            api = apiClient.get().create(apiService.class);
        } catch (Exception e) {
            Log.e(TAG, "Error creating API service", e);
            Toast.makeText(this, "Error initializing API. Please restart.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        api = apiClient.get().create(apiService.class);

        bindViews();
        setupListeners();
    }

    private void bindViews() {
        backButton = findViewById(R.id.backButton);
        emailLayout = findViewById(R.id.emailLayout);
        inputEmail = findViewById(R.id.inputEmail);
        sendCodeButton = findViewById(R.id.sendCodeButton);
        backToLoginButton = findViewById(R.id.backToLoginButton);

        if (backButton == null) Log.e(TAG, "backButton not found");
        if (emailLayout == null) Log.e(TAG, "emailLayout not found");
        if (sendCodeButton == null) Log.e(TAG, "sendCodeButton not found");
        if (backToLoginButton == null) Log.e(TAG, "backToLoginButton not found");
    }

    private void setupListeners() {
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        if (backToLoginButton != null) {
            backToLoginButton.setOnClickListener(v -> finish());
        }
        if (sendCodeButton != null) {
            sendCodeButton.setOnClickListener(v -> sendResetCode());
        }
    }

    private void sendResetCode() {
        String email = text(inputEmail);

        if (emailLayout != null) {
            emailLayout.setError(null);
        }

        if (!isValidEmail(email)) {
            if (emailLayout != null) {
                emailLayout.setError("Please enter a valid email address");
            }
            return;
        }

        setLoading(true);

        forgotPasswordIn body = new forgotPasswordIn(email);

        if (api == null) {
            Toast.makeText(this, "API Error", Toast.LENGTH_SHORT).show();
            setLoading(false);
            return;
        }

        api.forgotPassword(body, apiConfig.tokenForgotPassword, "application/json")
                .enqueue(new Callback<forgotPasswordOut>() {
                    @Override
                    public void onResponse(Call<forgotPasswordOut> call, Response<forgotPasswordOut> res) {
                        setLoading(false);
                        if (isFinishing() || isDestroyed()) return;

                        if (res.isSuccessful() && res.body() != null) {
                            // Navigate to verify code screen
                            Intent intent = new Intent(forgotPassword.this, verifyResetCode.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                            finish();
                        } else {
                            handleHttpError(res);
                        }
                    }

                    @Override
                    public void onFailure(Call<forgotPasswordOut> call, Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        setLoading(false);
                        showDialog("Network error", t.getMessage());
                    }
                });
    }

    private void setLoading(boolean loading) {
        if (sendCodeButton != null) {
            sendCodeButton.setEnabled(!loading);
            sendCodeButton.setText(loading ? "Sending..." : "Send Code");
            sendCodeButton.setAlpha(loading ? 0.6f : 1f);
        }
        if (backToLoginButton != null) {
            backToLoginButton.setEnabled(!loading);
        }
    }

    private String text(TextInputEditText et) {
        return (et == null || et.getText() == null)
                ? ""
                : et.getText().toString().trim();
    }

    private boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
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
