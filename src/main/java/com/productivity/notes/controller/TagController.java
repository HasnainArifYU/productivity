package com.productivity.notes.controller;

import com.productivity.notes.dto.TagDto;
import com.productivity.notes.service.TagService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@PreAuthorize("isAuthenticated()")
public class TagController {

    @Autowired
    private TagService tagService;

    @PostMapping
    public ResponseEntity<TagDto> createTag(@RequestParam @NotBlank String name) {
        TagDto tagDto = tagService.createTag(name);
        return ResponseEntity.status(HttpStatus.CREATED).body(tagDto);
    }

    @GetMapping
    public ResponseEntity<List<TagDto>> getAllTags() {
        List<TagDto> tags = tagService.getAllTagsByCurrentUser();
        return ResponseEntity.ok(tags);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
