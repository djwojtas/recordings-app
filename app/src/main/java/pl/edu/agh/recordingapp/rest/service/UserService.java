package pl.edu.agh.recordingapp.rest.service;

import pl.edu.agh.recordingapp.rest.request.CreateUserRequest;
import pl.edu.agh.recordingapp.rest.request.LoginUserRequest;
import pl.edu.agh.recordingapp.rest.response.DefaultResponse;
import pl.edu.agh.recordingapp.rest.response.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UserService {
    @POST("login")
    @Headers("Content-Type: application/json")
    Call<LoginResponse> login(@Body LoginUserRequest loginUserRequest);

    @POST("user-management/user")
    @Headers("Content-Type: application/json")
    Call<DefaultResponse> createUser(@Body CreateUserRequest createUserRequest);
}
