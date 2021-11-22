package com.webproject.flarepoint.controller.tag;

import com.webproject.flarepoint.controller.ApiResult;
import com.webproject.flarepoint.model.common.Tag;
import com.webproject.flarepoint.service.TagService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = { "http://localhost:8080", "http://172.30.1.30:8080" })
@RestController
@RequestMapping("/tag")
public class TagController {

  private final TagService tagService;

  public TagController(TagService tagService) {
    this.tagService = tagService;
  }

  @PostMapping(path = "/top")
  public ApiResult<List<Tag>> getTopTags(@RequestBody TopTagRequest topTagRequest) {
    List<Tag> tags = tagService.getTopTags(topTagRequest.getCount());
    return ApiResult.OK(tags);
  }

}
