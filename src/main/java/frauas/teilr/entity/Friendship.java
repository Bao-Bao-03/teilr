package frauas.teilr.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "friendships", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id_a", "user_id_b"})
})
@Data
@NoArgsConstructor
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id_a", nullable = false)
    private Long userIdA;

    @Column(name = "user_id_b", nullable = false)
    private Long userIdB;

    @Column(nullable = false)
    private String status = "PENDING";
}
