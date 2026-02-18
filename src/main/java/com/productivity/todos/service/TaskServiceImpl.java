package com.productivity.todos.service;

import com.productivity.common.exception.ResourceNotFoundException;
import com.productivity.common.exception.UnauthorizedException;
import com.productivity.security.UserDetailsImpl;
import com.productivity.todos.dto.CreateTaskRequest;
import com.productivity.todos.dto.TaskDto;
import com.productivity.todos.entity.Task;
import com.productivity.todos.entity.TaskPriority;
import com.productivity.todos.entity.TaskStatus;
import com.productivity.todos.entity.TodoList;
import com.productivity.todos.repository.TaskRepository;
import com.productivity.todos.repository.TodoListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TodoListRepository todoListRepository;

    @Override
    @Transactional
    public TaskDto createTask(Long todoListId, CreateTaskRequest createTaskRequest) {
        TodoList todoList = todoListRepository.findById(todoListId)
                .orElseThrow(() -> new ResourceNotFoundException("TodoList", "id", todoListId));
        
        // Check if the current user is the owner of the to-do list
        UserDetailsImpl userDetails = getCurrentUserDetails();
        if (!todoList.getUser().getId().equals(userDetails.getId())) {
            throw new UnauthorizedException("You are not authorized to add tasks to this to-do list");
        }
        
        Task task = Task.builder()
                .title(createTaskRequest.getTitle())
                .description(createTaskRequest.getDescription())
                .priority(createTaskRequest.getPriority() != null ? createTaskRequest.getPriority() : TaskPriority.MEDIUM)
                .status(TaskStatus.PENDING)
                .dueDate(createTaskRequest.getDueDate())
                .todoList(todoList)
                .build();
        
        Task savedTask = taskRepository.save(task);
        
        return mapTaskToDto(savedTask);
    }

    @Override
    public TaskDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        
        // Check if the current user is the owner of the task
        UserDetailsImpl userDetails = getCurrentUserDetails();
        if (!task.getTodoList().getUser().getId().equals(userDetails.getId())) {
            throw new UnauthorizedException("You are not authorized to access this task");
        }
        
        return mapTaskToDto(task);
    }

    @Override
    public Page<TaskDto> getTasksByTodoList(Long todoListId, Pageable pageable) {
        // Check if the to-do list exists and belongs to the current user
        TodoList todoList = todoListRepository.findById(todoListId)
                .orElseThrow(() -> new ResourceNotFoundException("TodoList", "id", todoListId));
        
        UserDetailsImpl userDetails = getCurrentUserDetails();
        if (!todoList.getUser().getId().equals(userDetails.getId())) {
            throw new UnauthorizedException("You are not authorized to access tasks in this to-do list");
        }
        
        Page<Task> tasks = taskRepository.findByTodoListId(todoListId, pageable);
        
        return tasks.map(this::mapTaskToDto);
    }

    @Override
    public Page<TaskDto> getTasksByTodoListAndStatus(Long todoListId, TaskStatus status, Pageable pageable) {
        // Check if the to-do list exists and belongs to the current user
        TodoList todoList = todoListRepository.findById(todoListId)
                .orElseThrow(() -> new ResourceNotFoundException("TodoList", "id", todoListId));
        
        UserDetailsImpl userDetails = getCurrentUserDetails();
        if (!todoList.getUser().getId().equals(userDetails.getId())) {
            throw new UnauthorizedException("You are not authorized to access tasks in this to-do list");
        }
        
        Page<Task> tasks = taskRepository.findByTodoListIdAndStatus(todoListId, status, pageable);
        
        return tasks.map(this::mapTaskToDto);
    }

    @Override
    public Page<TaskDto> getTasksByTodoListAndPriority(Long todoListId, TaskPriority priority, Pageable pageable) {
        // Check if the to-do list exists and belongs to the current user
        TodoList todoList = todoListRepository.findById(todoListId)
                .orElseThrow(() -> new ResourceNotFoundException("TodoList", "id", todoListId));
        
        UserDetailsImpl userDetails = getCurrentUserDetails();
        if (!todoList.getUser().getId().equals(userDetails.getId())) {
            throw new UnauthorizedException("You are not authorized to access tasks in this to-do list");
        }
        
        Page<Task> tasks = taskRepository.findByTodoListIdAndPriority(todoListId, priority, pageable);
        
        return tasks.map(this::mapTaskToDto);
    }

    @Override
    public Page<TaskDto> searchTasksByTodoList(Long todoListId, String searchTerm, Pageable pageable) {
        // Check if the to-do list exists and belongs to the current user
        TodoList todoList = todoListRepository.findById(todoListId)
                .orElseThrow(() -> new ResourceNotFoundException("TodoList", "id", todoListId));
        
        UserDetailsImpl userDetails = getCurrentUserDetails();
        if (!todoList.getUser().getId().equals(userDetails.getId())) {
            throw new UnauthorizedException("You are not authorized to access tasks in this to-do list");
        }
        
        Page<Task> tasks = taskRepository.searchTasksByTodoList(todoListId, searchTerm, pageable);
        
        return tasks.map(this::mapTaskToDto);
    }

    @Override
    public List<TaskDto> getTasksDueBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        UserDetailsImpl userDetails = getCurrentUserDetails();
        
        List<Task> tasks = taskRepository.findTasksDueBetweenDates(userDetails.getId(), startDate, endDate);
        
        return tasks.stream()
                .map(this::mapTaskToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskDto> getOverdueTasks() {
        UserDetailsImpl userDetails = getCurrentUserDetails();
        
        List<Task> tasks = taskRepository.findOverdueTasks(userDetails.getId(), LocalDateTime.now());
        
        return tasks.stream()
                .map(this::mapTaskToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskDto updateTask(Long id, CreateTaskRequest updateTaskRequest) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        
        // Check if the current user is the owner of the task
        UserDetailsImpl userDetails = getCurrentUserDetails();
        if (!task.getTodoList().getUser().getId().equals(userDetails.getId())) {
            throw new UnauthorizedException("You are not authorized to update this task");
        }
        
        task.setTitle(updateTaskRequest.getTitle());
        task.setDescription(updateTaskRequest.getDescription());
        
        if (updateTaskRequest.getPriority() != null) {
            task.setPriority(updateTaskRequest.getPriority());
        }
        
        task.setDueDate(updateTaskRequest.getDueDate());
        
        Task updatedTask = taskRepository.save(task);
        
        return mapTaskToDto(updatedTask);
    }

    @Override
    @Transactional
    public TaskDto updateTaskStatus(Long id, TaskStatus status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        
        // Check if the current user is the owner of the task
        UserDetailsImpl userDetails = getCurrentUserDetails();
        if (!task.getTodoList().getUser().getId().equals(userDetails.getId())) {
            throw new UnauthorizedException("You are not authorized to update this task");
        }
        
        task.setStatus(status);
        
        // If the task is marked as completed, set the completion date
        if (status == TaskStatus.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }
        
        Task updatedTask = taskRepository.save(task);
        
        return mapTaskToDto(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        
        // Check if the current user is the owner of the task
        UserDetailsImpl userDetails = getCurrentUserDetails();
        if (!task.getTodoList().getUser().getId().equals(userDetails.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this task");
        }
        
        taskRepository.delete(task);
    }
    
    private UserDetailsImpl getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserDetailsImpl) authentication.getPrincipal();
    }
    
    private TaskDto mapTaskToDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .todoListId(task.getTodoList().getId())
                .build();
    }
}
