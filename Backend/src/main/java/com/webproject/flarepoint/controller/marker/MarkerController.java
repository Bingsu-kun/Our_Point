package com.webproject.flarepoint.controller.marker;

import com.webproject.flarepoint.controller.ApiResult;
import com.webproject.flarepoint.model.liked.Liked;
import com.webproject.flarepoint.model.marker.Marker;
import com.webproject.flarepoint.security.JwtAuthentication;
import com.webproject.flarepoint.service.LikeService;
import com.webproject.flarepoint.service.MarkerService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.webproject.flarepoint.controller.ApiResult.OK;

@CrossOrigin(origins = { "http://localhost:8080", "https://localhost:8080","https://flarepoint.netlify.app" })
@RestController
@RequestMapping("/marker")
public class MarkerController {

  private final MarkerService markerService;
  private final LikeService likeService;

  public MarkerController(MarkerService markerService, LikeService likeService) {
    this.markerService = markerService;
    this.likeService = likeService;
  }

  @PostMapping(path = "/create")
  public ApiResult<MarkerDto> create(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody CreateRequest createRequest) {

    Marker marker = markerService.createMarker(
            authentication.id,
            createRequest.getName(),
            createRequest.getLatitude(),
            createRequest.getLongitude(),
            createRequest.getPlace_addr(),
            createRequest.getIsPrivate(),
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
            updateRequest.getPlace_addr(),
            updateRequest.getIsPrivate(),
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

  @GetMapping(path = "/mymarkers")
  public ApiResult<List<Marker>> myMarkers(@AuthenticationPrincipal JwtAuthentication authentication) {
    return OK(markerService.myMarkers(authentication.id));
  }

  @PostMapping(path = "/like")
  public ApiResult<Liked> like(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody LikeRequest likeRequest) {
    return OK(likeService.like(authentication.id, likeRequest.getMarkerId()));
  }

  @DeleteMapping(path = "/dislike")
  public ApiResult<String> dislike(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody LikeRequest likeRequest) {
    likeService.dislike(authentication.id, likeRequest.getMarkerId());
    return OK("deleted");
  }

  @PostMapping(path = "/likes")
  public ApiResult<List<Integer>> likes(@RequestBody AllLikedRequest allLikedRequest) {
    return OK(likeService.allMarkersLikeCount(allLikedRequest.getMarkerIds()));
  }

  @PostMapping(path = "/thiscount")
  public ApiResult<Integer> thisMarkerCount(@RequestBody LikeRequest likeRequest) {
    return OK(likeService.markerLikeCount(likeRequest.getMarkerId()));
  }

  @GetMapping(path = "/mylikecount")
  public ApiResult<Integer> myLikeCount(@AuthenticationPrincipal JwtAuthentication authentication) {
    return OK(likeService.userLikeCount(authentication.id));
  }

  @GetMapping(path = "/mylikelist")
  public ApiResult<List<Marker>> myLikeList(@AuthenticationPrincipal JwtAuthentication authentication) {
    return OK(likeService.myLikeList(authentication.id));
  }

  @GetMapping(path = "/all")
  public ApiResult<List<Marker>> all() {
    return OK(markerService.getAll());
  }
}
