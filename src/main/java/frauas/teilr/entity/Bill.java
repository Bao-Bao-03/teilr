package frauas.teilr.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "bills")
@Data
@NoArgsConstructor

public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(nullable = false)
    private String description;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    private String currency = "EUR";
    
    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
    
    @Column(name = "participant_names")
    private String participantNames; 

    @Column(name = "status")
    private String status = "COMPLETED";
}
