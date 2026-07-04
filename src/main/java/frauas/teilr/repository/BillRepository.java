package frauas.teilr.repository;

import frauas.teilr.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    
    List<Bill> findByGroupId(Long groupId);

    @Modifying
    @Transactional
    void deleteByGroupId(Long groupId);
}
