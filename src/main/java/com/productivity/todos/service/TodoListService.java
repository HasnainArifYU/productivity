package com.productivity.todos.service;

import com.productivity.todos.dto.CreateTodoListRequest;
import com.productivity.todos.dto.TodoListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TodoListService {
    
    TodoListDto createTodoList(CreateTodoListRequest createTodoListRequest);
    
    TodoListDto getTodoListById(Long id);
    
    Page<TodoListDto> getTodoListsByCurrentUser(Pageable pageable);
    
    Page<TodoListDto> searchTodoLists(String query, Pageable pageable);
    
    TodoListDto updateTodoList(Long id, CreateTodoListRequest updateTodoListRequest);
    
    void deleteTodoList(Long id);
}
