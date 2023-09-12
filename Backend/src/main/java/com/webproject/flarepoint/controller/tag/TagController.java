package com.webproject.flarepoint.controller.tag;

import com.webproject.flarepoint.controller.ApiResult;
import com.webproject.flarepoint.model.common.Tag;
import com.webproject.flarepoint.service.TagService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = { "http://localhost:8080","https://localhost:8080","https://flarepoint.netlify.app" })
@RestController
@RequestMapping("/tag")
public class TagController {

  private final TagService tagService;

  public TagController(TagService tagService) {
    this.tagService = tagService;
  }

  @GetMapping(path = "/top/{count}")
  public ApiResult<List<Tag>> getTopTags(@PathVariable(value = "count") int count) {
    List<Tag> tags = tagService.getTopTags(count);
    return ApiResult.OK(tags);
  }

  @GetMapping(path = "/all")
  public ApiResult<List<Tag>> getAllTags() {
    return ApiResult.OK(tagService.getAllTags());
  }

}
