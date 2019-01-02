package pl.edu.agh.recordingapp.rest.response;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingResponse {
    private Long id;
    private String title;
    private Timestamp uploadTime;
    private Long duration;
    private String username;
}
