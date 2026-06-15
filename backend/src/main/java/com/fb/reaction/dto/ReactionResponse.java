package com.fb.reaction.dto;

import com.fb.common.enums.ReactionType;
import lombok.*;

import java.util.Map;

/**
 * DTO dùng để trả về thông tin phản ứng (like, love, haha, ...) của bài viết hoặc bình luận.
 * Bao gồm trạng thái phản ứng hiện tại và thống kê tổng hợp.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionResponse {

    /** người dùng hiện tại đã phản ứng hay chưa */
    private boolean reacted;

    /** loại phản ứng hiện tại của người dùng (nếu có) */
    private ReactionType reactionType;

    /** thống kê số lượng theo từng loại phản ứng */
    private Map<ReactionType, Long> counts;

    /** tổng số lượng phản ứng */
    private long totalReactions;
}
