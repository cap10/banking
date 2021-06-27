package zw.co.jugaad.metbankbankingservice.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import zw.co.jugaad.metbankbankingservice.model.MetBankTransfer;
import zw.co.jugaad.metbankbankingservice.operations.TransactionService;


@Service
public class ReversalsScheduler {

    private final TransactionService transactionService;

    public ReversalsScheduler(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Scheduled(cron="*/20 * * * * ?")
        public void demoServiceMethod() throws Exception {
            for (MetBankTransfer transfer:transactionService.getAllTimedOutTransactions()
                 ) {
                transactionService.reverseTransfer(transfer);

            }

        }
}
