package frauas.teilr.repository;

import frauas.teilr.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String verificationToken);

    @org.springframework.data.jpa.repository.Query(
        value = "SELECT MIN(u1.id + 1) FROM users u1 LEFT JOIN users u2 ON u1.id + 1 = u2.id WHERE u2.id IS NULL AND u1.id < 9999",
        nativeQuery = true
    )
    Long findFirstAvailableId();
}
