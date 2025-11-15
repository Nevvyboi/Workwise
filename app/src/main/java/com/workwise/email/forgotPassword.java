package com.workwise.email;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;

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
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        backToLoginButton.setOnClickListener(v -> finish());
        
        sendCodeButton.setOnClickListener(v -> sendResetCode());
    }

    private void sendResetCode() {
        String email = text(inputEmail);

        // Clear previous errors
        if (emailLayout != null) {
            emailLayout.setError(null);
        }

        // Validate email
        if (!isValidEmail(email)) {
            if (emailLayout != null) {
                emailLayout.setError("Please enter a valid email address");
            }
            return;
        }

        setLoading(true);

        forgotPasswordIn body = new forgotPasswordIn(email);

        api.forgotPassword(body, apiConfig.tokenForgotPassword, "application/json")
                .enqueue(new Callback<forgotPasswordOut>() {
                    @Override
                    public void onResponse(Call<forgotPasswordOut> call, Response<forgotPasswordOut> res) {
                        setLoading(false);
                        if (res.isSuccessful() && res.body() != null) {
                            forgotPasswordOut data = res.body();
                            
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
