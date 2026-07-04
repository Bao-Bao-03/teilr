package frauas.teilr.controller;

import frauas.teilr.entity.Friendship;
import frauas.teilr.entity.User;
import frauas.teilr.service.FriendshipService;
import frauas.teilr.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@CrossOrigin(origins = "*")
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final UserService userService;

    @GetMapping
    public String getFriends(HttpSession session, Model model,
                             @RequestParam(defaultValue = "false") boolean selectable) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";

        List<User> friends = friendshipService.getFriends(userId);
        model.addAttribute("friends", friends);
        model.addAttribute("selectable", selectable);
        return "fragments/friend-list :: friendListContent";
    }

    @GetMapping("/pending")
    public String getPendingRequests(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";

        List<Friendship> requests = friendshipService.getPendingRequests(userId);
        Map<Long, String> names = requests.stream()
                .map(Friendship::getUserIdA)
                .distinct()
                .map(userService::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toMap(User::getId, User::getUsername));
        model.addAttribute("requests", requests);
        model.addAttribute("names", names);
        return "fragments/friend-requests :: requestListContent";
    }

    @PostMapping("/request")
    public ResponseEntity<Friendship> sendRequest(HttpSession session,
                                                  @RequestParam Long targetId) {
        Long requesterId = (Long) session.getAttribute("userId");
        if (requesterId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Friendship friendship = friendshipService.sendRequest(requesterId, targetId);

        return ResponseEntity.ok(friendship);
    }
    
    @PostMapping("/accept")
    public ResponseEntity<Friendship> acceptRequest(@RequestParam Long friendshipId,
                                                    HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Friendship friendship = friendshipService.acceptRequest(friendshipId, userId);
        
        return ResponseEntity.ok(friendship);
    }
}
