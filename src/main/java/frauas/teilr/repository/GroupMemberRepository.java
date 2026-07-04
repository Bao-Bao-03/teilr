package frauas.teilr.repository;

import frauas.teilr.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByGroupId(Long groupId);

    List<GroupMember> findByUserId(Long userId);

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    @Modifying
    @Transactional
    void deleteByGroupIdAndUserId(Long groupId, Long userId);

    @Modifying
    @Transactional
    void deleteByGroupId(Long groupId);
}
