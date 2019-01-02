package pl.edu.agh.recordingapp.rest.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkResponse {
    private Long id;
    private Long markTime;
    private Long recordingId;
    private String name;
    private String username;
    private String recordingTitle;
}
