package com.productivity.todos.dto;

import com.productivity.todos.entity.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTaskRequest {
    
    @NotBlank
    @Size(min = 1, max = 200)
    private String title;
    
    private String description;
    
    private TaskPriority priority;
    
    private LocalDateTime dueDate;
}
