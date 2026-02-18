package com.productivity.notes.service;

import com.productivity.notes.dto.CreateNoteRequest;
import com.productivity.notes.dto.NoteDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoteService {
    
    NoteDto createNote(CreateNoteRequest createNoteRequest);
    
    NoteDto getNoteById(Long id);
    
    Page<NoteDto> getNotesByCurrentUser(Pageable pageable);
    
    Page<NoteDto> searchNotesByCurrentUser(String searchTerm, Pageable pageable);
    
    Page<NoteDto> getNotesByTag(String tagName, Pageable pageable);
    
    NoteDto updateNote(Long id, CreateNoteRequest updateNoteRequest);
    
    void deleteNote(Long id);
}
