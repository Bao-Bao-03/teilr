package frauas.teilr.config;

import frauas.teilr.entity.User;
import frauas.teilr.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserService userService;

    @ModelAttribute
    public void populateCurrentUser(HttpSession session, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            return;
        }

        Long userId = (Long) session.getAttribute("userId");
        User user;
        if (userId == null) {
            user = userService.findByEmail(auth.getName()).orElse(null);
            if (user != null) {
                session.setAttribute("userId", user.getId());
            }
        } else {
            user = userService.findById(userId).orElse(null);
        }

        if (user != null) {
            model.addAttribute("currentUser", user);
        } else {
            SecurityContextHolder.clearContext();
            session.invalidate();
            throw new org.springframework.security.access.AccessDeniedException("User no longer exists");
        }
    }
}
