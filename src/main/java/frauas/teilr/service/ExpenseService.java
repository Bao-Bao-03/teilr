package frauas.teilr.service;

import frauas.teilr.dto.BillCreateRequest;
import frauas.teilr.dto.DetailedBillRequest;
import frauas.teilr.dto.SimplifiedDebtDTO;
import frauas.teilr.entity.Bill;
import frauas.teilr.entity.ExpenseSplit;
import frauas.teilr.entity.GroupMember;
import frauas.teilr.entity.Settlement;
import frauas.teilr.repository.BillRepository;
import frauas.teilr.repository.ExpenseSplitRepository;
import frauas.teilr.repository.GroupMemberRepository;
import frauas.teilr.repository.SettlementRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final BillRepository billRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SettlementRepository settlementRepository;

    public Bill createEqualBill(BillCreateRequest r) {
        return createEqualBill(r.getGroupId(), r.getCreatorId(), r.getDescription(),
                r.getTotalAmount(), r.getParticipantIds(), r.getParticipantNames());
    }

    @Transactional
    public Bill createEqualBill(Long groupId, Long creatorId, String description,
                                BigDecimal totalAmount, List<Long> participantIds, String participantNames) {

        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Bill amount must be strictly positive");
        }

        if (participantIds == null || participantIds.isEmpty()) {
            throw new IllegalArgumentException("Bill must have at least one participant");
        }

        List<GroupMember> groupMembers = groupMemberRepository.findByGroupId(groupId);
        Set<Long> validMemberIds = groupMembers.stream()
                .map(GroupMember::getUserId)
                .collect(Collectors.toSet());

        if (!validMemberIds.contains(creatorId)) {
            throw new SecurityException("Creator must be a member of the group");
        }

        for (Long pId : participantIds) {
            if (!validMemberIds.contains(pId)) {
                throw new IllegalArgumentException("Participant " + pId + " is not a member of the group");
            }
        }

        Bill bill = new Bill();
        bill.setGroupId(groupId);
        bill.setCreatorId(creatorId);
        bill.setDescription(description);
        bill.setTotalAmount(totalAmount);
        bill.setParticipantNames(participantNames);
        Bill savedBill = billRepository.save(bill);

        int numPeople = participantIds.size();
        BigDecimal divisor = new BigDecimal(numPeople);

        BigDecimal baseOwed = totalAmount.divide(divisor, 2, RoundingMode.DOWN);

        BigDecimal remainder = totalAmount.subtract(baseOwed.multiply(divisor));


        ExpenseSplit creatorSplit = getOrCreateExpenseSplit(creatorId, groupId);
        creatorSplit.setTotalPaid(creatorSplit.getTotalPaid().add(totalAmount));
        expenseSplitRepository.save(creatorSplit);

        List<ExpenseSplit> toSave = new ArrayList<>();
        for (int i = 0; i < participantIds.size(); i++) {
            Long participantId = participantIds.get(i);
            ExpenseSplit participantSplit = getOrCreateExpenseSplit(participantId, groupId);

            BigDecimal amountToOwe = baseOwed;
            if (i == 0) {
                amountToOwe = amountToOwe.add(remainder);
            }

            participantSplit.setTotalOwed(participantSplit.getTotalOwed().add(amountToOwe));
            toSave.add(participantSplit);
        }

        expenseSplitRepository.saveAll(toSave);
        return savedBill;
    }

    @Transactional
    public Bill createDetailedBill(Long groupId, Long creatorId, String description,
                                   BigDecimal totalAmount, Map<Long, BigDecimal> shares,
                                   String participantNames) {

        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Bill amount must be strictly positive");
        }
        if (shares == null || shares.isEmpty()) {
            throw new IllegalArgumentException("Bill must have at least one participant");
        }

        Set<Long> validMemberIds = groupMemberRepository.findByGroupId(groupId).stream()
                .map(GroupMember::getUserId)
                .collect(Collectors.toSet());
        if (!validMemberIds.contains(creatorId)) {
            throw new SecurityException("Creator must be a member of the group");
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal> e : shares.entrySet()) {
            if (!validMemberIds.contains(e.getKey())) {
                throw new IllegalArgumentException("Participant " + e.getKey() + " is not a member of the group");
            }
            BigDecimal share = e.getValue();
            if (share == null || share.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Each share must be zero or positive");
            }
            sum = sum.add(share);
        }
        if (sum.compareTo(totalAmount) != 0) {
            throw new IllegalArgumentException("Shares must add up to the total (" + totalAmount + ")");
        }

        Bill bill = new Bill();
        bill.setGroupId(groupId);
        bill.setCreatorId(creatorId);
        bill.setDescription(description);
        bill.setTotalAmount(totalAmount);
        bill.setParticipantNames(participantNames);
        Bill savedBill = billRepository.save(bill);

        ExpenseSplit creatorSplit = getOrCreateExpenseSplit(creatorId, groupId);
        creatorSplit.setTotalPaid(creatorSplit.getTotalPaid().add(totalAmount));
        expenseSplitRepository.save(creatorSplit);

        List<ExpenseSplit> toSave = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> e : shares.entrySet()) {
            ExpenseSplit s = getOrCreateExpenseSplit(e.getKey(), groupId);
            s.setTotalOwed(s.getTotalOwed().add(e.getValue()));
            toSave.add(s);
        }
        expenseSplitRepository.saveAll(toSave);
        
        return savedBill;
    }

    @Transactional
    public Settlement confirmSettlement(Long groupId, Long debtorId, Long creditorId,
                                        BigDecimal amount, Long byUserId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Settlement amount must be strictly positive");
        }
        if (debtorId.equals(creditorId)) {
            throw new IllegalArgumentException("Debtor and creditor must differ");
        }
        if (!byUserId.equals(debtorId) && !byUserId.equals(creditorId)) {
            throw new SecurityException("Only the debtor or creditor can confirm this settlement");
        }

        List<SimplifiedDebtDTO> currentDebts = getSimplifiedDebts(groupId);
        boolean isValidDebt = false;
        for (SimplifiedDebtDTO d : currentDebts) {
            if (d.getDebtorId().equals(debtorId) && d.getCreditorId().equals(creditorId)) {
                if (d.getAmount().compareTo(amount) >= 0) {
                    isValidDebt = true;
                    break;
                }
            }
        }

        if (!isValidDebt) {
            throw new IllegalStateException("This debt has already been settled or the amount is invalid due to recent changes.");
        }

        applyBalance(debtorId, creditorId, groupId, amount);

        Settlement settlement = new Settlement();
        settlement.setGroupId(groupId);
        settlement.setDebtorId(debtorId);
        settlement.setCreditorId(creditorId);
        settlement.setAmount(amount);
        settlement.setConfirmedById(byUserId);
        settlement.setStatus("CONFIRMED");
        settlement = settlementRepository.save(settlement);
        return settlement;
    }

    @Transactional
    public Settlement revertSettlement(Long settlementId, Long byUserId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found: " + settlementId));

        if (!"CONFIRMED".equals(settlement.getStatus())) {
            throw new IllegalStateException("Settlement is not currently confirmed");
        }
        if (!byUserId.equals(settlement.getCreditorId()) && !byUserId.equals(settlement.getDebtorId())) {
            throw new SecurityException("Only the payer or receiver can revert this settlement");
        }

        applyBalance(settlement.getCreditorId(), settlement.getDebtorId(),
                settlement.getGroupId(), settlement.getAmount());

        settlement.setStatus("REVERTED");
        settlement = settlementRepository.save(settlement);
        return settlement;
    }

    public List<Settlement> getActivity(Long groupId) {
        return settlementRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
    }

    private void applyBalance(Long debtorId, Long creditorId, Long groupId, BigDecimal amount) {
        ExpenseSplit debtorSplit = getOrCreateExpenseSplit(debtorId, groupId);
        debtorSplit.setTotalPaid(debtorSplit.getTotalPaid().add(amount));
        ExpenseSplit creditorSplit = getOrCreateExpenseSplit(creditorId, groupId);
        creditorSplit.setTotalOwed(creditorSplit.getTotalOwed().add(amount));
        expenseSplitRepository.save(debtorSplit);
        expenseSplitRepository.save(creditorSplit);
    }

    private static class BalanceCalc {
        Long userId;
        BigDecimal balance;

        BalanceCalc(Long userId, BigDecimal balance) {
            this.userId = userId;
            this.balance = balance;
        }
    }

    public List<SimplifiedDebtDTO> getSimplifiedDebts(Long groupId) {
        List<ExpenseSplit> allSplits = expenseSplitRepository.findByGroupId(groupId);

        Map<Boolean, List<ExpenseSplit>> split = allSplits.stream()
            .collect(Collectors.partitioningBy(s -> s.getBalance().compareTo(BigDecimal.ZERO) > 0));

        List<BalanceCalc> creditors = split.get(true).stream()
            .map(s -> new BalanceCalc(s.getUserId(), s.getBalance()))
            .toList();

        List<BalanceCalc> debtors = split.get(false).stream()
            .filter(s -> s.getBalance().compareTo(BigDecimal.ZERO) < 0)
            .map(s -> new BalanceCalc(s.getUserId(), s.getBalance().abs()))
            .toList();

        List<SimplifiedDebtDTO> simplifiedDebts = new ArrayList<>();
        int i = 0;
        int j = 0; 
        while (i < debtors.size() && j < creditors.size()) {
            BalanceCalc debtor = debtors.get(i);
            BalanceCalc creditor = creditors.get(j);

            BigDecimal minAmount = debtor.balance.min(creditor.balance);

            simplifiedDebts.add(new SimplifiedDebtDTO(debtor.userId, creditor.userId, minAmount));

            debtor.balance = debtor.balance.subtract(minAmount);
            creditor.balance = creditor.balance.subtract(minAmount);

            if (debtor.balance.compareTo(BigDecimal.ZERO) == 0) i++;
            if (creditor.balance.compareTo(BigDecimal.ZERO) == 0) j++;
        }

        return simplifiedDebts;
    }

    public List<Bill> getBillsForGroup(Long groupId) {
        return billRepository.findByGroupId(groupId);
    }

    private ExpenseSplit getOrCreateExpenseSplit(Long userId, Long groupId) {
        return expenseSplitRepository.findByUserIdAndGroupId(userId, groupId)
                .orElseGet(() -> {
                    ExpenseSplit newSplit = new ExpenseSplit();
                    newSplit.setUserId(userId);
                    newSplit.setGroupId(groupId);
                    return newSplit;
                });
    }
}
