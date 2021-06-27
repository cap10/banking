package zw.co.jugaad.metbankbankingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zw.co.jugaad.metbankbankingservice.model.SwiftCode;

public interface SwiftCodeRepository extends JpaRepository<SwiftCode, String> {
}
