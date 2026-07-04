package frauas.teilr.repository;

import frauas.teilr.entity.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {
    
    Optional<ExpenseSplit> findByUserIdAndGroupId(Long userId, Long groupId);

    List<ExpenseSplit> findByGroupId(Long groupId);

    @Modifying
    @Transactional
    void deleteByGroupId(Long groupId);
}
