package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Advertisement {
    private Long id;
    private String title;
    private String content;
    private String imagePath;
    private LocalDateTime scheduledTime;
    private boolean isSent;
}

