package com.productivity.notes.repository;

import com.productivity.notes.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    Optional<Tag> findByName(String name);
    
    @Query("SELECT t FROM Tag t JOIN t.notes n WHERE n.user.id = :userId GROUP BY t")
    List<Tag> findTagsByUserId(Long userId);
    
    boolean existsByName(String name);
}
