package com.workwise.network;

import com.workwise.models.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface apiService {

    @POST("v1/workwise/account")
    Call<registerOut> register(@Body registerIn body,
                               @Header("X-Endpoint-Token") String endpointToken,
                               @Header("Accept") String accept);

    @POST("v1/workwise/user")
    Call<loginOut> login(@Body loginIn body,
                         @Header("X-Endpoint-Token") String endpointToken,
                         @Header("Accept") String accept);
}
