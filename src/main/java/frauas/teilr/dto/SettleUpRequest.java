package frauas.teilr.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SettleUpRequest {
    private Long groupId;
    private Long debtorId; 
    private Long creditorId;
    private BigDecimal amount;
}
