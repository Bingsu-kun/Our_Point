package com.webproject.ourpoint.controller.marker;

import com.webproject.ourpoint.controller.ApiResult;
import com.webproject.ourpoint.errors.NotFoundException;
import com.webproject.ourpoint.model.marker.Category;
import com.webproject.ourpoint.model.marker.Marker;
import com.webproject.ourpoint.security.AuthenticationRequest;
import com.webproject.ourpoint.security.JwtAuthentication;
import com.webproject.ourpoint.service.MarkerService;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.search.SearchHit;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.webproject.ourpoint.controller.ApiResult.OK;

@Controller
@RequestMapping("/marker")
public class MarkerController {

  private final MarkerService markerService;

  public MarkerController(MarkerService markerService) {
    this.markerService = markerService;
  }

  @PostMapping(path = "/create")
  public ApiResult<MarkerDto> create(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody CreateRequest createRequest) {

    Marker marker = markerService.createMarker(
            authentication.id,
            createRequest.getName(),
            createRequest.getLatitude(),
            createRequest.getLongitude(),
            createRequest.getIsPrivate(),
            Category.of(createRequest.getCategory()),
            createRequest.getTagString(),
            createRequest.getDescription()
    );
    return OK( new MarkerDto(marker));
  }

  @GetMapping(path = "/{markerId}")
  public ApiResult<String> getOne(@PathVariable("markerId") Long markerId) {
    return OK(markerService.getMarker(markerId).orElseThrow(() -> new NotFoundException(String.class, markerId)));
  }

  @GetMapping(path = "/all")
  public ApiResult<SearchHit[]> getAll() {
    return OK(markerService.getAllMarkers().orElseThrow(() -> new NotFoundException(SearchHit.class)));
  }

  @PostMapping(path = "/search")
  public ApiResult<SearchHit[]> search(@RequestBody SearchRequestBody searchRequestBody) {
    return OK(markerService.searchByKeywordAndCategory(searchRequestBody.getKeyword(),Category.of(searchRequestBody.getCategory()))
            .orElseThrow(() -> new NotFoundException(SearchHit.class)));
  }

  @PutMapping(path = "/update")
  public ApiResult<UpdateResponse> update(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody UpdateRequestBody updateRequestBody) {
    UpdateResponse updateResponse = markerService.updateMarker(
            authentication.id,
            updateRequestBody.getMarkerId(),
            updateRequestBody.getMfId(),
            updateRequestBody.getName(),
            updateRequestBody.getLatitude(),
            updateRequestBody.getLongitude(),
            updateRequestBody.getIsPrivate(),
            Category.of(updateRequestBody.getCategory()),
            updateRequestBody.getTagString(),
            updateRequestBody.getDescription()
    );
    return OK(updateResponse);
  }

  @DeleteMapping(path = "/delete")
  public ApiResult<DeleteResponse> delete(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody DeleteRequestBody deleteRequestBody) {
    return OK(markerService.deleteMarker(authentication.id, deleteRequestBody.getMarkerId(), deleteRequestBody.getMfId()));
  }

  @PostMapping(path = "/like")
  public ApiResult<Integer> like(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody LikeRequest likeRequest) {
    return OK(markerService.like(authentication.id, likeRequest.getMarkerId()));
  }

  @GetMapping(path = "/mylikes")
  public ApiResult<List<String>> myLikes(@AuthenticationPrincipal JwtAuthentication authentication) {
    return OK(markerService.getLikedMarkers(authentication.id));
  }

}
