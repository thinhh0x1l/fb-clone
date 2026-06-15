package com.fb.comment.service.impl;

import com.fb.comment.model.Comment;
import com.fb.comment.repository.CommentRepository;
import com.fb.comment.dto.CommentResponse;
import com.fb.comment.dto.CreateCommentRequest;
import com.fb.comment.dto.UpdateCommentRequest;
import com.fb.comment.mapper.CommentMapper;
import com.fb.comment.service.CommentService;
import com.fb.post.model.Post;
import com.fb.post.repository.PostRepository;
import com.fb.auth.model.User;
import com.fb.auth.repository.UserRepository;
import com.fb.common.exception.BadRequestException;
import com.fb.common.exception.ResourceNotFoundException;
import com.fb.common.exception.UnauthorizedException;
import com.fb.common.response.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentResponse createComment(Long postId, Long userId, CreateCommentRequest request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BadRequestException("Nội dung bình luận không được để trống");
        }

        if (request.getContent().length() > 2000) {
            throw new BadRequestException("Nội dung bình luận vượt quá 2000 ký tự");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setPost(post);
        comment.setUser(user);

        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận cha"));

            if (parent.getDepth() >= 3) {
                throw new BadRequestException("Đã đạt giới hạn độ sâu bình luận");
            }

            comment.setParent(parent);
            comment.setDepth(parent.getDepth() + 1);
            parent.setRepliesCount(parent.getRepliesCount() + 1);
            commentRepository.save(parent);
        }

        comment = commentRepository.save(comment);

        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);

        log.info("Tạo bình luận thành công: {} trên bài viết {}", comment.getId(), postId);
        return commentMapper.toResponse(comment);
    }

    @Override
    public PagedResponse<CommentResponse> getPostComments(Long postId, Long currentUserId, int page, int size) {
        List<Comment> allComments = commentRepository.findByPostIdWithUser(postId);

        Map<Long, List<Comment>> groupedByParent = allComments.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        List<CommentResponse> responses = allComments.stream()
                .filter(c -> c.getParent() == null)
                .skip((long) page * size)
                .limit(size)
                .map(c -> commentMapper.toResponseWithReplies(c, groupedByParent))
                .collect(Collectors.toList());

        long totalRootComments = allComments.stream().filter(c -> c.getParent() == null).count();
        return PagedResponse.of(responses, page, size, totalRootComments);
    }

    @Override
    public List<CommentResponse> getCommentReplies(Long commentId, Long currentUserId) {
        List<Comment> replies = commentRepository.findRepliesByParentId(commentId);
        return replies.stream()
                .map(c -> commentMapper.toResponse(c))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, Long userId, UpdateCommentRequest request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BadRequestException("Nội dung bình luận không được để trống");
        }

        if (request.getContent().length() > 2000) {
            throw new BadRequestException("Nội dung bình luận vượt quá 2000 ký tự");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Bạn không có quyền chỉnh sửa bình luận này");
        }

        comment.setContent(request.getContent());
        comment = commentRepository.save(comment);

        log.info("Cập nhật bình luận thành công: {}", commentId);
        return commentMapper.toResponse(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận"));

        if (!comment.getUser().getId().equals(userId) &&
            !comment.getPost().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Bạn không có quyền xóa bình luận này");
        }

        comment.softDelete();
        commentRepository.save(comment);

        Post post = comment.getPost();
        post.setCommentsCount(Math.max(0, post.getCommentsCount() - 1));
        postRepository.save(post);

        if (comment.getParent() != null) {
            Comment parent = comment.getParent();
            parent.setRepliesCount(Math.max(0, parent.getRepliesCount() - 1));
            commentRepository.save(parent);
        }

        log.info("Xóa bình luận thành công: {}", commentId);
    }
}
