package com.productivity.notes.service;

import com.productivity.notes.dto.TagDto;

import java.util.List;

public interface TagService {
    
    TagDto createTag(String name);
    
    List<TagDto> getAllTagsByCurrentUser();
    
    void deleteTag(Long id);
}
