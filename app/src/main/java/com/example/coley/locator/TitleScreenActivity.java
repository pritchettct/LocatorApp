package com.example.coley.locator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Coley Pritchett
 *
 * This activity is the first screen the user sees when they open the app. They must register
 * or login before being able to select the next activity.
 */
public class TitleScreenActivity extends Activity
{

    private Button mFriendButton, mPlaceButton, mRegisterButton, mLoginButton;
    private EditText mEmail, mPassword, mPhoneNumber;
    private Firebase ref;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_screen);
        Firebase.setAndroidContext(this);
        ref = new Firebase("https://invenio.firebaseio.com");

        mEmail = (EditText)findViewById(R.id.email);
        mPassword = (EditText)findViewById(R.id.password);
        mPhoneNumber = (EditText)findViewById(R.id.phone_number);

        // Button for registering a new account
        mRegisterButton = (Button)findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ref.createUser(mEmail.getText().toString(), mPassword.getText().toString(),
                        new Firebase.ValueResultHandler<Map<String, Object>>() {

                            @Override
                            public void onSuccess(Map<String, Object> result) {
                                System.out.println("Successfully created user account with uid: "
                                        + result.get("uid"));
                                ref.authWithPassword(mEmail.getText().toString(),
                                        mPassword.getText().toString(), new Firebase.AuthResultHandler() {

                                            @Override
                                            public void onAuthenticated(AuthData authData) {
                                                Map<String, String> map = new HashMap<String, String>();
                                                map.put("phone", mPhoneNumber.getText().toString());
                                                ref.child("users").child(authData.getUid()).setValue(map);
                                            }

                                            @Override
                                            public void onAuthenticationError(FirebaseError error) {
                                                System.out.print("Authentication Error");
                                            }
                                        });
                            }

                            @Override
                            public void onError(FirebaseError firebaseError) {
                                // there was an error
                            }
                        });
            }
        });

        // Button for logging in with an existing account
        mLoginButton = (Button)findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref.authWithPassword(mEmail.getText().toString(), mPassword.getText().toString(),
                        new Firebase.AuthResultHandler() {
                            @Override
                            public void onAuthenticated(AuthData authData) {
                                System.out.println("User ID: " + authData.getUid() + ", Provider: "
                                        + authData.getProvider());
                                mFriendButton.setClickable(true);
                            }

                            @Override
                            public void onAuthenticationError(FirebaseError firebaseError) {
                                System.out.print("there was an error");
                            }
                        });
            }
        });

        mFriendButton = (Button)findViewById(R.id.friend_button);
        mFriendButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TitleScreenActivity.this, LocateFriendActivity.class);
                startActivity(i);
            }
        });
        mFriendButton.setClickable(false);

        mPlaceButton = (Button)findViewById(R.id.place_button);
        mPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TitleScreenActivity.this, LocatePlaceActivity.class);
                startActivity(i);
            }
        });
        mPlaceButton.setClickable(false);
    }
}
