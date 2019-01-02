package pl.edu.agh.recordingapp.rest.service;

import java.util.List;

import okhttp3.MultipartBody;
import pl.edu.agh.recordingapp.rest.request.CreateMarkRequest;
import pl.edu.agh.recordingapp.rest.response.MarkResponse;
import pl.edu.agh.recordingapp.rest.response.RecordingResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface RecordingsService {
    @Multipart
    @POST("recordings/upload")
    Call<RecordingResponse> upload(@Header("Authorization") String authorization, @Part MultipartBody.Part recording);

    @POST("recordings/{recordingId}/marks")
    Call<List<MarkResponse>> createMarks(@Header("Authorization") String authorization, @Path("recordingId") Long recordingId, @Body List<CreateMarkRequest> createMarkRequests);
}
