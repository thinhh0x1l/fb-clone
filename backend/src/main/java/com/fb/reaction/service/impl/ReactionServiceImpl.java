package com.fb.reaction.service.impl;

import com.fb.common.enums.ReactionType;
import com.fb.reaction.model.Reaction;
import com.fb.reaction.repository.ReactionRepository;
import com.fb.reaction.dto.ReactionRequest;
import com.fb.reaction.dto.ReactionResponse;
import com.fb.reaction.service.ReactionService;
import com.fb.post.model.Post;
import com.fb.post.repository.PostRepository;
import com.fb.auth.model.User;
import com.fb.auth.repository.UserRepository;
import com.fb.common.exception.ResourceNotFoundException;
import com.fb.infrastructure.cache.MultiTierCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReactionServiceImpl implements ReactionService {

    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MultiTierCache cache;

    private static final String REACTION_COUNT_PREFIX = "reaction:count:";
    private static final String USER_REACTION_PREFIX = "user:reaction:";
    private static final int REACTION_TTL = 300;

    @Override
    @Transactional
    public ReactionResponse togglePostReaction(Long postId, Long userId, ReactionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));

        com.fb.common.enums.ReactionType type = request.getType();
        String countKey = REACTION_COUNT_PREFIX + "post:" + postId;
        String userReactionKey = USER_REACTION_PREFIX + userId + ":post:" + postId;

        Reaction existingReaction = reactionRepository.findByUserIdAndPostId(userId, postId).orElse(null);

        ReactionType userReactionType = null;

        if (existingReaction != null) {
            if (existingReaction.getType() == type) {
                existingReaction.softDelete();
                reactionRepository.save(existingReaction);
                cache.delete(userReactionKey);
                userReactionType = null;
            } else {
                existingReaction.setType(type);
                reactionRepository.save(existingReaction);
                cache.set(userReactionKey, type.name(), REACTION_TTL);
                userReactionType = type;
            }
        } else {
            Reaction reaction = new Reaction();
            reaction.setUser(user);
            reaction.setPost(post);
            reaction.setType(type);
            reactionRepository.save(reaction);
            cache.set(userReactionKey, type.name(), REACTION_TTL);
            userReactionType = type;
        }

        Map<ReactionType, Long> counts = getCounts(countKey);

        return ReactionResponse.builder()
                .reacted(userReactionType != null)
                .reactionType(userReactionType)
                .counts(counts)
                .totalReactions(counts.values().stream().mapToLong(Long::longValue).sum())
                .build();
    }

    @Override
    @Transactional
    public ReactionResponse toggleCommentReaction(Long commentId, Long userId, ReactionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        String countKey = REACTION_COUNT_PREFIX + "comment:" + commentId;
        String userReactionKey = USER_REACTION_PREFIX + userId + ":comment:" + commentId;

        Reaction existingReaction = reactionRepository.findByUserIdAndCommentId(userId, commentId).orElse(null);
        com.fb.common.enums.ReactionType type = request.getType();

        ReactionType userReactionType = null;

        if (existingReaction != null) {
            if (existingReaction.getType() == type) {
                existingReaction.softDelete();
                reactionRepository.save(existingReaction);
                cache.delete(userReactionKey);
                userReactionType = null;
            } else {
                existingReaction.setType(type);
                reactionRepository.save(existingReaction);
                cache.set(userReactionKey, type.name(), REACTION_TTL);
                userReactionType = type;
            }
        } else {
            Reaction reaction = new Reaction();
            reaction.setUser(user);
            reaction.setType(type);
            reactionRepository.save(reaction);
            cache.set(userReactionKey, type.name(), REACTION_TTL);
            userReactionType = type;
        }

        Map<ReactionType, Long> counts = getCounts(countKey);

        return ReactionResponse.builder()
                .reacted(userReactionType != null)
                .reactionType(userReactionType)
                .counts(counts)
                .totalReactions(counts.values().stream().mapToLong(Long::longValue).sum())
                .build();
    }

    @Override
    public ReactionResponse getPostReactions(Long postId, Long userId) {
        String countKey = REACTION_COUNT_PREFIX + "post:" + postId;
        String userReactionKey = USER_REACTION_PREFIX + userId + ":post:" + postId;

        Map<ReactionType, Long> counts = getCounts(countKey);
        String cachedType = cache.get(userReactionKey, String.class);
        ReactionType userReactionType = cachedType != null ? ReactionType.valueOf(cachedType) : null;

        return ReactionResponse.builder()
                .reacted(userReactionType != null)
                .reactionType(userReactionType)
                .counts(counts)
                .totalReactions(counts.values().stream().mapToLong(Long::longValue).sum())
                .build();
    }

    @Override
    public ReactionResponse getCommentReactions(Long commentId, Long userId) {
        String countKey = REACTION_COUNT_PREFIX + "comment:" + commentId;
        String userReactionKey = USER_REACTION_PREFIX + userId + ":comment:" + commentId;

        Map<ReactionType, Long> counts = getCounts(countKey);
        String cachedType = cache.get(userReactionKey, String.class);
        ReactionType userReactionType = cachedType != null ? ReactionType.valueOf(cachedType) : null;

        return ReactionResponse.builder()
                .reacted(userReactionType != null)
                .reactionType(userReactionType)
                .counts(counts)
                .totalReactions(counts.values().stream().mapToLong(Long::longValue).sum())
                .build();
    }

    @Override
    public Map<ReactionType, Long> getPostReactionCounts(Long postId) {
        String countKey = REACTION_COUNT_PREFIX + "post:" + postId;
        return getCounts(countKey);
    }

    private Map<ReactionType, Long> getCounts(String countKey) {
        Map<ReactionType, Long> counts = new HashMap<>();
        for (ReactionType type : ReactionType.values()) {
            Object value = cache.get(countKey + ":" + type.name(), Object.class);
            long count = value instanceof Number ? ((Number) value).longValue() : 0;
            counts.put(ReactionType.valueOf(type.name()), count);
        }
        return counts;
    }
}
