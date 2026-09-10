package com.openmosque.modules.mosque.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.modules.mosque.dto.FavoriteMosqueResponseDto;
import com.openmosque.modules.mosque.dto.FavoriteStatusDto;
import com.openmosque.modules.mosque.service.MosqueFavoriteService;
import com.openmosque.modules.user.entity.User;
import com.openmosque.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Mosque Favorites", description = "Endpoints for bookmarking and managing favorited mosques in PostgreSQL")
public class MosqueFavoriteController {

    private final MosqueFavoriteService favoriteService;

    @Operation(summary = "Get user's favorite mosques", description = "Retrieves all mosques favorited by current authenticated user directly from PostgreSQL.")
    @GetMapping("/users/me/favorites")
    public ResponseEntity<ApiResponse<List<FavoriteMosqueResponseDto>>> getMyFavorites(
            @CurrentUser User user,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon
    ) {
        List<FavoriteMosqueResponseDto> favorites = favoriteService.getUserFavorites(user, lat, lon);
        return ResponseEntity.ok(ApiResponse.success(favorites));
    }

    @Operation(summary = "Add mosque to favorites", description = "Persists a mosque bookmark directly into PostgreSQL.")
    @PostMapping({"/users/me/favorites/{mosqueId}", "/mosques/{mosqueId}/favorite"})
    public ResponseEntity<ApiResponse<FavoriteStatusDto>> addFavorite(
            @CurrentUser User user,
            @PathVariable UUID mosqueId
    ) {
        FavoriteStatusDto status = favoriteService.addFavorite(user, mosqueId);
        return ResponseEntity.ok(ApiResponse.success(status, "Mosque added to favorites"));
    }

    @Operation(summary = "Remove mosque from favorites", description = "Removes a mosque bookmark from PostgreSQL.")
    @DeleteMapping({"/users/me/favorites/{mosqueId}", "/mosques/{mosqueId}/favorite"})
    public ResponseEntity<ApiResponse<FavoriteStatusDto>> removeFavorite(
            @CurrentUser User user,
            @PathVariable UUID mosqueId
    ) {
        FavoriteStatusDto status = favoriteService.removeFavorite(user, mosqueId);
        return ResponseEntity.ok(ApiResponse.success(status, "Mosque removed from favorites"));
    }

    @Operation(summary = "Check favorite status", description = "Checks whether current user has favorited a specific mosque in PostgreSQL.")
    @GetMapping({"/users/me/favorites/{mosqueId}/status", "/mosques/{mosqueId}/is-favorite", "/mosques/{mosqueId}/favorite"})
    public ResponseEntity<ApiResponse<FavoriteStatusDto>> getFavoriteStatus(
            @CurrentUser User user,
            @PathVariable UUID mosqueId
    ) {
        FavoriteStatusDto status = favoriteService.getFavoriteStatus(user, mosqueId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
