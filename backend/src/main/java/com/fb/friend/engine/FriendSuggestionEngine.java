package com.fb.friend.engine;

import com.fb.auth.model.User;
import com.fb.friend.model.Friend;
import com.fb.friend.repository.FriendRepository;
import com.fb.auth.repository.UserRepository;
import com.fb.common.constant.AppConstant;
import com.fb.common.enums.FriendStatus;
import com.fb.infrastructure.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FriendSuggestionEngine {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;

    public List<FriendSuggestion> suggestFriends(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.fb.common.exception.ResourceNotFoundException("Không tìm thấy người dùng"));

        Set<Long> existingFriends = getExistingFriendIds(userId);

        Map<Long, Double> candidateScores = new HashMap<>();

        scoreByMutualFriends(userId, existingFriends, candidateScores);
        scoreByWorkplace(user, existingFriends, candidateScores);
        scoreByEducation(user, existingFriends, candidateScores);
        scoreByLocation(user, existingFriends, candidateScores);
        scoreByProfileViewers(userId, existingFriends, candidateScores);

        return candidateScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    User suggestedUser = userRepository.findById(entry.getKey()).orElse(null);
                    if (suggestedUser == null) return null;
                    
                    return FriendSuggestion.builder()
                            .user(suggestedUser)
                            .score(entry.getValue())
                            .reasons(getSuggestionReasons(user, suggestedUser, existingFriends))
                            .mutualFriendsCount(getMutualFriendsCount(userId, entry.getKey()))
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void scoreByMutualFriends(Long userId, Set<Long> existingFriends, 
                                       Map<Long, Double> scores) {
        for (Long friendId : existingFriends) {
            Set<Long> friendOfFriendIds = getExistingFriendIds(friendId);
            
            for (Long candidateId : friendOfFriendIds) {
                if (candidateId.equals(userId) || existingFriends.contains(candidateId)) {
                    continue;
                }
                
                int mutualCount = getMutualFriendsCount(userId, candidateId);
                double score = Math.min(mutualCount / 10.0, 1.0) * AppConstant.MUTUAL_FRIEND_WEIGHT;
                
                scores.merge(candidateId, score, Double::sum);
            }
        }
    }

    private void scoreByWorkplace(User user, Set<Long> existingFriends, 
                                   Map<Long, Double> scores) {
        if (user.getWorkplace() == null || user.getWorkplace().isEmpty()) return;
        
        List<User> allUsers = userRepository.findAll();
        List<User> sameWorkplace = allUsers.stream()
                .filter(u -> user.getWorkplace().equals(u.getWorkplace()))
                .collect(Collectors.toList());
        
        for (User candidate : sameWorkplace) {
            if (candidate.getId().equals(user.getId()) || existingFriends.contains(candidate.getId())) {
                continue;
            }
            
            scores.merge(candidate.getId(), AppConstant.SAME_WORKPLACE_WEIGHT, Double::sum);
        }
    }

    private void scoreByEducation(User user, Set<Long> existingFriends, 
                                   Map<Long, Double> scores) {
        if (user.getEducation() == null || user.getEducation().isEmpty()) return;
        
        List<User> allUsers = userRepository.findAll();
        List<User> sameEducation = allUsers.stream()
                .filter(u -> user.getEducation().equals(u.getEducation()))
                .collect(Collectors.toList());
        
        for (User candidate : sameEducation) {
            if (candidate.getId().equals(user.getId()) || existingFriends.contains(candidate.getId())) {
                continue;
            }
            
            scores.merge(candidate.getId(), AppConstant.SAME_EDUCATION_WEIGHT, Double::sum);
        }
    }

    private void scoreByLocation(User user, Set<Long> existingFriends, 
                                  Map<Long, Double> scores) {
        if (user.getLocation() == null || user.getLocation().isEmpty()) return;
        
        List<User> allUsers = userRepository.findAll();
        List<User> sameLocation = allUsers.stream()
                .filter(u -> user.getLocation().equals(u.getLocation()))
                .collect(Collectors.toList());
        
        for (User candidate : sameLocation) {
            if (candidate.getId().equals(user.getId()) || existingFriends.contains(candidate.getId())) {
                continue;
            }
            
            scores.merge(candidate.getId(), AppConstant.SAME_LOCATION_WEIGHT, Double::sum);
        }
    }

    private void scoreByProfileViewers(Long userId, Set<Long> existingFriends, 
                                         Map<Long, Double> scores) {
        String key = "profile:viewers:" + userId;
        Set<Object> viewers = cacheService.sMembers(key);
        
        if (viewers == null) return;
        
        for (Object viewerId : viewers) {
            Long id = Long.parseLong(viewerId.toString());
            if (existingFriends.contains(id)) continue;
            
            scores.merge(id, 0.3, Double::sum);
        }
    }

    private Set<Long> getExistingFriendIds(Long userId) {
        List<Long> friendIds = friendRepository.findFriendIds(userId);
        return new HashSet<>(friendIds);
    }

    private int getMutualFriendsCount(Long userId1, Long userId2) {
        Set<Long> friends1 = getExistingFriendIds(userId1);
        Set<Long> friends2 = getExistingFriendIds(userId2);
        
        Set<Long> mutual = new HashSet<>(friends1);
        mutual.retainAll(friends2);
        
        return mutual.size();
    }

    private List<String> getSuggestionReasons(User user, User suggested, Set<Long> existingFriends) {
        List<String> reasons = new ArrayList<>();
        
        int mutualCount = getMutualFriendsCount(user.getId(), suggested.getId());
        if (mutualCount > 0) {
            reasons.add(mutualCount + " bạn chung");
        }
        
        if (user.getWorkplace() != null && user.getWorkplace().equals(suggested.getWorkplace())) {
            reasons.add("Làm việc tại " + user.getWorkplace());
        }
        
        if (user.getEducation() != null && user.getEducation().equals(suggested.getEducation())) {
            reasons.add("Học tại " + user.getEducation());
        }
        
        if (user.getLocation() != null && user.getLocation().equals(suggested.getLocation())) {
            reasons.add("Sống tại " + user.getLocation());
        }
        
        return reasons;
    }

    @lombok.Data
    @lombok.Builder
    public static class FriendSuggestion {
        private User user;
        private double score;
        private List<String> reasons;
        private int mutualFriendsCount;
    }
}
