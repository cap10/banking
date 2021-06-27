package zw.co.jugaad.metbankbankingservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SwiftCode extends AbstractAuditingEntity {

    @Id
    private String bankCode;

    private String bank;

    private String swift;

}
