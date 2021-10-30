package com.webproject.ourpoint.controller.marker;

import com.webproject.ourpoint.controller.ApiResult;
import com.webproject.ourpoint.model.liked.Liked;
import com.webproject.ourpoint.model.marker.Category;
import com.webproject.ourpoint.model.marker.Marker;
import com.webproject.ourpoint.security.JwtAuthentication;
import com.webproject.ourpoint.service.MarkerService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.webproject.ourpoint.controller.ApiResult.OK;

@CrossOrigin(origins = {"http://localhost:8080", "http://172.31.44.156:8080", "https://172.31.44.156:8080"})
@RestController
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

  @PutMapping(path = "/update")
  public ApiResult<MarkerDto> update(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody UpdateRequest updateRequest) throws IOException {
    Marker updatedMarker = markerService.updateMarker(
            authentication.id,
            updateRequest.getMarkerId(),
            updateRequest.getMfId(),
            updateRequest.getName(),
            updateRequest.getLatitude(),
            updateRequest.getLongitude(),
            updateRequest.getIsPrivate(),
            Category.of(updateRequest.getCategory()),
            updateRequest.getTagString(),
            updateRequest.getDescription()
    );

    return OK(new MarkerDto(updatedMarker));
  }

  @DeleteMapping(path = "/delete")
  public ApiResult<String> delete(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody DeleteRequest deleteRequest) throws IOException {
    markerService.deleteMarker(authentication.id,deleteRequest.getMarkerId(), deleteRequest.getMfId());
    return OK("deleted");
  }

  @PostMapping(path = "/like")
  public ApiResult<Liked> like(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody LikeRequest likeRequest) {
    return OK(markerService.like(authentication.id, likeRequest.getMarkerId()));
  }

  @PostMapping(path = "/dislike")
  public ApiResult<String> dislike(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody LikeRequest likeRequest) {
    markerService.dislike(authentication.id, likeRequest.getMarkerId());
    return OK("deleted");
  }

  @GetMapping(path = "/likes")
  public ApiResult<List<Integer>> likes(@RequestBody AllLikedRequest allLikedRequest) {
    return OK(markerService.allMarkersLikeCount(allLikedRequest.getMarkerIds()));
  }

  @GetMapping(path = "/thiscount")
  public ApiResult<Integer> thisMarkerCount(@RequestBody LikeRequest likeRequest) {
    return OK(markerService.markerLikeCount(likeRequest.getMarkerId()));
  }

  @GetMapping(path = "/mylikecount")
  public ApiResult<Integer> myLikeCount(@AuthenticationPrincipal JwtAuthentication authentication) {
    return OK(markerService.fisherLikeCount(authentication.id));
  }

  @GetMapping(path = "/mylikelist")
  public ApiResult<List<Marker>> myLikeList(@AuthenticationPrincipal JwtAuthentication authentication) {
    return OK(markerService.myLikeList(authentication.id));
  }

  @GetMapping(path = "/all")
  public ApiResult<List<Marker>> all() {
    return OK(markerService.getAll());
  }
}
