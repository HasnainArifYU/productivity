package com.productivity.notes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateNoteRequest {
    
    @NotBlank
    @Size(min = 1, max = 100)
    private String title;
    
    private String content;
    
    private Set<String> tags;
}
