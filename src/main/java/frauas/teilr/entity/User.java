package frauas.teilr.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    private Long id;

    private String username;

    @Column(unique = true)
    private String email;
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "verification_token")
    private String verificationToken;
    
    @Column(nullable = false)
    private boolean enabled = false;
}
