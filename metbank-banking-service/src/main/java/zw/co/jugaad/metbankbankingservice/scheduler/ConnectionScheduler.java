package zw.co.jugaad.metbankbankingservice.scheduler;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import zw.co.jugaad.metbankbankingservice.util.ISOConnection;

@Service
public class ConnectionScheduler {

    private final ISOConnection isoConnection;

    public ConnectionScheduler(ISOConnection isoConnection) {
        this.isoConnection = isoConnection;
    }

    @Scheduled(cron = "*/60 * * * * ?")
    public void demoServiceMethod() throws Exception {
        isoConnection.getConnection();

    }
}
