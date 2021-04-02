package com.zzt.google.auto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.xxweb.network.playapi.PlayApi;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.zzt.google.auto.net.HttpResponse;

import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class ActivityAutoLoginV3 extends AppCompatActivity {
    private static final String TAG = ActivityAutoLoginV3.class.getSimpleName();
    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private static final String CLIENT_ID = "618281019386-1ofuoofpub8cp6ck9kct1ufcp6qkbv84.apps.googleusercontent.com";
    private boolean showOneTapUI = true;

    private SignInButton sign_in_button;
    private SignInClient oneTapClient;
    private BeginSignInRequest signUpRequest;
    private String idTokenString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_login_v2);

        TextView textView = findViewById(R.id.textView);
        textView.setText("Create new accounts with one tap \n 创建新的账号");


        findViewById(R.id.button).setOnClickListener(v -> {
            startActivity(new Intent(ActivityAutoLoginV3.this, ActivityAutoLoginV2.class));
        });


        /**
         * 令牌验证
         */
        findViewById(R.id.token_verifier).setOnClickListener(v -> {
//            try {
//                tokenVerifier();
//            } catch (GeneralSecurityException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            getAccessToken();

        });

        oneTapClient = Identity.getSignInClient(this);
        signUpRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.your_web_client_id))
                        // Show all accounts on the device.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();

        sign_in_button = findViewById(R.id.sign_in_button);
        sign_in_button.setOnClickListener(v -> {
            oneTapClient.beginSignIn(signUpRequest)
                    .addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() {
                        @Override
                        public void onSuccess(BeginSignInResult result) {
                            try {
                                startIntentSenderForResult(
                                        result.getPendingIntent().getIntentSender(), REQ_ONE_TAP,
                                        null, 0, 0, 0);
                            } catch (IntentSender.SendIntentException e) {
                                Log.e(TAG, "Couldn't start One Tap UI: " + e.getLocalizedMessage());
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // No Google Accounts found. Just continue presenting the signed-out UI.
                            Log.d(TAG, e.getLocalizedMessage());
                        }
                    });

        });

    }


    public void getAccessToken() {
        /**
        {
            "access_token":"ya29.a0AfH6SMDivI62vNlSGYP0rzarG28gAMkf2egAwKJzLWbQMl3KZW_oYSCYp6dvlwC2NbQiZngFaQnut1xxs9_KPIBCUQ58iO0_wXceTO6DgDuNkyaCLHgLZWptBhtnB4xlguWCDkDnPJFqQMIBYvmxKHm61yqw",
                "scope":"https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile openid",
               "id_token":"eyJhbGciOiJSUzI1NiIsImtpZCI6IjEzZThkNDVhNDNjYjIyNDIxNTRjN2Y0ZGFmYWMyOTMzZmVhMjAzNzQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI2MTgyODEwMTkzODYtdGE0YzNqcHBuYmVob2RlMnA5aXIxOHNobnU3ajJvN2suYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI2MTgyODEwMTkzODYtdGE0YzNqcHBuYmVob2RlMnA5aXIxOHNobnU3ajJvN2suYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTI2OTM5MzEwODg3NjA2MzcwODAiLCJlbWFpbCI6InpldGluZ3podUBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXRfaGFzaCI6IkJsNFRtWVVEaDh4LTQwTUIyQXE1RHciLCJuYW1lIjoi5pyx5rO95LqtIiwicGljdHVyZSI6Imh0dHBzOi8vbGg2Lmdvb2dsZXVzZXJjb250ZW50LmNvbS8tLUZ5Zi1kZW9zQzgvQUFBQUFBQUFBQUkvQUFBQUFBQUFBQUEvQU1adXVjbjdiRWpuY1FYdzRKaXZOcUR0eUtUVDBxM1FRUS9zOTYtYy9waG90by5qcGciLCJnaXZlbl9uYW1lIjoi5rO95LqtIiwiZmFtaWx5X25hbWUiOiLmnLEiLCJsb2NhbGUiOiJ6aC1DTiIsImlhdCI6MTYxNzA5MDY0MywiZXhwIjoxNjE3MDk0MjQzfQ.lLUgQT4zafNOZQkRl-11I_PLA5mtDhL2WB34Vl7GmvFzah4Lq66WeCVvMctBO0hqkIT42ZvCx3xaGlaoZWNDH0VLvnHMvBGnEMOxH0Fl9sy81RgAoCVS-Zd6aW96gWEO8IxLYivP4U74ldhxLRwxPf_zZgJjxe-fv-SGQ3Ta-ODX7ujQg6BoJ3o7eY4ZnMMv6HhljyOnvOoeum9M6XUt-hWr3ZTwWQM4BfHgI5G1BLtU1CEZGZZNJ9gxRVFE5feSu0VgpOIULxGw_zzJIXOz-B2dqYtxE4yFMBGSi1fNkm4HDHIMPOVObPxJ12vgCTuunvQpxoKkD1kRp5dtkDP60g",
                "token_type":"Bearer",
                "expires_in":3599
        }
        String idToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEzZThkNDVhNDNjYjIyNDIxNTRjN2Y0ZGFmYWMyOTMzZmVhMjAzNzQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI2MTgyODEwMTkzODYtMW9mdW9vZnB1YjhjcDZjazlrY3QxdWZjcDZxa2J2ODQuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI2MTgyODEwMTkzODYtdGE0YzNqcHBuYmVob2RlMnA5aXIxOHNobnU3ajJvN2suYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTI2OTM5MzEwODg3NjA2MzcwODAiLCJlbWFpbCI6InpldGluZ3podUBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6IuacseazveS6rSIsInBpY3R1cmUiOiJodHRwczovL2xoNi5nb29nbGV1c2VyY29udGVudC5jb20vLS1GeWYtZGVvc0M4L0FBQUFBQUFBQUFJL0FBQUFBQUFBQUFBL0FNWnV1Y243YkVqbmNRWHc0Sml2TnFEdHlLVFQwcTNRUVEvczk2LWMvcGhvdG8uanBnIiwiZ2l2ZW5fbmFtZSI6IuazveS6rSIsImZhbWlseV9uYW1lIjoi5pyxIiwibG9jYWxlIjoiemgtQ04iLCJpYXQiOjE2MTcwODc4OTYsImV4cCI6MTYxNzA5MTQ5Nn0.l53tkgWFuwNl8-bQexXaLAlbLm5M5od6NcYpxfrhepDt-DCSXYYZ-nIWiciEBCvorOW4GokSfXjnROvloHKwRCNCyKRpUaIcrHbKZTvH7qPvYBY9V4ChyOLzL9F1ebURavnUjzNq8-bwX-bFAdk-5Ca3fGe_RckOdjhxgqo4S6ip7pYLTKSN8jRqQHrHnnuLJ8fBTIKII5m9q4QOl0S-T072wwvjbSVbm0H9-KUcrjf3nZmzLcZykEGKlytbm5WxyKdt3lB5OHd9nJRHfu1CYongH7nZkVskUFmsGbX7lIcM2ITa5clAMMC4tLyPwaNrACYmHTLfkMTncJlwlSBa9g";
         */
        LiveData<HttpResponse<String>> jtQpv6Q0mpGmfFSZ1qs0FDUi = PlayApi.Companion.getApi().getAccountTokenNew(
                "618281019386-ta4c3jppnbehode2p9ir18shnu7j2o7k.apps.googleusercontent.com",
                "jtQpv6Q0mpGmfFSZ1qs0FDUi",
                "https://oauth2.googleapis.com/token",
                "",
                "https://accounts.google.com/o/oauth2/auth",
                idTokenString, "authorization_code");
        jtQpv6Q0mpGmfFSZ1qs0FDUi.observe(ActivityAutoLoginV3.this, new Observer<HttpResponse<String>>() {
            @Override
            public void onChanged(HttpResponse<String> stringHttpResponse) {
                Log.e(TAG, "google Sign 接口请求数据：" + stringHttpResponse);
            }
        });

        /**
         https://oauth2.googleapis.com/token?refresh_token={{tokenUpdated}}&client_id={{googleClientId}}&client_secret={{googleClientSecret}}&grant_type=refresh_token
         */
    }


    /**
     * 令牌验证
     */
    private void tokenVerifier() throws GeneralSecurityException, IOException {
        HttpTransport transport = null;
        JsonFactory jsonFactory = null;
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                // Specify the CLIENT_ID of the app that accesses the backend:
                .setAudience(Collections.singletonList(CLIENT_ID))
                // Or, if multiple clients access the backend:
                //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                .build();

// (Receive idTokenString by HTTPS POST)

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            // Print user identifier
            String userId = payload.getSubject();
            System.out.println("User ID: " + userId);

            // Get profile information from payload
            String email = payload.getEmail();
            boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String locale = (String) payload.get("locale");
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");

            // Use or store profile information
            // ...

        } else {
            System.out.println("Invalid ID token.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_ONE_TAP:
                try {
                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                    String idToken = credential.getGoogleIdToken();
                    if (idToken != null) {
                        // Got an ID token from Google. Use it to authenticate
                        // with your backend.
                        Log.d(TAG, "Got ID token.  ==:" + idToken);
                        idTokenString = idToken;
                    }
                } catch (ApiException e) {
                    // ...
                    switch (e.getStatusCode()) {
                        case CommonStatusCodes.CANCELED:
                            Log.d(TAG, "One-tap dialog was closed.");
                            // Don't re-prompt the user.
                            showOneTapUI = false;
                            break;
                        case CommonStatusCodes.NETWORK_ERROR:
                            Log.d(TAG, "One-tap encountered a network error.");
                            // Try again or just ignore.
                            break;
                        default:
                            Log.d(TAG, "Couldn't get credential from result."
                                    + e.getLocalizedMessage());
                            break;
                    }

                }
                break;
        }
    }

}