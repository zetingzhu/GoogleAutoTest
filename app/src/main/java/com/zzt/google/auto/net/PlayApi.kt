package com.example.xxweb.network.playapi

import androidx.lifecycle.LiveData
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest
import com.zzt.google.auto.net.ApiRetrofitUtils
import com.zzt.google.auto.net.HttpResponse
import retrofit2.http.*
import java.util.*

/**
 * @author: zeting
 * @date: 2020/12/7
 *
 */
interface PlayApi {
    companion object {
        var baseUrl = "https://oauth2.googleapis.com/"
        fun getApi(): PlayApi {
            return ApiRetrofitUtils.getInstance().getApiService(baseUrl, PlayApi::class.java)
        }
    }

    /**
     *
     *
     * client_id: 618281019386-ta4c3jppnbehode2p9ir18shnu7j2o7k.apps.googleusercontent.com
     * client_secret: jtQpv6Q0mpGmfFSZ1qs0FDUi
     * code: 4/0AY0e-g5kAxTQWkm7-KO753hyNyCWB2qemFn1a3LqcwaRcLm9SijfC9bUjmmWcz9Q7ulNUw
     * redirect_uri:""
     * grant_type: authorization_code
     */
    @Headers("Content_Type:application/x-www-form-urlencoded", "charset:UTF-8")
    @POST("token")
    @FormUrlEncoded
    fun getAccountToken(
            @Field("client_id") clientId: String,
            @Field("client_secret") clientSecret: String,
            @Field("code") code: String,
            @Field("redirect_uri") redirectUri: String
    ): LiveData<HttpResponse<String>>

    @Headers("Content_Type:application/x-www-form-urlencoded", "charset:UTF-8")
    @POST("token")
    @FormUrlEncoded
    fun getAccountTokenNew(
            @Field("client_id") clientId: String,
            @Field("client_secret") clientSecret: String,
            @Field("token_uri") tokenUri: String,
            @Field("redirect_uri") redirectUri: String,
            @Field("auth_uri") authUri: String,
            @Field("code") code: String,
            @Field("grant_type") grantType: String
    ): LiveData<HttpResponse<String>>

    fun abc() {
        var ss = GoogleAuthorizationCodeTokenRequest(null, null, null, null, null, null, null)
    }

}