package frauas.teilr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SimplifiedDebtDTO {
    private Long debtorId;
    private Long creditorId;
    private BigDecimal amount; 
}
