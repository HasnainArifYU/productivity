package com.productivity.todos.service;

import com.productivity.todos.dto.CreateTaskRequest;
import com.productivity.todos.dto.TaskDto;
import com.productivity.todos.entity.TaskPriority;
import com.productivity.todos.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskService {
    
    TaskDto createTask(Long todoListId, CreateTaskRequest createTaskRequest);
    
    TaskDto getTaskById(Long id);
    
    Page<TaskDto> getTasksByTodoList(Long todoListId, Pageable pageable);
    
    Page<TaskDto> getTasksByTodoListAndStatus(Long todoListId, TaskStatus status, Pageable pageable);
    
    Page<TaskDto> getTasksByTodoListAndPriority(Long todoListId, TaskPriority priority, Pageable pageable);
    
    Page<TaskDto> searchTasksByTodoList(Long todoListId, String searchTerm, Pageable pageable);
    
    List<TaskDto> getTasksDueBetweenDates(LocalDateTime startDate, LocalDateTime endDate);
    
    List<TaskDto> getOverdueTasks();
    
    TaskDto updateTask(Long id, CreateTaskRequest updateTaskRequest);
    
    TaskDto updateTaskStatus(Long id, TaskStatus status);
    
    void deleteTask(Long id);
}
