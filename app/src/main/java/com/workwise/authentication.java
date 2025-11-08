package com.workwise;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.workwise.models.loginIn;
import com.workwise.models.loginOut;
import com.workwise.models.registerIn;
import com.workwise.models.registerOut;
import com.workwise.network.apiClient;
import com.workwise.network.apiConfig;
import com.workwise.network.apiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class authentication extends AppCompatActivity {

    // SharedPreferences
    private static final String PREFS_NAME = "WorkWisePrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";

    // UI
    private MaterialButtonToggleGroup modeToggle;
    private View loginOptionsRow;
    private MaterialButton primaryAction;
    private TextInputLayout confirmPasswordLayout;
    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;
    private TextInputEditText inputConfirmPassword;

    // API
    private apiService api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authentication);

        api = apiClient.get().create(apiService.class);

        bindViews();

        // default = login
        modeToggle.check(R.id.btnLogin);
        applyAuthMode(false);

        modeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            boolean signUp = (checkedId == R.id.btnSignUp);
            applyAuthMode(signUp);
        });

        primaryAction.setOnClickListener(v -> {
            boolean signUp = (modeToggle.getCheckedButtonId() == R.id.btnSignUp);
            if (signUp) {
                doSignUp();
            } else {
                doLogin();
            }
        });
    }

    private void bindViews() {
        modeToggle = findViewById(R.id.modeToggle);
        loginOptionsRow = findViewById(R.id.loginOptionsRow);
        primaryAction = findViewById(R.id.primaryAction);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
    }

    private void applyAuthMode(boolean signUp) {
        if (confirmPasswordLayout != null) {
            confirmPasswordLayout.setVisibility(signUp ? View.VISIBLE : View.GONE);
            confirmPasswordLayout.setError(null);
        }

        if (loginOptionsRow != null) {
            loginOptionsRow.setVisibility(signUp ? View.GONE : View.VISIBLE);
        }

        if (primaryAction != null) {
            primaryAction.setText(signUp ? R.string.signUp : R.string.login);
        }

        if (inputEmail != null) inputEmail.setError(null);
        if (inputPassword != null) inputPassword.setError(null);
        if (inputConfirmPassword != null) inputConfirmPassword.setError(null);
    }

    // ---------- SharedPreferences helpers ----------

    private void saveUserSession(int userId, String username, String email) {
        if (userId <= 0) return;

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, username);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    // ---------- Sign Up ----------

    private void doSignUp() {
        String email = text(inputEmail);
        String password = text(inputPassword);
        String confirm = text(inputConfirmPassword);

        if (!isValidEmail(email)) {
            showDialog("Invalid email", "Please enter a valid email address.");
            return;
        }
        if (!isStrongPassword(password)) {
            showDialog("Weak password", "Password must be at least 8 characters.");
            return;
        }
        if (!password.equals(confirm)) {
            showDialog("Passwords don't match", "Please re-enter your password.");
            return;
        }

        setLoading(true);

        String username = deriveUsernameFromEmail(email);
        registerIn body = new registerIn(username, email, password);

        api.register(body, apiConfig.tokenRegister, "application/json")
                .enqueue(new Callback<registerOut>() {
                    @Override
                    public void onResponse(Call<registerOut> call, Response<registerOut> res) {
                        setLoading(false);
                        if (res.isSuccessful() && res.body() != null) {
                            registerOut data = res.body();
                            saveUserSession(data.userId, data.username, data.email);
                            navigateToHome();
                        } else {
                            handleHttpError(res);
                        }
                    }

                    @Override
                    public void onFailure(Call<registerOut> call, Throwable t) {
                        setLoading(false);
                        showDialog("Network error", t.getMessage());
                    }
                });
    }

    // ---------- Login ----------

    private void doLogin() {
        String usernameOrEmail = text(inputEmail);
        String password = text(inputPassword);

        if (usernameOrEmail.isEmpty()) {
            showDialog("Login required", "Please enter your email or username.");
            return;
        }
        if (password.isEmpty()) {
            showDialog("Password required", "Please enter your password.");
            return;
        }

        setLoading(true);

        loginIn body = new loginIn(usernameOrEmail, password);

        api.login(body, apiConfig.tokenLogin, "application/json")
                .enqueue(new Callback<loginOut>() {
                    @Override
                    public void onResponse(Call<loginOut> call, Response<loginOut> res) {
                        setLoading(false);
                        if (res.isSuccessful() && res.body() != null) {
                            loginOut data = res.body();
                            saveUserSession(data.userId, data.username, data.email);
                            navigateToHome();
                        } else {
                            handleHttpError(res);
                        }
                    }

                    @Override
                    public void onFailure(Call<loginOut> call, Throwable t) {
                        setLoading(false);
                        showDialog("Network error", t.getMessage());
                    }
                });
    }

    // ---------- Utils ----------

    private void navigateToHome() {
        Intent intent = new Intent(authentication.this, home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        if (primaryAction != null) {
            primaryAction.setEnabled(!loading);
            primaryAction.setAlpha(loading ? 0.6f : 1f);
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

    private boolean isStrongPassword(String password) {
        return password != null && password.length() >= 8;
    }

    private String deriveUsernameFromEmail(String email) {
        if (email == null) return "";
        int at = email.indexOf('@');
        return (at > 0) ? email.substring(0, at) : email.replaceAll("[^a-zA-Z0-9]", "_");
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
                    JsonElement element = JsonParser.parseString(raw);
                    if (element.isJsonObject()) {
                        JsonObject obj = element.getAsJsonObject();
                        if (obj.has("message")) {
                            errorMessage = obj.get("message").getAsString();
                        } else if (obj.has("detail")) {
                            errorMessage = obj.get("detail").getAsString();
                        }
                    } else {
                        errorMessage = raw;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        showDialog("Error " + response.code(), errorMessage);
    }
}
