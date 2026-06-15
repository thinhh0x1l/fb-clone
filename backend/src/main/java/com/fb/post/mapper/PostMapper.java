package com.fb.post.mapper;

import com.fb.auth.model.User;
import com.fb.post.dto.PostResponse;
import com.fb.post.model.Post;
import com.fb.post.model.PostMedia;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper chuyển đổi dữ liệu Post
 * Chuyển đổi giữa Entity Post và PostResponse DTO
 */
@Component
public class PostMapper {

    /**
     * Alias cho toPostResponse
     */
    public PostResponse toResponse(Post post, boolean isLiked) {
        return toPostResponse(post, isLiked);
    }

    /**
     * Chuyển đổi Post entity sang PostResponse DTO
     * @param post Post entity từ database
     * @param isLiked trạng thái liked của bài viết cho người dùng hiện tại
     * @return PostResponse DTO cho API response
     */
    public PostResponse toPostResponse(Post post, boolean isLiked) {
        if (post == null) {
            return null;
        }

        List<PostResponse.MediaResponse> mediaResponses = new ArrayList<>();
        if (post.getMedia() != null) {
            mediaResponses = post.getMedia().stream()
                    .map(this::toMediaResponse)
                    .collect(Collectors.toList());
        }

        return PostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .user(toUserInfo(post.getUser()))
                .visibility(post.getVisibility())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .sharesCount(post.getSharesCount())
                .media(mediaResponses)
                .isLiked(isLiked)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    /**
     * Chuyển đổi User entity sang UserInfo nested DTO
     * Chứa thông tin cơ bản của người dùng trong bài viết
     */
    public PostResponse.UserInfo toUserInfo(User user) {
        if (user == null) {
            return null;
        }

        return PostResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .avatar(user.getAvatar())
                .build();
    }

    /**
     * Chuyển đổi PostMedia entity sang MediaResponse DTO
     * Chứa thông tin phương tiện đính kèm trong bài viết
     */
    public PostResponse.MediaResponse toMediaResponse(PostMedia media) {
        if (media == null) {
            return null;
        }

        return PostResponse.MediaResponse.builder()
                .id(media.getId())
                .url(media.getUrl())
                .thumbnailUrl(media.getThumbnailUrl())
                .type(media.getType())
                .orderIndex(media.getOrderIndex())
                .width(media.getWidth())
                .height(media.getHeight())
                .build();
    }
}
