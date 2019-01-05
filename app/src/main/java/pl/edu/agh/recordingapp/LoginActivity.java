package pl.edu.agh.recordingapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;

import pl.edu.agh.recordingapp.rest.request.LoginUserRequest;
import pl.edu.agh.recordingapp.rest.response.LoginResponse;
import pl.edu.agh.recordingapp.rest.service.UserService;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    public static String SERVER_ADDRESS = "http://localhost:8080/";

    private UserLoginTask mAuthTask = null;

    private String token;

    private EditText mLoginView;
    private EditText mPasswordView;
    private EditText mServerAddressView;
    private View mProgressView;
    private View mLoginFormView;

    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl(LoginActivity.SERVER_ADDRESS)
                .build();

        userService = retrofit.create(UserService.class);

        setContentView(R.layout.activity_login);

        Button createAccountButton = findViewById(R.id.create_account_button);
        createAccountButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, CreateAccountActivity.class);
            startActivity(intent);
        });

        mLoginView = findViewById(R.id.login);

        mServerAddressView = findViewById(R.id.server_address);
        Button setServerAddressButton = findViewById(R.id.set_address_button);
        setServerAddressButton.setOnClickListener(view -> SERVER_ADDRESS = mServerAddressView.getText().toString());

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });

        Button signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(view -> attemptLogin());

        mLoginFormView = findViewById(R.id.create_account_form);
        mProgressView = findViewById(R.id.create_account_progress);
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mLoginView.setError(null);
        mPasswordView.setError(null);

        String login = mLoginView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(login)) {
            mLoginView.setError(getString(R.string.error_field_required));
            focusView = mLoginView;
            cancel = true;
        } else if (!isLoginValid(login)) {
            mLoginView.setError(getString(R.string.error_invalid_login));
            focusView = mLoginView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(login, password, this);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isLoginValid(String login) {
        return login.length() >= 4 && login.length() <= 16;
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private LoginActivity mUiThread;
        private final String mLogin;
        private final String mPassword;

        UserLoginTask(String login, String password, LoginActivity uiThread) {
            mLogin = login;
            mPassword = password;
            mUiThread = uiThread;
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        protected Boolean doInBackground(Void... params) {
            Call<LoginResponse> login = userService.login(new LoginUserRequest(mLogin, mPassword));
            try {
                Response<LoginResponse> execute = login.execute();
                if (execute.isSuccessful()) {
                    token = execute.body().getToken();
                    return true;
                } else return false;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Intent mainActivity = new Intent(mUiThread, MainActivity.class);
                mainActivity.putExtra("token", token);
                startActivity(mainActivity);
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

