package frauas.teilr.service;

import frauas.teilr.dto.SimplifiedDebtDTO;
import frauas.teilr.entity.Bill;
import frauas.teilr.entity.Group;
import frauas.teilr.entity.Settlement;
import frauas.teilr.entity.User;
import frauas.teilr.repository.GroupMemberRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupViewService {

    private final GroupService groupService;
    private final ExpenseService expenseService;
    private final GroupMemberRepository groupMemberRepository;
    private final FriendshipService friendshipService;

    public Map<String, Object> build(Long groupId, Long requesterId) {
        Group group = groupService.findGroupById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, requesterId)) {
            throw new SecurityException("You are not a member of this group.");
        }

        List<User> members = groupService.getMembersOfGroup(groupId);
        Map<Long, String> names = members.stream()
                .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a, LinkedHashMap::new));

        List<Bill> bills = expenseService.getBillsForGroup(groupId).stream()
                .sorted(Comparator.comparing(Bill::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        List<SimplifiedDebtDTO> debts = expenseService.getSimplifiedDebts(groupId);
        List<Settlement> activity = expenseService.getActivity(groupId);

        boolean isAdmin = group.getAdminId() != null && group.getAdminId().equals(requesterId);

        List<User> addableFriends = List.of();
        if (isAdmin) {
            java.util.Set<Long> memberIds = members.stream().map(User::getId).collect(Collectors.toSet());
            addableFriends = friendshipService.getFriends(requesterId).stream()
                    .filter(f -> !memberIds.contains(f.getId()))
                    .toList();
        }

        Map<String, Object> model = new LinkedHashMap<>();
        model.put("group", group);
        model.put("members", members);
        model.put("names", names);
        model.put("bills", bills);
        model.put("debts", debts);
        model.put("activity", activity);
        model.put("isAdmin", isAdmin);
        model.put("addableFriends", addableFriends);
        return model;
    }
}
