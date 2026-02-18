package com.productivity.notes.service;

import com.productivity.common.exception.BadRequestException;
import com.productivity.common.exception.ResourceNotFoundException;
import com.productivity.common.exception.UnauthorizedException;
import com.productivity.notes.dto.TagDto;
import com.productivity.notes.entity.Tag;
import com.productivity.notes.repository.TagRepository;
import com.productivity.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagRepository tagRepository;

    @Override
    @Transactional
    public TagDto createTag(String name) {
        // Check if tag already exists
        if (tagRepository.existsByName(name)) {
            throw new BadRequestException("Tag with name '" + name + "' already exists");
        }
        
        Tag tag = new Tag();
        tag.setName(name);
        tag.setNotes(new HashSet<>());
        
        Tag savedTag = tagRepository.save(tag);
        
        return mapTagToDto(savedTag);
    }

    @Override
    public List<TagDto> getAllTagsByCurrentUser() {
        UserDetailsImpl userDetails = getCurrentUserDetails();
        
        List<Tag> tags = tagRepository.findTagsByUserId(userDetails.getId());
        
        return tags.stream()
                .map(this::mapTagToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", id));
        
        // Check if any of the tag's notes belong to the current user
        UserDetailsImpl userDetails = getCurrentUserDetails();
        boolean hasUserNotes = tag.getNotes().stream()
                .anyMatch(note -> note.getUser().getId().equals(userDetails.getId()));
        
        if (!hasUserNotes) {
            throw new UnauthorizedException("You are not authorized to delete this tag");
        }
        
        // Remove the tag from all notes
        tag.getNotes().forEach(note -> note.getTags().remove(tag));
        
        // Delete the tag
        tagRepository.delete(tag);
    }
    
    private UserDetailsImpl getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserDetailsImpl) authentication.getPrincipal();
    }
    
    private TagDto mapTagToDto(Tag tag) {
        return TagDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }
}
