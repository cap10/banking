package zw.co.jugaad.metbankbankingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableEurekaClient
public class MetbankBankingServiceApplication {


    public static void main(String[] args) {
        SpringApplication.run(MetbankBankingServiceApplication.class, args);

    }



    /*AFCNZWHA    "AFRICAN CENTRURY"
    AGRZZWHA    "AGRIBANK"
    BARCZWHX    "BARCLAYS"
    CABSZWHA    "CABS"
    COBZZWHA    "CBZ"
    ECOCZWHX    "ECOBANK"
    EMPWZWHX    "EMPOWERBANK LIMITED"
    FBCPZWHA    "FBC"
    FMBZZWHX    "AFRICAN BANKING CORP"
    GBSPZWHA    "GETBUCKS"
    LMLDZWHA    "LION FINANCE"
    MBCAZWHX    "MBCA"
    NABYZWHA    "NBS"
    NMBLZWHX    "NMB"
    PWSBZWHX    "POSB"
    REBZZWHX    "RBZ"
    SBICZWHX    "STANBIC"
    SCBLZWHX    "STANCHART"
    SMFBZWHA    "SUCCESS MICROFINANCE"
    STBLZWHX    "STEWARD"
    TTSLZWHX    "TETRAD"
    ZBCOZWHX    "ZB"
    ZDBLZWHA    "IDBZ"
    ZWMFZWHA    "Zimbabwe Women's Microfinance Bank"*/


}
