package com.productivity.notes.repository;

import com.productivity.notes.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    
    List<Note> findByUserId(Long userId);
    
    Page<Note> findByUserId(Long userId, Pageable pageable);
    
    @Query("SELECT n FROM Note n WHERE n.user.id = :userId AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Note> searchNotesByUser(@Param("userId") Long userId, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT n FROM Note n JOIN n.tags t WHERE n.user.id = :userId AND LOWER(t.name) = LOWER(:tagName)")
    Page<Note> findByUserIdAndTagName(@Param("userId") Long userId, @Param("tagName") String tagName, Pageable pageable);
}
