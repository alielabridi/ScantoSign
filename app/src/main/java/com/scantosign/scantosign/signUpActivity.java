package com.scantosign.scantosign;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

public class signUpActivity extends AppCompatActivity{

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private sendingTask sendingtask = null;

    // UI references.
    private EditText EmailView;
    private EditText StudentIDView;
    private EditText FirstNameView;
    private EditText LastnameView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setupActionBar();


        EmailView = (EditText) findViewById(R.id.email);
        FirstNameView = (EditText) findViewById(R.id.firstname);
        LastnameView = (EditText) findViewById(R.id.lastname);
        StudentIDView = (EditText) findViewById(R.id.studentid);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptToSignUp();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptToSignUp() {
        if (sendingtask != null) {
            return;
        }

        // Reset errors.
        EmailView.setError(null);
        LastnameView.setError(null);
        FirstNameView.setError(null);
        StudentIDView.setError(null);

        // Store values at the time of the login attempt.
        String email = EmailView.getText().toString();
        String firstname = FirstNameView.getText().toString();
        String lastname = LastnameView.getText().toString();
        String studentid = StudentIDView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid lastname, if the user entered one.
        if (TextUtils.isEmpty(lastname)) {
            LastnameView.setError(getString(R.string.error_empty_field));
            focusView = LastnameView;
            cancel = true;
        }

        // Check for a valid firstname, if the user entered one.
        if (TextUtils.isEmpty(firstname)) {
            FirstNameView.setError(getString(R.string.error_empty_field));
            focusView = FirstNameView;
            cancel = true;
        }
        // Check for a valid studentid, if the user entered one.
        if (TextUtils.isEmpty(studentid)) {
            StudentIDView.setError(getString(R.string.error_empty_field));
            focusView = StudentIDView;
            cancel = true;
        }


        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            EmailView.setError(getString(R.string.error_empty_field));
            focusView = EmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            EmailView.setError(getString(R.string.error_invalid_email));
            focusView = EmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            sendingtask = new sendingTask(email,firstname,lastname,studentid);
            sendingtask.execute((Void) null);

        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
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
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class sendingTask extends AsyncTask<Void, Void, Boolean> {
        String email ;
        String firstname ;
        String lastname;
        String studentid ;

        sendingTask(String email,String firstname,String lastname,String studentid) {
            this.email = email;
            this.firstname = firstname;
            this.lastname = lastname;
            this.studentid = studentid;
        }

        private JSONObject constructJsonObject() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.accumulate("firstNameID", firstname);
                jsonObject.accumulate("lastNameID", lastname);
                jsonObject.accumulate("emailID", email);
                jsonObject.accumulate("StudentID", studentid);
                return jsonObject;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
        private String sendToServer() {
            String JsonResponse = null;
            String JsonDATA = constructJsonObject().toString();
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            System.out.println(JsonDATA);
            try {
                URL url = new URL("https://scantosign.herokuapp.com/sheet?q=9fa47bf1_9550_42e9_bf8d_75b7698a8d38");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                // is output buffer writter
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                //set headers and method
                Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                writer.write(JsonDATA);
                // json data
                writer.close();
                InputStream inputStream = urlConnection.getInputStream();
                //input stream
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    buffer.append(inputLine + "\n");
                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return null;
                }
                JsonResponse = buffer.toString();
                //response data
                Log.i("test",JsonResponse);
                //send to post execute
                return JsonResponse;
                //return null;



            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("test", "Error closing stream", e);
                    }
                }
            }
            return null;

        }

        @Override
        protected Boolean doInBackground(Void... params) {
                if(!isNetworkAvailable()){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            showProgress(false);
                            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_LONG).show();
                        }
                    });
                    return false;
                }
            String result = sendToServer();
            for (int i = 0; i < 20000 && result != null && !result.contains("OK"); i++) {
                try {
                    wait(100);
                    result = sendToServer();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (result != null && result.contains("OK")) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "You have signed up successfully", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
                return true;
            } else {
                runOnUiThread(new Runnable() {
                    public void run() {
                        showProgress(false);
                        Toast.makeText(getApplicationContext(), "The connection to the server took too long! Please retry later", Toast.LENGTH_LONG).show();
                    }
                });
                return false;
            }
        }

        private boolean isNetworkAvailable() {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
        }

        @Override
        protected void onCancelled() {
            sendingtask = null;
            showProgress(false);
        }
    }
}

