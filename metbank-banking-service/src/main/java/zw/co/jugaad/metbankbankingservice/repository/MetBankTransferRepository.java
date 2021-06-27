package zw.co.jugaad.metbankbankingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import zw.co.jugaad.metbankbankingservice.model.MetBankTransfer;

import java.util.List;
import java.util.Optional;

public interface MetBankTransferRepository extends JpaRepository<MetBankTransfer, Long> {

    @Query(value = "update MetBankTransfer set responseCode =?2 and status ='COMPLETE' where rrn =?1", nativeQuery = true)
    void updateTransaction(String rrn, String responseCode);

    @Query(value = "select * from  MetBankTransfer where responseCode is null  and status ='TIMEOUT' ", nativeQuery = true)
    List<MetBankTransfer> findAllByMtiAndStatusAndResponseCode();

    Optional<MetBankTransfer> findByRrn(String rrn);
}
