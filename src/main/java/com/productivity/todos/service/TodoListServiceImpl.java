package com.productivity.todos.service;

import com.productivity.common.exception.ResourceNotFoundException;
import com.productivity.common.exception.UnauthorizedException;
import com.productivity.security.UserDetailsImpl;
import com.productivity.todos.dto.CreateTodoListRequest;
import com.productivity.todos.dto.TaskDto;
import com.productivity.todos.dto.TodoListDto;
import com.productivity.todos.entity.TodoList;
import com.productivity.todos.repository.TodoListRepository;
import com.productivity.user.entity.User;
import com.productivity.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class TodoListServiceImpl implements TodoListService {

    @Autowired
    private TodoListRepository todoListRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public TodoListDto createTodoList(CreateTodoListRequest createTodoListRequest) {
        User currentUser = getCurrentUser();
        
        TodoList todoList = TodoList.builder()
                .name(createTodoListRequest.getName())
                .description(createTodoListRequest.getDescription())
                .user(currentUser)
                .tasks(new ArrayList<>())
                .build();

        TodoList savedTodoList = todoListRepository.save(todoList);
        
        return mapTodoListToDto(savedTodoList);
    }

    @Override
    public TodoListDto getTodoListById(Long id) {
        TodoList todoList = todoListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TodoList", "id", id));
        
        // Check if the current user is the owner of the to-do list
        if (!todoList.getUser().getId().equals(getCurrentUser().getId())) {
            throw new UnauthorizedException("You are not authorized to access this to-do list");
        }
        
        return mapTodoListToDto(todoList);
    }

    @Override
    public Page<TodoListDto> getTodoListsByCurrentUser(Pageable pageable) {
        User currentUser = getCurrentUser();
        
        Page<TodoList> todoLists = todoListRepository.findByUserId(currentUser.getId(), pageable);
        
        return todoLists.map(this::mapTodoListToDto);
    }

    @Override
    public Page<TodoListDto> searchTodoLists(String query, Pageable pageable) {
        User currentUser = getCurrentUser();
        
        Page<TodoList> todoLists = todoListRepository.findByUserIdAndNameContainingIgnoreCase(
                currentUser.getId(), query, pageable);
        
        return todoLists.map(this::mapTodoListToDto);
    }

    @Override
    @Transactional
    public TodoListDto updateTodoList(Long id, CreateTodoListRequest updateTodoListRequest) {
        TodoList todoList = todoListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TodoList", "id", id));
        
        // Check if the current user is the owner of the to-do list
        if (!todoList.getUser().getId().equals(getCurrentUser().getId())) {
            throw new UnauthorizedException("You are not authorized to update this to-do list");
        }
        
        todoList.setName(updateTodoListRequest.getName());
        todoList.setDescription(updateTodoListRequest.getDescription());
        
        TodoList updatedTodoList = todoListRepository.save(todoList);
        
        return mapTodoListToDto(updatedTodoList);
    }

    @Override
    @Transactional
    public void deleteTodoList(Long id) {
        TodoList todoList = todoListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TodoList", "id", id));
        
        // Check if the current user is the owner of the to-do list
        if (!todoList.getUser().getId().equals(getCurrentUser().getId())) {
            throw new UnauthorizedException("You are not authorized to delete this to-do list");
        }
        
        todoListRepository.delete(todoList);
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));
    }
    
    private TodoListDto mapTodoListToDto(TodoList todoList) {
        return TodoListDto.builder()
                .id(todoList.getId())
                .name(todoList.getName())
                .description(todoList.getDescription())
                .createdAt(todoList.getCreatedAt())
                .updatedAt(todoList.getUpdatedAt())
                .userId(todoList.getUser().getId())
                .tasks(todoList.getTasks().stream()
                        .map(task -> TaskDto.builder()
                                .id(task.getId())
                                .title(task.getTitle())
                                .description(task.getDescription())
                                .status(task.getStatus())
                                .priority(task.getPriority())
                                .dueDate(task.getDueDate())
                                .completedAt(task.getCompletedAt())
                                .createdAt(task.getCreatedAt())
                                .updatedAt(task.getUpdatedAt())
                                .todoListId(todoList.getId())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
