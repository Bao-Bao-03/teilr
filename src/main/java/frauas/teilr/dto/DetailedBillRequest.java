package frauas.teilr.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DetailedBillRequest {
    private Long groupId;
    private String description;
    private BigDecimal totalAmount;
    private List<SplitLine> splits;

    @Data
    public static class SplitLine {
        private Long userId;
        private BigDecimal amountOwed;
    }
}
