package com.workwise.network;

import com.workwise.models.*;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

import com.workwise.models.ConversationCreateIn;
import com.workwise.models.ConversationOut;
import com.workwise.models.MessageSendIn;
import com.workwise.models.MessageOut;

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
    Call<UserProfileOut> getProfile(
            @Path("user_id") int userId,
            @Header("X-Endpoint-Token") String token
    );

    @PUT("v1/workwise/profile/{user_id}")
    Call<UserProfileOut> updateProfile(@Path("user_id") int userId,
                                       @Body UpdateProfileIn body,
                                       @Header("X-Endpoint-Token") String endpointToken);

    // --- FIXED: URL now includes {user_id} and uses @Path ---
    @Multipart
    @POST("v1/workwise/profile/{user_id}/image")
    Call<ProfileImageUploadResponse> uploadProfileImage(
            @Path("user_id") int userId,
            @Part MultipartBody.Part file,
            @Header("X-Endpoint-Token") String token
    );

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
    Call<List<savedJobs>> getSavedJobs(@Path("user_id") int userId,
                                       @Header("X-Endpoint-Token") String endpointToken);

    @POST("v1/workwise/saved-jobs/{user_id}")
    Call<savedJobs> addSavedJob(@Path("user_id") int userId,
                                @Body savedJobInput job,
                                @Header("X-Endpoint-Token") String endpointToken);

    @DELETE("v1/workwise/saved-jobs/{user_id}/{saved_job_id}")
    Call<apiResponse> deleteSavedJob(@Path("user_id") int userId,
                                     @Path("saved_job_id") int savedJobId,
                                     @Header("X-Endpoint-Token") String endpointToken);

    // ========== JOBS ==========
    @GET("v1/workwise/jobs")
    Call<List<job>> getActiveJobs(
            @Header("X-Endpoint-Token") String token,
            @Query("limit") int limit,
            @Query("offset") int offset,
            @Query("employment_type") String employmentType, // New
            @Query("work_arrangement") String workArrangement, // New
            @Query("location") String location // New
    );

    // --- THIS DUPLICATE METHOD WAS REMOVED ---
    // @GET("v1/workwise/jobs/search")
    // Call<List<JobListingResponse>> searchJobs( ... );
    // --- END REMOVAL ---

    @GET("v1/workwise/jobs/detail/{job_id}")
    Call<JobDetailResponse> getJobDetail(
            @Header("X-Endpoint-Token") String token,
            @Path("job_id") int jobId
    );

    @GET("/v1/workwise/jobs/{job_id}")
    Call<job> getJobById(
            @Header("X-Endpoint-Token") String endpointToken,
            @Path("job_id") int jobId
    );

    // FOR THE 'JOB SEARCH' PAGE - This is the correct definition
    @GET("v1/workwise/jobs/search")
    Call<List<job>> searchJobs(
            @Header("X-Endpoint-Token") String token,
            @Query("query") String query,
            @Query("employment_type") String employmentType,
            @Query("work_arrangement") String workArrangement,
            @Query("limit") int limit,
            @Query("offset") int offset
    );


    @POST("v1/workwise/forgot-password")
    Call<forgotPasswordOut> forgotPassword(@Body forgotPasswordIn body,
                                           @Header("X-Endpoint-Token") String endpointToken,
                                           @Header("Accept") String accept);

    @POST("v1/workwise/verify-reset-code")
    Call<verifyResetCodeOut> verifyResetCode(@Body verifyResetCodeIn body,
                                             @Header("X-Endpoint-Token") String endpointToken,
                                             @Header("Accept") String accept);

    @POST("v1/workwise/reset-password")
    Call<resetPasswordOut> resetPassword(@Body resetPasswordIn body,
                                         @Header("X-Endpoint-Token") String endpointToken,
                                         @Header("Accept") String accept);

    @POST("v1/workwise/chats")
    Call<ConversationOut> createConversation(@Body ConversationCreateIn body,
                                             @Header("X-Endpoint-Token") String endpointToken);

    @GET("v1/workwise/chats/{user_id}")
    Call<List<ConversationOut>> getUserConversations(@Path("user_id") int userId,
                                                     @Header("X-Endpoint-Token") String endpointToken);

    @GET("v1/workwise/chats/{conversation_id}/messages")
    Call<List<MessageOut>> getMessages(@Header("X-Endpoint-Token") String endpointToken,
                                       @Path("conversation_id") int conversationId,
                                       @Query("limit") int limit,
                                       @Query("before") Integer before);

    @POST("v1/workwise/chats/{conversation_id}/messages")
    Call<MessageOut> sendMessage(@Header("X-Endpoint-Token") String endpointToken,
                                 @Path("conversation_id") int conversationId,
                                 @Body MessageSendIn message);


}