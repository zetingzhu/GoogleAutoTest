package com.zzt.google.auto;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.xxweb.network.playapi.PlayApi;
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.Gson;
import com.zzt.google.auto.net.HttpResponse;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9999;
    GoogleSignInClient mGoogleSignInClient;

    // Scope for reading user's contacts
    private static final String CONTACTS_SCOPE = "https://www.googleapis.com/auth/contacts.readonly";
    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 1; /* unique request id */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_my);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        String serverClientId = "618281019386-ta4c3jppnbehode2p9ir18shnu7j2o7k.apps.googleusercontent.com";


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestScopes(new Scope(CONTACTS_SCOPE))
                .requestScopes(new Scope(Scopes.PROFILE))
                .requestServerAuthCode(serverClientId)
//                .requestIdToken(serverClientId)
                .requestEmail()
//                .requestProfile()
//                .requestId()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        findViewById(
                R.id.btn_login_out
        ).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }

        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.w(TAG, "google Sign  onStart ");
        // Check for existing Google Sign In account, if the user is already signed in
// the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            String personName = account.getDisplayName();
            String personGivenName = account.getGivenName();
            String personFamilyName = account.getFamilyName();
            String personEmail = account.getEmail();
            String personId = account.getId();
            Uri personPhoto = account.getPhotoUrl();

            Log.d(TAG, "google Sign  onStart personName: " + personName +
                    "\npersonGivenName: " + personGivenName +
                    "\npersonFamilyName: " + personFamilyName +
                    "\npersonEmail: " + personEmail +
                    "\npersonId: " + personId +
                    "\npersonPhoto: " + personPhoto
            );
        }

        Log.w(TAG, "google Sign  onStart account:" + AccountToString(account));
        updateUI(account);
    }

    private void updateUI(GoogleSignInAccount account) {
        // 这里是登录成功？
        Log.d(TAG, "google Sign  updateUI account:" + AccountToString(account));
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signInNew() {
        GetSignInIntentRequest request =
                GetSignInIntentRequest.builder()
                        .setServerClientId(getString(R.string.server_client_id))
                        .build();

        Identity.getSignInClient(MainActivity.this)
                .getSignInIntent(request)
                .addOnSuccessListener(new OnSuccessListener<PendingIntent>() {
                    @Override
                    public void onSuccess(PendingIntent result) {
                        try {
                            startIntentSenderForResult(
                                    result.getIntentSender(),
                                    REQUEST_CODE_GOOGLE_SIGN_IN,
                                    /* fillInIntent= */ null,
                                    /* flagsMask= */ 0,
                                    /* flagsValue= */ 0,
                                    /* extraFlags= */ 0,
                                    /* options= */ null);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }


    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                        Log.w(TAG, "google Sign  signOut onComplete task:" + task);
                        revokeAccess();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            Log.i(TAG, "google Sign  onActivityResult   task:" + task);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {

        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            Log.e(TAG, "google Sign  handleSignInResult   account:" + AccountToString(account));

            String authCode = account.getServerAuthCode();


//            LiveData<HttpResponse<String>> jtQpv6Q0mpGmfFSZ1qs0FDUi = PlayApi.Companion.getApi().getAccountToken("618281019386-ta4c3jppnbehode2p9ir18shnu7j2o7k.apps.googleusercontent.com",
//                    "jtQpv6Q0mpGmfFSZ1qs0FDUi", authCode, "");

            /** getServerAuthCode:4/0AY0e-g51KL4WQqo9RYiyy5uU_7kpVLvT_iAYQj2zmq75rHKfl6z73X8dtn3QWuPoJN38FQ
              client_id=618281019386-ta4c3jppnbehode2p9ir18shnu7j2o7k.apps.googleusercontent.com& client_secret=jtQpv6Q0mpGmfFSZ1qs0FDUi&token_uri=https://oauth2.googleapis.com/token&redirect_uri=http://www.baidu.com&auth_uri=https://accounts.google.com/o/oauth2/auth&code=4/0AY0e-g51KL4WQqo9RYiyy5uU_7kpVLvT_iAYQj2zmq75rHKfl6z73X8dtn3QWuPoJN38FQ&grant_type= authorization_code
             */
            LiveData<HttpResponse<String>> getAccountToken = PlayApi.Companion.getApi().getAccountTokenNew(
                    "618281019386-ta4c3jppnbehode2p9ir18shnu7j2o7k.apps.googleusercontent.com",
                    "jtQpv6Q0mpGmfFSZ1qs0FDUi",
                    "https://oauth2.googleapis.com/token",
                    "",
                    "https://accounts.google.com/o/oauth2/auth",
                    authCode, "authorization_code");
            getAccountToken.observe(MainActivity.this, new Observer<HttpResponse<String>>() {
                @Override
                public void onChanged(HttpResponse<String> stringHttpResponse) {
                    Log.e(TAG, "google Sign 接口请求数据：" + stringHttpResponse);
                }
            });


//            client_id:618281019386-ta4c3jppnbehode2p9ir18shnu7j2o7k.apps.googleusercontent.com
//            client_secret:jtQpv6Q0mpGmfFSZ1qs0FDUi
//            code:4/0AY0e-g5kAxTQWkm7-KO753hyNyCWB2qemFn1a3LqcwaRcLm9SijfC9bUjmmWcz9Q7ulNUw
//            redirect_uri:
            GoogleAuthorizationCodeTokenRequest ss = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    "618281019386-ta4c3jppnbehode2p9ir18shnu7j2o7k.apps.googleusercontent.com",
                    "jtQpv6Q0mpGmfFSZ1qs0FDUi",
                    "4/0AY0e-g5kAxTQWkm7-KO753hyNyCWB2qemFn1a3LqcwaRcLm9SijfC9bUjmmWcz9Q7ulNUw", "");

            Log.w(TAG, "json:" + new Gson().toJson(ss));

/**
 * 您还可以得到用户的电子邮件地址getEmail ，用户的谷歌标识（客户端使用）用getId和令牌与用户的ID getIdToken 。
 * 如果您需要将当前登录的用户传递到后端服务器，请将ID令牌发送到您的后端服务器，并在服务器上验证该令牌。
 */
            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    /**
     * 清除账号
     */
    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...

                        Log.w(TAG, "google Sign  revokeAccess  onComplete task:" + task);
                    }
                });
    }

    public String AccountToString(GoogleSignInAccount account) {
        if (account != null) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(account.zac());
            stringBuffer.append(" ---   getServerAuthCode:");
            stringBuffer.append(account.getServerAuthCode());

            return stringBuffer.toString();
        }
        return "获取授权为空";
    }


//    public void RequestAccounToken() {
//        HttpPost httpPost = new HttpPost("https://yourbackend.example.com/authcode");
//
//        try {
//            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
//            nameValuePairs.add(new BasicNameValuePair("authCode", authCode));
//            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//
//            HttpResponse response = httpClient.execute(httpPost);
//            int statusCode = response.getStatusLine().getStatusCode();
//            final String responseBody = EntityUtils.toString(response.getEntity());
//        } catch (ClientProtocolException e) {
//            Log.e(TAG, "Error sending auth code to backend.", e);
//        } catch (IOException e) {
//            Log.e(TAG, "Error sending auth code to backend.", e);
//        }
//    }


}