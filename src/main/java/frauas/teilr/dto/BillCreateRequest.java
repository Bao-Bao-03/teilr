package frauas.teilr.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BillCreateRequest {
    private Long groupId;
    private Long creatorId;
    private String description;
    private BigDecimal totalAmount;
    private List<Long> participantIds;
    private String participantNames;
}
