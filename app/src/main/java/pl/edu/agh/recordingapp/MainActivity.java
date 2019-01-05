package pl.edu.agh.recordingapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pl.edu.agh.recordingapp.rest.request.CreateMarkRequest;
import pl.edu.agh.recordingapp.rest.response.MarkResponse;
import pl.edu.agh.recordingapp.rest.response.RecordingResponse;
import pl.edu.agh.recordingapp.rest.service.RecordingsService;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final int GET_BASIC_PERMISSIONS = 3;

    private boolean isRecording = false;
    private long recordingTime = 0;
    private String token;
    private MediaRecorder myAudioRecorder;
    private String outputFile;
    private RecordingsService recordingsService;
    private List<CreateMarkRequest> createMarkRequests;
    private boolean isDarkMode = false;

    private Button mRecordButton;
    private Button mNowButton;
    private Button mThirtySecondsButton;
    private Button mTwoMinutesButton;
    private Button mFiveMinutesButton;
    private Button mDarkModeButton;
    private ImageView mBlackScreen;
    private LinearLayout mForm;

    private int numberOfClicks = 0;
    private boolean threadStarted = false;
    private final int TAP_DELAY = 400;

    private int firstTimeValue = 1000 * 30;
    private int secondTimeValue = 1000 * 60 * 2;
    private int thirdTimeValue = 1000 * 60 * 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        token = getIntent().getStringExtra("token");

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl(LoginActivity.SERVER_ADDRESS)
                .build();

        recordingsService = retrofit.create(RecordingsService.class);

        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.m4a";

        mRecordButton = findViewById(R.id.start_recording);
        mRecordButton.setOnClickListener(this::record);

        mNowButton = findViewById(R.id.now_button);
        mNowButton.setOnClickListener(view -> addMark(0));

        mThirtySecondsButton = findViewById(R.id.thirty_seconds_button);
        mThirtySecondsButton.setOnClickListener(view -> addMark(firstTimeValue));

        mTwoMinutesButton = findViewById(R.id.two_minutes_button);
        mTwoMinutesButton.setOnClickListener(view -> addMark(secondTimeValue));

        mFiveMinutesButton = findViewById(R.id.five_minutes_button);
        mFiveMinutesButton.setOnClickListener(view -> addMark(thirdTimeValue));

        mDarkModeButton = findViewById(R.id.dark_mode_button);
        mDarkModeButton.setOnClickListener(view -> enableDarkMode());

        mBlackScreen = findViewById(R.id.black_screen);

        mBlackScreen.setOnClickListener(v -> detectBlackScreenTaps());

        mForm = findViewById(R.id.form_layout);
    }

    private void detectBlackScreenTaps() {
        ++numberOfClicks;

        if(!threadStarted){
            new Thread(() -> {
                threadStarted = true;
                try {
                    int currentClicks;
                    do {
                        currentClicks = numberOfClicks;
                        Thread.sleep(TAP_DELAY);
                    } while(currentClicks < numberOfClicks);

                    switch(numberOfClicks) {
                        case 1: {addMark(0); break;}
                        case 2: {addMark(firstTimeValue); break;}
                        case 3: {addMark(secondTimeValue); break;}
                        case 4: {addMark(thirdTimeValue); break;}
                        default: break;
                    }

                    numberOfClicks = 0;
                    threadStarted = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void enableDarkMode() {
        isDarkMode = true;

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        mForm.setVisibility(View.GONE);
        mBlackScreen.setVisibility(View.VISIBLE);
    }

    private void disableDarkMode() {
        isDarkMode = false;

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mBlackScreen.setVisibility(View.GONE);
        mForm.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (isDarkMode) {
            disableDarkMode();
        } else {
            super.onBackPressed();
        }
    }

    private void addMark(long timeBefore) {
        long now = System.currentTimeMillis();
        long markTime = (now - timeBefore) - recordingTime;
        if (markTime < 0) markTime = 0;

        createMarkRequests.add(CreateMarkRequest.builder()
                .name("Unnamed")
                .markTime(markTime)
                .build());

        long secondsBefore = ((now - (recordingTime + markTime)) / 1000);
        if (secondsBefore > 0) {
            MainActivity.this.runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Mark created " + secondsBefore + " seconds earlier", Toast.LENGTH_SHORT).show());
        } else {
            MainActivity.this.runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Mark created at current time", Toast.LENGTH_SHORT).show());
        }
    }

    private MediaRecorder getAudioRecorder() {
        MediaRecorder mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(outputFile);
        return mediaRecorder;
    }

    private void checkPermissions() {
        List<String> neededPermissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.RECORD_AUDIO);
        }

        if (neededPermissions.size() != 0) {
            ActivityCompat.requestPermissions(this, neededPermissions.toArray(new String[neededPermissions.size()]), GET_BASIC_PERMISSIONS);
        }
    }

    private void record(View view) {
        if (isRecording) {
            myAudioRecorder.stop();
            myAudioRecorder.release();
            myAudioRecorder = null;

            Toast.makeText(getApplicationContext(), "Uploading file...", Toast.LENGTH_LONG).show();

            (new UploadRecordingTask()).execute((Void) null);

            setMarkButtonsVisibility(View.GONE);
            mRecordButton.setText(getString(R.string.start_recording));
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            isRecording = !isRecording;
        } else {
            try {
                createMarkRequests = new ArrayList<>();
                myAudioRecorder = getAudioRecorder();
                myAudioRecorder.prepare();
                myAudioRecorder.start();
                recordingTime = System.currentTimeMillis();
                setMarkButtonsVisibility(View.VISIBLE);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                isRecording = !isRecording;
            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
            mRecordButton.setText(getString(R.string.stop_recording));
        }
    }

    private void setMarkButtonsVisibility(int visibility) {
        mNowButton.setVisibility(visibility);
        mThirtySecondsButton.setVisibility(visibility);
        mTwoMinutesButton.setVisibility(visibility);
        mFiveMinutesButton.setVisibility(visibility);
        mDarkModeButton.setVisibility(visibility);
    }

    private class UploadRecordingTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            File recording = new File(outputFile);

            RequestBody filePart = RequestBody.create(
                    MediaType.get("audio/mp4"),
                    recording
            );

            MultipartBody.Part file = MultipartBody.Part.createFormData("file", recording.getName(), filePart);

            Call<RecordingResponse> upload = recordingsService.upload(token, file);
            try {
                Response<RecordingResponse> fileUploadResponse = upload.execute();
                if (fileUploadResponse.isSuccessful()) {
                    Call<List<MarkResponse>> markResponseCall = recordingsService.createMarks(token, fileUploadResponse.body().getId(), createMarkRequests);
                    Response<List<MarkResponse>> marksResponse = markResponseCall.execute();
                    if (marksResponse.isSuccessful()) {
                        return true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                Toast.makeText(getApplicationContext(), "File successfully uploaded", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "There was a problem during file upload", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
        }
    }
}
