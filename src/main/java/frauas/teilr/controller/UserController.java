package frauas.teilr.controller;

import frauas.teilr.entity.User;
import frauas.teilr.service.FriendshipService;
import frauas.teilr.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@CrossOrigin(origins = "*")
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final FriendshipService friendshipService;

    @GetMapping("/search")
    public String searchUser(@RequestParam Long userId,
                             @RequestParam(defaultValue = "friend") String context,
                             HttpSession session,
                             Model model) {
        Optional<User> result = userService.findById(userId);
        model.addAttribute("user", result.orElse(null));
        model.addAttribute("notFound", result.isEmpty());
        
        Long currentUserId = (Long) session.getAttribute("userId");
        if (currentUserId != null && result.isPresent()) {
            String status = friendshipService.getFriendshipStatus(currentUserId, result.get().getId());
            model.addAttribute("friendshipStatus", status);
        } else {
            model.addAttribute("friendshipStatus", "NONE");
        }
        
        model.addAttribute("context", context);
        return "fragments/user-search-result :: searchResultContent";
    }
}
