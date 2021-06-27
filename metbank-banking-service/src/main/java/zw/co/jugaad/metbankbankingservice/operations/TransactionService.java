package zw.co.jugaad.metbankbankingservice.operations;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import zw.co.jugaad.metbankbankingservice.model.MetBankTransfer;

import java.io.IOException;
import java.util.List;

public interface TransactionService {


    String walletToBankTransfer(String mobile, String creditAccount, String amount) throws Exception;

    String bankToWalletTransfer(String mobile, String debitAccount, String amount) throws Exception;

    String reverseTransfer(MetBankTransfer transfer) throws IOException, ISOException, Exception;

    List<MetBankTransfer> getAllTimedOutTransactions();

    String bank2Wallet(String mobile, String debitAccount, String amount) throws ISOException, IOException;

    String wallet2Bank(String mobile, String creditAccount, String amount) throws ISOException, IOException;


    ISOMsg b2W(MetBankTransfer transaction) throws IOException, ISOException;

    ISOMsg w2B(MetBankTransfer transaction) throws IOException, ISOException;

    ISOMsg settleMerchantBiller(MetBankTransfer transaction) throws IOException, ISOException;

    ISOMsg reverse(MetBankTransfer transaction) throws IOException, ISOException;

    void saveTransactions(MetBankTransfer transaction);

    void updateTransactions(MetBankTransfer transaction, ISOMsg response);

}
