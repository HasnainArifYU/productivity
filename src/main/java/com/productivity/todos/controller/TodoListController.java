package com.productivity.todos.controller;

import com.productivity.todos.dto.CreateTodoListRequest;
import com.productivity.todos.dto.TodoListDto;
import com.productivity.todos.service.TodoListService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/todo-lists")
@PreAuthorize("isAuthenticated()")
public class TodoListController {

    @Autowired
    private TodoListService todoListService;

    @PostMapping
    public ResponseEntity<TodoListDto> createTodoList(@Valid @RequestBody CreateTodoListRequest createTodoListRequest) {
        TodoListDto todoListDto = todoListService.createTodoList(createTodoListRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(todoListDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoListDto> getTodoListById(@PathVariable Long id) {
        TodoListDto todoListDto = todoListService.getTodoListById(id);
        return ResponseEntity.ok(todoListDto);
    }

    @GetMapping
    public ResponseEntity<Page<TodoListDto>> getTodoLists(
            @PageableDefault(size = 10, sort = "updatedAt") Pageable pageable) {
        Page<TodoListDto> todoLists = todoListService.getTodoListsByCurrentUser(pageable);
        return ResponseEntity.ok(todoLists);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TodoListDto>> searchTodoLists(
            @RequestParam String query,
            @PageableDefault(size = 10, sort = "updatedAt") Pageable pageable) {
        Page<TodoListDto> todoLists = todoListService.searchTodoLists(query, pageable);
        return ResponseEntity.ok(todoLists);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoListDto> updateTodoList(
            @PathVariable Long id,
            @Valid @RequestBody CreateTodoListRequest updateTodoListRequest) {
        TodoListDto todoListDto = todoListService.updateTodoList(id, updateTodoListRequest);
        return ResponseEntity.ok(todoListDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodoList(@PathVariable Long id) {
        todoListService.deleteTodoList(id);
        return ResponseEntity.noContent().build();
    }
}
