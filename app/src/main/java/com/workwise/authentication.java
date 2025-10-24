package com.workwise;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView; // Import ImageView
import android.widget.LinearLayout;

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

    private MaterialButtonToggleGroup modeToggle;
    private View loginOptionsRow;
    private MaterialButton primaryAction;
    private TextInputLayout confirmPasswordLayout;
    private TextInputEditText inputEmail, inputPassword, inputConfirmPassword;

    private apiService api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authentication);

        api = apiClient.get().create(apiService.class);
        bindViews();

        modeToggle.check(R.id.btnLogin);
        applyAuthMode(false);

        modeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            boolean signUp = (checkedId == R.id.btnSignUp);
            applyAuthMode(signUp);
        });

        primaryAction.setOnClickListener(v -> {
            boolean signUp = (modeToggle.getCheckedButtonId() == R.id.btnSignUp);
            if (signUp) doSignUp(); else doLogin();
        });
    }

    private void bindViews() {
        modeToggle = findViewById(R.id.modeToggle);
        loginOptionsRow = findViewById(R.id.loginOptionsRow);
        primaryAction = findViewById(R.id.primaryAction);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        inputConfirmPassword  = findViewById(R.id.inputConfirmPassword);
    }

    private void applyAuthMode(boolean signUp) {
        if (confirmPasswordLayout != null) {
            confirmPasswordLayout.setVisibility(signUp ? View.VISIBLE : View.GONE);
            confirmPasswordLayout.setError(null);
        }
        if (loginOptionsRow != null) {
            loginOptionsRow.setVisibility(signUp ? View.GONE : View.VISIBLE);
        }
        primaryAction.setText(signUp ? R.string.signUp : R.string.login);

        if (inputEmail != null) inputEmail.setError(null);
        if (inputPassword != null) inputPassword.setError(null);
        if (inputConfirmPassword != null) inputConfirmPassword.setError(null);
    }



    private void doSignUp() {
        String email = inputEmail.getText() == null ? "" : inputEmail.getText().toString().trim();
        String password = inputPassword.getText() == null ? "" : inputPassword.getText().toString();
        System.out.println(password);
        String confirm  = inputConfirmPassword.getText() == null ? "" : inputConfirmPassword.getText().toString();

        if (!isValidEmail(email)) {
            showDialog("Invalid email", "Please enter a valid email address.", null, null, null);
            return;
        }
        if (!isStrongPassword(password)) {
            showDialog("Weak password", "Password must be at least 8 characters.", null, null, null);
            return;
        }
        if (confirmPasswordLayout.getVisibility() == View.VISIBLE && !password.equals(confirm)) {
            showDialog("Passwords don’t match", "Please re-enter your password.", null, null, null);
            return;
        }

        setLoading(true);
        String username = deriveUsernameFromEmail(email);

        registerIn body = new registerIn(username, email, password);

        api.register(body, apiConfig.tokenRegister, "application/json")
                .enqueue(new Callback<registerOut>() {
                    @Override public void onResponse(Call<registerOut> call, Response<registerOut> res) {
                        setLoading(false);
                        if (res.isSuccessful() && res.body() != null) {
                            setLoading(false);
                            try {
                                Intent intent = new Intent(authentication.this, home.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                new androidx.appcompat.app.AlertDialog.Builder(authentication.this)
                                        .setTitle("Launch error")
                                        .setMessage(ex.getClass().getSimpleName() + ": " + String.valueOf(ex.getMessage()))
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        } else {
                            handleHttpError(res);
                        }
                    }
                    @Override public void onFailure(Call<registerOut> call, Throwable t) {
                        setLoading(false);
                        showDialog("Network error", t.getMessage(), null, null, null);
                    }
                });
    }

    private void doLogin() {
        String email = text(inputEmail);
        String password = text(inputPassword);


        if (!isValidEmail(email)) {
            showDialog("Invalid email", "Please enter a valid email address.", null, null, null);
            return;
        }
        if (password.isEmpty()) {
            showDialog("Password required", "Please enter your password.", null, null, null);
            return;
        }

        setLoading(true);
        loginIn body = new loginIn(email, password);

        api.login(body, apiConfig.tokenLogin, "application/json").enqueue(new Callback<loginOut>() {
            @Override public void onResponse(Call<loginOut> call, Response<loginOut> res) {
                setLoading(false);
                if (res.isSuccessful() && res.body() != null) {
                    setLoading(false);
                    try {
                        Intent intent = new Intent(authentication.this, home.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        new androidx.appcompat.app.AlertDialog.Builder(authentication.this)
                                .setTitle("Launch error")
                                .setMessage(ex.getClass().getSimpleName() + ": " + String.valueOf(ex.getMessage()))
                                .setPositiveButton("OK", null)
                                .show();
                    }
                } else {
                    handleHttpError(res);
                }
            }
            @Override public void onFailure(Call<loginOut> call, Throwable t) {
                setLoading(false);
                showDialog("Network error", t.getMessage(), null, null, null);
            }
        });
    }

    private void showDialog(String title,
                            String message,
                            @Nullable DialogOptions options,
                            @Nullable java.util.function.Consumer<String> onPositive,
                            @Nullable Runnable onNegative) {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message == null ? "" : message);

        TextInputLayout til = null;
        TextInputEditText et = null;

        if (options != null && options.withInput) {

            til = new TextInputLayout(this, null, com.google.android.material.R.style.Widget_Material3_TextInputLayout_FilledBox_Dense);
            til.setHint("Enter value");
            til.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);

            et = new TextInputEditText(til.getContext());
            et.setInputType(options.inputType != null ? options.inputType : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
            et.setHint("Type here");

            til.addView(et, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            LinearLayout container = new LinearLayout(this);
            container.setOrientation(LinearLayout.VERTICAL);
            int pad = (int) (16 * getResources().getDisplayMetrics().density);
            container.setPadding(pad, pad, pad, 0);
            container.addView(til);

            builder.setView(container);
        }

        String positive = options != null && options.positiveText != null ? options.positiveText : "OK";
        String negative = options != null && options.negativeText != null ? options.negativeText : null;

        builder.setPositiveButton(positive, null);
        if (negative != null) {
            builder.setNegativeButton(negative, (d, w) -> {
                d.dismiss();
                if (onNegative != null) onNegative.run();
            });
        }

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        TextInputLayout finalTil = til;
        TextInputEditText finalEt = et;

        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String value = "";
                    if (finalEt != null) value = finalEt.getText() == null ? "" : finalEt.getText().toString();

                    if (options != null && options.withInput) {
                        if (value.isEmpty()) {
                            if (finalTil != null) finalTil.setError("This field is required");
                            return;
                        }
                        if (finalTil != null) finalTil.setError(null);
                    }

                    dialog.dismiss();
                    if (onPositive != null) onPositive.accept(value);
                }));
        dialog.show();
    }

    private static class DialogOptions {
        final boolean withInput;
        final String positiveText;
        final String negativeText;
        final Integer inputType;

        DialogOptions(boolean withInput, String positiveText, String negativeText, Integer inputType) {
            this.withInput = withInput;
            this.positiveText = positiveText;
            this.negativeText = negativeText;
            this.inputType = inputType;
        }
    }


    private boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isStrongPassword(String password) {
        return password != null && password.length() >= 8 && password.length() <= 128;
    }

    private void setLoading(boolean loading) {
        primaryAction.setEnabled(!loading);
        primaryAction.setAlpha(loading ? 0.6f : 1f);
    }

    private String text(TextInputEditText t) {
        return t == null || t.getText() == null ? "" : t.getText().toString().trim();
    }

    private String deriveUsernameFromEmail(String email) {
        int at = email.indexOf('@');
        if (at > 0) return email.substring(0, at);
        return email.replaceAll("[^a-zA-Z0-9]", "_");
    }

    private boolean looksLikeHtml(String s) {
        if (s == null) return false;
        String t = s.trim().toLowerCase();
        return t.startsWith("<!doctype html") || t.startsWith("<html") || t.contains("<body");
    }

    private void handleHttpError(Response<?> response) {
        String errorMessage = "An unknown error occurred.";
        if (response.errorBody() != null) {
            try {
                errorMessage = response.errorBody().string();

                JsonElement element = JsonParser.parseString(errorMessage);
                if (element.isJsonObject()) {
                    JsonObject obj = element.getAsJsonObject();
                    if (obj.has("message")) {
                        errorMessage = obj.get("message").getAsString();
                    } else if (obj.has("detail")) {
                        errorMessage = obj.get("detail").getAsString();
                    }
                }
            } catch (Exception e) {
                // Ignore parsing errors, use raw response
            }
        }
        showDialog("Error " + response.code(), errorMessage, null, null, null);
    }
}
