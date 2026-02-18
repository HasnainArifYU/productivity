package com.productivity.notes.service;

import com.productivity.common.exception.ResourceNotFoundException;
import com.productivity.common.exception.UnauthorizedException;
import com.productivity.notes.dto.CreateNoteRequest;
import com.productivity.notes.dto.NoteDto;
import com.productivity.notes.dto.TagDto;
import com.productivity.notes.entity.Note;
import com.productivity.notes.entity.Tag;
import com.productivity.notes.repository.NoteRepository;
import com.productivity.notes.repository.TagRepository;
import com.productivity.security.UserDetailsImpl;
import com.productivity.user.entity.User;
import com.productivity.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NoteServiceImpl implements NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public NoteDto createNote(CreateNoteRequest createNoteRequest) {
        User currentUser = getCurrentUser();
        
        Note note = Note.builder()
                .title(createNoteRequest.getTitle())
                .content(createNoteRequest.getContent())
                .user(currentUser)
                .build();

        // Add tags
        if (createNoteRequest.getTags() != null && !createNoteRequest.getTags().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            
            for (String tagName : createNoteRequest.getTags()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElse(new Tag(null, tagName, new HashSet<>()));
                tags.add(tag);
            }
            
            note.setTags(tags);
        }

        Note savedNote = noteRepository.save(note);
        
        return mapNoteToDto(savedNote);
    }

    @Override
    public NoteDto getNoteById(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", id));
        
        // Check if the current user is the owner of the note
        if (!note.getUser().getId().equals(getCurrentUser().getId())) {
            throw new UnauthorizedException("You are not authorized to access this note");
        }
        
        return mapNoteToDto(note);
    }

    @Override
    public Page<NoteDto> getNotesByCurrentUser(Pageable pageable) {
        User currentUser = getCurrentUser();
        
        Page<Note> notes = noteRepository.findByUserId(currentUser.getId(), pageable);
        
        return notes.map(this::mapNoteToDto);
    }

    @Override
    public Page<NoteDto> searchNotesByCurrentUser(String searchTerm, Pageable pageable) {
        User currentUser = getCurrentUser();
        
        Page<Note> notes = noteRepository.searchNotesByUser(currentUser.getId(), searchTerm, pageable);
        
        return notes.map(this::mapNoteToDto);
    }

    @Override
    public Page<NoteDto> getNotesByTag(String tagName, Pageable pageable) {
        User currentUser = getCurrentUser();
        
        Page<Note> notes = noteRepository.findByUserIdAndTagName(currentUser.getId(), tagName, pageable);
        
        return notes.map(this::mapNoteToDto);
    }

    @Override
    @Transactional
    public NoteDto updateNote(Long id, CreateNoteRequest updateNoteRequest) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", id));
        
        // Check if the current user is the owner of the note
        if (!note.getUser().getId().equals(getCurrentUser().getId())) {
            throw new UnauthorizedException("You are not authorized to update this note");
        }
        
        note.setTitle(updateNoteRequest.getTitle());
        note.setContent(updateNoteRequest.getContent());
        
        // Update tags
        if (updateNoteRequest.getTags() != null) {
            Set<Tag> tags = new HashSet<>();
            
            for (String tagName : updateNoteRequest.getTags()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElse(new Tag(null, tagName, new HashSet<>()));
                tags.add(tag);
            }
            
            note.setTags(tags);
        }
        
        Note updatedNote = noteRepository.save(note);
        
        return mapNoteToDto(updatedNote);
    }

    @Override
    @Transactional
    public void deleteNote(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", id));
        
        // Check if the current user is the owner of the note
        if (!note.getUser().getId().equals(getCurrentUser().getId())) {
            throw new UnauthorizedException("You are not authorized to delete this note");
        }
        
        noteRepository.delete(note);
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));
    }
    
    private NoteDto mapNoteToDto(Note note) {
        Set<TagDto> tagDtos = note.getTags().stream()
                .map(tag -> TagDto.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .build())
                .collect(Collectors.toSet());
        
        return NoteDto.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .userId(note.getUser().getId())
                .tags(tagDtos)
                .build();
    }
}
