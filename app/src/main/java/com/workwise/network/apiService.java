package com.workwise.network;

import com.workwise.models.*;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface apiService {

    // ========== AUTHENTICATION ==========
    @POST("v1/workwise/account")
    Call<registerOut> register(@Body registerIn body,
                               @Header("X-Endpoint-Token") String endpointToken,
                               @Header("Accept") String accept);

    @POST("v1/workwise/user")
    Call<loginOut> login(@Body loginIn body,
                         @Header("X-Endpoint-Token") String endpointToken,
                         @Header("Accept") String accept);

    // ========== PROFILE ==========
    @GET("v1/workwise/profile/{user_id}")
    Call<userProfile> getProfile(@Path("user_id") int userId,
                                 @Header("X-Endpoint-Token") String endpointToken);

    @PUT("v1/workwise/profile/{user_id}")
    Call<userProfile> updateProfile(@Path("user_id") int userId,
                                    @Body userProfileUpdate profile,
                                    @Header("X-Endpoint-Token") String endpointToken);

    @Multipart
    @POST("v1/workwise/profile/{user_id}/image")
    Call<profileImageUpload> uploadProfileImage(@Path("user_id") int userId,
                                                @Part MultipartBody.Part file,
                                                @Header("X-Endpoint-Token") String endpointToken);

    // ========== CV ==========
    @GET("v1/workwise/cvs/{user_id}")
    Call<List<cvItem>> getCVs(@Path("user_id") int userId,
                              @Header("X-Endpoint-Token") String endpointToken);

    @Multipart
    @POST("v1/workwise/cvs/{user_id}")
    Call<cvUpload> uploadCV(@Path("user_id") int userId,
                            @Part MultipartBody.Part file,
                            @Part("is_primary") RequestBody isPrimary,
                            @Header("X-Endpoint-Token") String endpointToken);

    @DELETE("v1/workwise/cvs/{user_id}/{cv_id}")
    Call<apiResponse> deleteCV(@Path("user_id") int userId,
                               @Path("cv_id") int cvId,
                               @Header("X-Endpoint-Token") String endpointToken);

    @PUT("v1/workwise/cvs/{user_id}/{cv_id}/primary")
    Call<apiResponse> setPrimaryCV(@Path("user_id") int userId,
                                   @Path("cv_id") int cvId,
                                   @Header("X-Endpoint-Token") String endpointToken);

    // ========== QUALIFICATIONS ==========
    @GET("v1/workwise/qualifications/{user_id}")
    Call<List<qualification>> getQualifications(@Path("user_id") int userId,
                                                @Header("X-Endpoint-Token") String endpointToken);

    @POST("v1/workwise/qualifications/{user_id}")
    Call<qualification> addQualification(@Path("user_id") int userId,
                                         @Body qualificationInput qualification,
                                         @Header("X-Endpoint-Token") String endpointToken);

    @PUT("v1/workwise/qualifications/{user_id}/{qualification_id}")
    Call<qualification> updateQualification(@Path("user_id") int userId,
                                            @Path("qualification_id") int qualificationId,
                                            @Body qualificationInput qualification,
                                            @Header("X-Endpoint-Token") String endpointToken);

    @DELETE("v1/workwise/qualifications/{user_id}/{qualification_id}")
    Call<apiResponse> deleteQualification(@Path("user_id") int userId,
                                          @Path("qualification_id") int qualificationId,
                                          @Header("X-Endpoint-Token") String endpointToken);

    // ========== STATS ==========
    @GET("v1/workwise/stats/{user_id}")
    Call<userStats> getStats(@Path("user_id") int userId,
                             @Header("X-Endpoint-Token") String endpointToken);

    // ========== SAVED JOBS ==========
    @GET("v1/workwise/saved-jobs/{user_id}")
    Call<List<savedJob>> getSavedJobs(@Path("user_id") int userId,
                                      @Header("X-Endpoint-Token") String endpointToken);

    @POST("v1/workwise/saved-jobs/{user_id}")
    Call<savedJob> addSavedJob(@Path("user_id") int userId,
                               @Body savedJobInput job,
                               @Header("X-Endpoint-Token") String endpointToken);

    @DELETE("v1/workwise/saved-jobs/{user_id}/{saved_job_id}")
    Call<apiResponse> deleteSavedJob(@Path("user_id") int userId,
                                     @Path("saved_job_id") int savedJobId,
                                     @Header("X-Endpoint-Token") String endpointToken);

    // Get random jobs list
    @GET("/v1/workwise/jobs")
    Call<List<job>> getJobs(
            @Header("X-Endpoint-Token") String endpointToken,
            @Query("limit") int limit
    );

    // Get a single job by ID
    @GET("/v1/workwise/jobs/{job_id}")
    Call<job> getJobById(
            @Header("X-Endpoint-Token") String endpointToken,
            @Path("job_id") int jobId
    );
}