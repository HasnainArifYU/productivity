package com.productivity.notes.controller;

import com.productivity.notes.dto.CreateNoteRequest;
import com.productivity.notes.dto.NoteDto;
import com.productivity.notes.service.NoteService;
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
@RequestMapping("/api/notes")
@PreAuthorize("isAuthenticated()")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @PostMapping
    public ResponseEntity<NoteDto> createNote(@Valid @RequestBody CreateNoteRequest createNoteRequest) {
        NoteDto noteDto = noteService.createNote(createNoteRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(noteDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteDto> getNoteById(@PathVariable Long id) {
        NoteDto noteDto = noteService.getNoteById(id);
        return ResponseEntity.ok(noteDto);
    }

    @GetMapping
    public ResponseEntity<Page<NoteDto>> getNotesByCurrentUser(
            @PageableDefault(size = 10, sort = "updatedAt") Pageable pageable) {
        Page<NoteDto> notes = noteService.getNotesByCurrentUser(pageable);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<NoteDto>> searchNotes(
            @RequestParam String query,
            @PageableDefault(size = 10, sort = "updatedAt") Pageable pageable) {
        Page<NoteDto> notes = noteService.searchNotesByCurrentUser(query, pageable);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/tags/{tagName}")
    public ResponseEntity<Page<NoteDto>> getNotesByTag(
            @PathVariable String tagName,
            @PageableDefault(size = 10, sort = "updatedAt") Pageable pageable) {
        Page<NoteDto> notes = noteService.getNotesByTag(tagName, pageable);
        return ResponseEntity.ok(notes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteDto> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody CreateNoteRequest updateNoteRequest) {
        NoteDto noteDto = noteService.updateNote(id, updateNoteRequest);
        return ResponseEntity.ok(noteDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
}
