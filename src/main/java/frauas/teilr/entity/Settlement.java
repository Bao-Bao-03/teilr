package frauas.teilr.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "settlements")
@Data
@NoArgsConstructor
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "debtor_id", nullable = false)
    private Long debtorId;
    @Column(name = "creditor_id", nullable = false)
    private Long creditorId;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false)
    private String status = "CONFIRMED";
    
    @Column(name = "confirmed_by_id", nullable = false)
    private Long confirmedById;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
