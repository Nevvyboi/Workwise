package com.workwise.email;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

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
import com.workwise.models.verifyResetCodeIn;
import com.workwise.models.verifyResetCodeOut;
import com.workwise.network.apiClient;
import com.workwise.network.apiConfig;
import com.workwise.network.apiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class verifyResetCode extends AppCompatActivity {

    private ImageButton backButton;
    private TextView emailDisplay;
    private TextView timerText;
    private TextInputLayout codeLayout;
    private TextInputEditText inputCode;
    private MaterialButton verifyButton;
    private MaterialButton resendButton;
    
    private apiService api;
    private String email;
    private CountDownTimer countDownTimer;
    private boolean canResend = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verifyresetcode);

        api = apiClient.get().create(apiService.class);

        // Get email from intent
        email = getIntent().getStringExtra("email");
        if (email == null || email.isEmpty()) {
            finish();
            return;
        }

        bindViews();
        setupListeners();
        displayEmail();
        startTimer();
    }

    private void bindViews() {
        backButton = findViewById(R.id.backButton);
        emailDisplay = findViewById(R.id.emailDisplay);
        timerText = findViewById(R.id.timerText);
        codeLayout = findViewById(R.id.codeLayout);
        inputCode = findViewById(R.id.inputCode);
        verifyButton = findViewById(R.id.verifyButton);
        resendButton = findViewById(R.id.resendButton);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        verifyButton.setOnClickListener(v -> verifyCode());
        
        resendButton.setOnClickListener(v -> resendCode());
        
        // Auto-verify when 6 digits entered
        inputCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 6) {
                    verifyCode();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void displayEmail() {
        if (emailDisplay != null) {
            emailDisplay.setText("Code sent to " + maskEmail(email));
        }
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        if (localPart.length() <= 2) {
            return email;
        }
        
        String masked = localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1);
        return masked + "@" + domain;
    }

    private void startTimer() {
        canResend = false;
        if (resendButton != null) {
            resendButton.setEnabled(false);
            resendButton.setAlpha(0.5f);
        }
        
        // 15 minute countdown
        countDownTimer = new CountDownTimer(15 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                
                if (timerText != null) {
                    timerText.setText(String.format("Code expires in %02d:%02d", minutes, seconds));
                }
                
                // Enable resend after 1 minute
                if (millisUntilFinished < 14 * 60 * 1000 && !canResend) {
                    canResend = true;
                    if (resendButton != null) {
                        resendButton.setEnabled(true);
                        resendButton.setAlpha(1f);
                    }
                }
            }

            @Override
            public void onFinish() {
                if (timerText != null) {
                    timerText.setText("Code expired");
                    timerText.setTextColor(getColor(android.R.color.holo_red_dark));
                }
                canResend = true;
                if (resendButton != null) {
                    resendButton.setEnabled(true);
                    resendButton.setAlpha(1f);
                }
            }
        }.start();
    }

    private void verifyCode() {
        String code = text(inputCode);
        if (code.length() != 6) {
            codeLayout.setError("Please enter the 6-digit code");
            return;
        }

        setLoading(true);

        verifyResetCodeIn body = new verifyResetCodeIn(email, code);

        api.verifyResetCode(apiConfig.tokenVerifyResetCode, body)
                .enqueue(new Callback<verifyResetCodeOut>() {
                    @Override
                    public void onResponse(Call<verifyResetCodeOut> call,
                                           Response<verifyResetCodeOut> res) {
                        setLoading(false);

                        if (res.isSuccessful() && res.body() != null) {
                            if (res.body().valid) {
                                Intent intent = new Intent(verifyResetCode.this, resetPassword.class);
                                intent.putExtra("email", email);
                                intent.putExtra("code", code);
                                startActivity(intent);
                                finish();
                            } else {
                                codeLayout.setError("Invalid or expired code");
                            }
                        } else {
                            handleHttpError(res);
                        }
                    }

                    @Override
                    public void onFailure(Call<verifyResetCodeOut> call, Throwable t) {
                        setLoading(false);
                        showDialog("Network error", t.getMessage());
                    }
                });
    }

    private void resendCode() {
        if (!canResend) {
            return;
        }

        resendButton.setEnabled(false);
        resendButton.setText("Sending...");

        forgotPasswordIn body = new forgotPasswordIn(email);

        api.forgotPassword(body, apiConfig.tokenForgotPassword, "application/json")
                .enqueue(new Callback<forgotPasswordOut>() {
                    @Override
                    public void onResponse(Call<forgotPasswordOut> call, Response<forgotPasswordOut> res) {
                        resendButton.setText("Resend Code");
                        
                        if (res.isSuccessful()) {
                            showDialog("Code Sent", "A new verification code has been sent to your email.");
                            
                            // Restart timer
                            if (countDownTimer != null) {
                                countDownTimer.cancel();
                            }
                            startTimer();
                            
                            // Clear input
                            if (inputCode != null) {
                                inputCode.setText("");
                            }
                        } else {
                            resendButton.setEnabled(true);
                            handleHttpError(res);
                        }
                    }

                    @Override
                    public void onFailure(Call<forgotPasswordOut> call, Throwable t) {
                        resendButton.setText("Resend Code");
                        resendButton.setEnabled(true);
                        showDialog("Network error", t.getMessage());
                    }
                });
    }

    private void setLoading(boolean loading) {
        if (verifyButton != null) {
            verifyButton.setEnabled(!loading);
            verifyButton.setText(loading ? "Verifying..." : "Verify Code");
            verifyButton.setAlpha(loading ? 0.6f : 1f);
        }
    }

    private String text(TextInputEditText et) {
        return (et == null || et.getText() == null)
                ? ""
                : et.getText().toString().trim();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
