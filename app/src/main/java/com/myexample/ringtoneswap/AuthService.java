package com.myexample.ringtoneswap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface AuthService {
    @FormUrlEncoded
    @POST("auth")
    Call<ResponseBody> auth(@Field("access_token") String accessToken);
}
