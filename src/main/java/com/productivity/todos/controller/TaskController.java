package com.productivity.todos.controller;

import com.productivity.todos.dto.CreateTaskRequest;
import com.productivity.todos.dto.TaskDto;
import com.productivity.todos.entity.TaskPriority;
import com.productivity.todos.entity.TaskStatus;
import com.productivity.todos.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/todo-lists/{todoListId}/tasks")
@PreAuthorize("isAuthenticated()")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskDto> createTask(
            @PathVariable Long todoListId,
            @Valid @RequestBody CreateTaskRequest createTaskRequest) {
        TaskDto taskDto = taskService.createTask(todoListId, createTaskRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long id) {
        TaskDto taskDto = taskService.getTaskById(id);
        return ResponseEntity.ok(taskDto);
    }

    @GetMapping
    public ResponseEntity<Page<TaskDto>> getTasksByTodoList(
            @PathVariable Long todoListId,
            @PageableDefault(size = 10, sort = "dueDate") Pageable pageable) {
        Page<TaskDto> tasks = taskService.getTasksByTodoList(todoListId, pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<TaskDto>> getTasksByStatus(
            @PathVariable Long todoListId,
            @PathVariable TaskStatus status,
            @PageableDefault(size = 10, sort = "dueDate") Pageable pageable) {
        Page<TaskDto> tasks = taskService.getTasksByTodoListAndStatus(todoListId, status, pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<Page<TaskDto>> getTasksByPriority(
            @PathVariable Long todoListId,
            @PathVariable TaskPriority priority,
            @PageableDefault(size = 10, sort = "dueDate") Pageable pageable) {
        Page<TaskDto> tasks = taskService.getTasksByTodoListAndPriority(todoListId, priority, pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TaskDto>> searchTasks(
            @PathVariable Long todoListId,
            @RequestParam String query,
            @PageableDefault(size = 10, sort = "dueDate") Pageable pageable) {
        Page<TaskDto> tasks = taskService.searchTasksByTodoList(todoListId, query, pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/due-between")
    public ResponseEntity<List<TaskDto>> getTasksDueBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<TaskDto> tasks = taskService.getTasksDueBetweenDates(startDateTime, endDateTime);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<TaskDto>> getOverdueTasks() {
        List<TaskDto> tasks = taskService.getOverdueTasks();
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable Long todoListId,
            @PathVariable Long id,
            @Valid @RequestBody CreateTaskRequest updateTaskRequest) {
        TaskDto taskDto = taskService.updateTask(id, updateTaskRequest);
        return ResponseEntity.ok(taskDto);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskDto> updateTaskStatus(
            @PathVariable Long todoListId,
            @PathVariable Long id,
            @RequestParam TaskStatus status) {
        TaskDto taskDto = taskService.updateTaskStatus(id, status);
        return ResponseEntity.ok(taskDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long todoListId,
            @PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
