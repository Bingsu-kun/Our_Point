package com.webproject.flarepoint.service;

import com.webproject.flarepoint.model.common.Tag;
import com.webproject.flarepoint.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TagService {

  private TagRepository tagRepository;

  public TagService(TagRepository tagRepository) {
    this.tagRepository = tagRepository;
  }

  public List<Tag> getTopTags(int count) {
    List<Tag> topTags = tagRepository.topTags();
    List<Tag> returnList = new ArrayList<>();
    int rank = Math.min(topTags.size(), count);
    for (int i = 0; i < rank; i++) {
      returnList.add(topTags.get(i));
    }
    return returnList;
  }

  public List<Tag> getAllTags() {
    return tagRepository.findAll();
  }

}
