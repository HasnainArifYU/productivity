package com.productivity.todos.repository;

import com.productivity.todos.entity.Task;
import com.productivity.todos.entity.TaskPriority;
import com.productivity.todos.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByTodoListId(Long todoListId);
    
    Page<Task> findByTodoListId(Long todoListId, Pageable pageable);
    
    Page<Task> findByTodoListIdAndStatus(Long todoListId, TaskStatus status, Pageable pageable);
    
    Page<Task> findByTodoListIdAndPriority(Long todoListId, TaskPriority priority, Pageable pageable);
    
    @Query("SELECT t FROM Task t WHERE t.todoList.id = :todoListId AND LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Task> searchTasksByTodoList(@Param("todoListId") Long todoListId, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT t FROM Task t WHERE t.todoList.user.id = :userId AND t.dueDate BETWEEN :startDate AND :endDate ORDER BY t.dueDate ASC")
    List<Task> findTasksDueBetweenDates(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Task t WHERE t.todoList.user.id = :userId AND t.dueDate < :now AND t.status <> 'COMPLETED'")
    List<Task> findOverdueTasks(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
