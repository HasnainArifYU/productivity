package com.productivity.todos.repository;

import com.productivity.todos.entity.TodoList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoListRepository extends JpaRepository<TodoList, Long> {
    
    List<TodoList> findByUserId(Long userId);
    
    Page<TodoList> findByUserId(Long userId, Pageable pageable);
    
    Page<TodoList> findByUserIdAndNameContainingIgnoreCase(Long userId, String name, Pageable pageable);
}
