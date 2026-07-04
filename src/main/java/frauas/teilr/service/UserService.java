package frauas.teilr.service;

import frauas.teilr.dto.RegisterRequest;
import frauas.teilr.entity.User;
import frauas.teilr.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User register(RegisterRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String email = request.getEmail() == null ? "" : request.getEmail().trim();
        String rawPassword = request.getPassword();

        if (username.isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (email.isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        if (userRepository.count() >= 10000) {
            throw new IllegalStateException("Maximum user capacity reached (10,000 users).");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email '" + email + "' is already taken.");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setEnabled(false);
        user.setId(generateId());

        return userRepository.save(user);
    }

    public Optional<User> confirmEmail(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByVerificationToken(token).map(user -> {
            user.setEnabled(true);
            return userRepository.save(user);
        });
    }
    
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = resolve(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("No account found for '" + identifier + "'."));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .disabled(!user.isEnabled())
                .authorities("ROLE_USER")
                .build();
    }

    private Optional<User> resolve(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }
        String trimmed = identifier.trim();
        try {
            return userRepository.findById(Long.parseLong(trimmed));
        } catch (NumberFormatException ignored) {
        }
        Optional<User> byEmail = userRepository.findByEmail(trimmed);
        return byEmail.isPresent() ? byEmail : userRepository.findByUsername(trimmed);
    }
    private Long generateId() {
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 3; i++) {
            Long guess = (long) random.nextInt(10000);
            if (!userRepository.existsById(guess)) {
                return guess;
            }
        }

        if (!userRepository.existsById(0L)) {
            return 0L;
        }
        Long available = userRepository.findFirstAvailableId();
        if (available == null) {
            throw new IllegalStateException("Maximum user capacity reached (10,000 users).");
        }
        return available;
    }
}
