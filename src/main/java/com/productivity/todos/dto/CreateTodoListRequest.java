package com.productivity.todos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTodoListRequest {
    
    @NotBlank
    @Size(min = 1, max = 100)
    private String name;
    
    private String description;
}
