package frauas.teilr.service;

import frauas.teilr.entity.Friendship;
import frauas.teilr.entity.User;
import frauas.teilr.repository.FriendshipRepository;
import frauas.teilr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public Friendship sendRequest(Long requesterId, Long targetId) {
        if (requesterId.equals(targetId)) {
            throw new IllegalArgumentException("Cannot friend yourself.");
        }
        if (!userRepository.existsById(targetId)) {
            throw new IllegalArgumentException("Target user does not exist.");
        }
        if (friendshipRepository.findByUserIdAAndUserIdB(requesterId, targetId).isPresent() ||
            friendshipRepository.findByUserIdAAndUserIdB(targetId, requesterId).isPresent()) {
            throw new IllegalStateException("Friend request already exists.");
        }
        Friendship f = new Friendship();
        f.setUserIdA(requesterId);
        f.setUserIdB(targetId);
        return friendshipRepository.save(f);
    }

    public Friendship acceptRequest(Long friendshipId, Long userId) {
        Friendship f = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found: " + friendshipId));
        if (!f.getUserIdB().equals(userId)) {
            throw new SecurityException("Only the recipient can accept this request.");
        }
        f.setStatus("ACCEPTED");
        return friendshipRepository.save(f);
    }

    public List<User> getFriends(Long userId) {
        List<Long> friendIds = friendshipRepository.findAcceptedByUserId(userId).stream()
                .map(f -> f.getUserIdA().equals(userId) ? f.getUserIdB() : f.getUserIdA())
                .toList();
        return userRepository.findAllById(friendIds);
    }
    public List<Friendship> getPendingRequests(Long userId) {
        return friendshipRepository.findByUserIdBAndStatus(userId, "PENDING");
    }

    public boolean areFriends(Long a, Long b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.equals(b)) {
            return true;
        }
        return friendshipRepository.findByUserIdAAndUserIdB(a, b)
                        .filter(f -> "ACCEPTED".equals(f.getStatus())).isPresent()
                || friendshipRepository.findByUserIdAAndUserIdB(b, a)
                        .filter(f -> "ACCEPTED".equals(f.getStatus())).isPresent();
    }

    public String getFriendshipStatus(Long a, Long b) {
        if (a == null || b == null) return "NONE";
        if (a.equals(b)) return "SELF";
        return friendshipRepository.findByUserIdAAndUserIdB(a, b)
                .map(Friendship::getStatus)
                .orElseGet(() -> friendshipRepository.findByUserIdAAndUserIdB(b, a)
                        .map(Friendship::getStatus)
                        .orElse("NONE"));
    }
}
