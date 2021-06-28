package zw.co.jugaad.metbankbankingservice.operations;


import lombok.extern.slf4j.Slf4j;
import org.jpos.iso.*;
import org.springframework.stereotype.Service;
import zw.co.jugaad.metbankbankingservice.model.MetBankTransfer;
import zw.co.jugaad.metbankbankingservice.model.ResponseCode;
import zw.co.jugaad.metbankbankingservice.repository.MetBankTransferRepository;
import zw.co.jugaad.metbankbankingservice.util.FlexPackager;
import zw.co.jugaad.metbankbankingservice.util.ISOConnection;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    private final MetBankTransferRepository metBankTransferRepository;
    private final ISOConnection isoConnection = ISOConnection.getInstance();
    ISOChannel channel = isoConnection.getConnection();

    public TransactionServiceImpl(MetBankTransferRepository metBankTransferRepository) throws IOException, ISOException {
        this.metBankTransferRepository = metBankTransferRepository;
    }

    @Override
    public String walletToBankTransfer(String mobile, String creditAccount, String amount) throws Exception {
        try {
            CompletableFuture.supplyAsync(() -> {
                try {
                    return wallet2Bank(mobile, creditAccount, amount);

                } catch (ISOException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            })
                    .get(20, TimeUnit.SECONDS);

        } catch (TimeoutException timeoutException) {
            throw new Exception("Issuer not available, please try again");
        }
        return wallet2Bank(mobile, creditAccount, amount);
    }


    @Override
    public String reverseTransfer(MetBankTransfer transfer) throws Exception {

        try {
            CompletableFuture.supplyAsync(() -> {
                try {
                    return reverseTransactionThaTimedOut(transfer);
                } catch (ISOException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            })
                    .get(20, TimeUnit.SECONDS);

        } catch (TimeoutException timeoutException) {
            //throw new Exception("Issuer not available, please try again");
        }
        return null;
    }

    @Override
    public List<MetBankTransfer> getAllTimedOutTransactions() {
        return metBankTransferRepository.findAllByMtiAndStatusAndResponseCode();
    }

    @Override
    public String bankToWalletTransfer(String mobile, String debitAccount, String amount) throws Exception {
        String response;
        try {
            CompletableFuture.supplyAsync(() -> {

                try {
                    return bank2Wallet(mobile, debitAccount, amount);
                } catch (ISOException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            })
                    .get(20, TimeUnit.SECONDS);
        } catch (TimeoutException timeoutException) {
            throw new Exception("Issuer not available, please try again");
        }
        return bank2Wallet(mobile, debitAccount, amount);
    }


    @Override
    public String bank2Wallet(String mobile, String debitAccount, String amount) throws ISOException, IOException {
        Random random = new Random();
        MetBankTransfer transaction = new MetBankTransfer();
        transaction.setMti("0200");
        transaction.setProcessingCode("540000");
        transaction.setAmount(amount);
        transaction.setTransactionDate(ISODate.formatDate(new Date(), "MMddyyhhmm"));
        transaction.setStan(ISODate.formatDate(new Date(), "yyhhmm"));
        transaction.setTimeLocalTransaction(ISODate.formatDate(new Date(), "yyhhmm"));
        transaction.setDateLocalTransaction(ISODate.formatDate(new Date(), "MMdd"));
        transaction.setRrn(ISOUtil.padleft(String.valueOf(random.nextInt(999999)), 12, '0'));
        transaction.setCardAcceptorTid("CASHMET");
        transaction.setCardAcceptorIdCode(mobile);
        transaction.setCardAcceptorLocation("Bank to Wallet");
        transaction.setCurrencyCode("932");
        transaction.setAccountDebit(debitAccount);
        transaction.setAccountCredit("1000010428");
        metBankTransferRepository.save(transaction);
        ISOMsg m3 = new ISOMsg();
        m3.setMTI(transaction.getMti());
        m3.set(3, transaction.getProcessingCode()); //processing code
        m3.set(4, transaction.getAmount()); //transaction amount <---
        m3.set(7, transaction.getTransactionDate()); //transmission date & time 0211152402
        m3.set(11, transaction.getStan()); //systems trace audit number
        m3.set(12, transaction.getTimeLocalTransaction()); //Time, Local Transaction
        m3.set(13, transaction.getDateLocalTransaction()); //Date, Local Transaction
        m3.set(37, transaction.getRrn());//Retrieval Reference Number <---- -->Unique for every transaction
        m3.set(41, transaction.getCardAcceptorTid()); //Card Acceptor Terminal ID
        m3.set(42, transaction.getCardAcceptorIdCode()); //Card Acceptor ID Code
        m3.set(43, transaction.getCardAcceptorLocation()); //Card Acceptor Name Location
        m3.set(49, transaction.getCurrencyCode()); //Currency Code, Transaction
        m3.set(102, transaction.getAccountDebit()); //Account Identification 1 --> Debit
        m3.set(103, transaction.getAccountCredit()); //Account Identification 2 --> Credit
        channel.send(m3);
        ISOMsg response1 = channel.receive();
        System.out.println("############## Response:  " + ISOUtil.dumpString(response1.pack()));
        //Update transaction with response
        transaction.setResponseCode(response1.getString(39));
        transaction.setStatus("COMPLETE");
        metBankTransferRepository.save(transaction);
        return response1.getString(39);
    }

    @Override
    public String wallet2Bank(String mobile, String creditAccount, String amount) throws ISOException, IOException {

        Random random = new Random();
        MetBankTransfer transaction = new MetBankTransfer();
        transaction.setMti("0200");
        transaction.setProcessingCode("540000");
        transaction.setAmount(amount);
        transaction.setTransactionDate(ISODate.formatDate(new Date(), "MMddyyhhmm"));
        transaction.setStan(ISODate.formatDate(new Date(), "yyhhmm"));
        transaction.setTimeLocalTransaction(ISODate.formatDate(new Date(), "yyhhmm"));
        transaction.setDateLocalTransaction(ISODate.formatDate(new Date(), "MMdd"));
        transaction.setRrn(ISOUtil.padleft(String.valueOf(random.nextInt(999999)), 12, '0'));
        transaction.setCardAcceptorTid("CASHMET");
        transaction.setCardAcceptorIdCode(mobile);
        transaction.setCardAcceptorLocation("Wallet to Bank");
        transaction.setCurrencyCode("932");
        transaction.setAccountDebit("1000010428");
        transaction.setAccountCredit(creditAccount);
        metBankTransferRepository.save(transaction);


        ISOMsg m3 = new ISOMsg();

        m3.setMTI(transaction.getMti());
        m3.set(3, transaction.getProcessingCode()); //processing code
        m3.set(4, transaction.getAmount()); //transaction amount <---
        m3.set(7, transaction.getTransactionDate()); //transmission date & time 0211152402
        m3.set(11, transaction.getStan()); //systems trace audit number
        m3.set(12, transaction.getTimeLocalTransaction()); //Time, Local Transaction
        m3.set(13, transaction.getDateLocalTransaction()); //Date, Local Transaction
        m3.set(37, transaction.getRrn());//Retrieval Reference Number <---- -->Unique for every transaction
        m3.set(41, transaction.getCardAcceptorTid()); //Card Acceptor Terminal ID
        m3.set(42, transaction.getCardAcceptorIdCode()); //Card Acceptor ID Code
        m3.set(43, transaction.getCardAcceptorLocation()); //Card Acceptor Name Location
        m3.set(49, transaction.getCurrencyCode()); //Currency Code, Transaction
        m3.set(102, transaction.getAccountDebit()); //Account Identification 1 --> Debit
        m3.set(103, transaction.getAccountCredit()); //Account Identification 2 --> Credit

        channel.send(m3);
        ISOMsg response = channel.receive();
        System.out.println("############## Response:  " + ISOUtil.dumpString(response.pack()));
        //Update transaction with response
        transaction.setResponseCode(response.getString(39));
        transaction.setStatus("COMPLETE");
        metBankTransferRepository.updateTransaction(transaction.getRrn(), response.getString(39));
        return response.getString(39);
    }

    @Override
    public ISOMsg b2W(MetBankTransfer transaction) throws IOException, ISOException {
        ISOMsg isoMsg = new ISOMsg();
        isoMsg.setMTI(transaction.getMti());
        isoMsg.set(3, transaction.getProcessingCode()); //processing code
        isoMsg.set(4, transaction.getAmount()); //transaction amount <---
        isoMsg.set(7, transaction.getTransactionDate()); //transmission date & time 0211152402
        isoMsg.set(11, transaction.getStan()); //systems trace audit number
        isoMsg.set(12, transaction.getTimeLocalTransaction()); //Time, Local Transaction
        isoMsg.set(13, transaction.getDateLocalTransaction()); //Date, Local Transaction
        isoMsg.set(37, transaction.getRrn());//Retrieval Reference Number <---- -->Unique for every transaction
        isoMsg.set(41, transaction.getCardAcceptorTid()); //Card Acceptor Terminal ID
        isoMsg.set(42, transaction.getCardAcceptorIdCode()); //Card Acceptor ID Code
        isoMsg.set(43, transaction.getCardAcceptorLocation()); //Card Acceptor Name Location
        isoMsg.set(49, transaction.getCurrencyCode()); //Currency Code, Transaction
        isoMsg.set(102, transaction.getAccountDebit()); //Account Identification 1 --> Debit
        isoMsg.set(103, transaction.getAccountCredit()); //Account Identification 2 --> Credit
        channel.send(isoMsg);
        return channel.receive();
    }

    @Override
    public ISOMsg w2B(MetBankTransfer transaction) throws IOException, ISOException {
        ISOMsg isoMsg = new ISOMsg();
        isoMsg.setMTI(transaction.getMti());
        isoMsg.set(3, transaction.getProcessingCode()); //processing code
        isoMsg.set(4, transaction.getAmount()); //transaction amount <---
        isoMsg.set(7, transaction.getTransactionDate()); //transmission date & time 0211152402
        isoMsg.set(11, transaction.getStan()); //systems trace audit number
        isoMsg.set(12, transaction.getTimeLocalTransaction()); //Time, Local Transaction
        isoMsg.set(13, transaction.getDateLocalTransaction()); //Date, Local Transaction
        isoMsg.set(37, transaction.getRrn());//Retrieval Reference Number <---- -->Unique for every transaction
        isoMsg.set(41, transaction.getCardAcceptorTid()); //Card Acceptor Terminal ID
        isoMsg.set(42, transaction.getCardAcceptorIdCode()); //Card Acceptor ID Code
        isoMsg.set(43, transaction.getCardAcceptorLocation()); //Card Acceptor Name Location
        isoMsg.set(49, transaction.getCurrencyCode()); //Currency Code, Transaction
        isoMsg.set(102, transaction.getAccountDebit()); //Account Identification 1 --> Debit
        isoMsg.set(103, transaction.getAccountCredit()); //Account Identification 2 --> Credit
        channel.send(isoMsg);
        return channel.receive();
    }

    @Override
    public ISOMsg settleMerchantBiller(MetBankTransfer transaction) throws IOException, ISOException {
        ISOMsg isoMsg = new ISOMsg();
        isoMsg.setPackager(new FlexPackager());
        isoMsg.setMTI(transaction.getMti());
        isoMsg.set(3, transaction.getProcessingCode()); //processing code
        isoMsg.set(4, transaction.getAmount()); //transaction amount <---
        isoMsg.set(7, transaction.getTransactionDate()); //transmission date & time 0211152402
        isoMsg.set(11, transaction.getStan()); //systems trace audit number
        isoMsg.set(12, transaction.getTimeLocalTransaction()); //Time, Local Transaction
        isoMsg.set(13, transaction.getDateLocalTransaction()); //Date, Local Transaction
        isoMsg.set(37, transaction.getRrn());//Retrieval Reference Number <---- -->Unique for every transaction
        isoMsg.set(41, transaction.getCardAcceptorTid()); //Card Acceptor Terminal ID
        isoMsg.set(42, transaction.getCardAcceptorIdCode()); //Card Acceptor ID Code
        isoMsg.set(43, transaction.getCardAcceptorLocation()); //Card Acceptor Name Location
        isoMsg.set(49, transaction.getCurrencyCode()); //Currency Code, Transaction
        isoMsg.set(102, transaction.getAccountDebit()); //Account Identification 1 --> Debit
        isoMsg.set(103, transaction.getAccountCredit()); //Account Identification 2 --> Credit
        isoMsg.set("127.019", "Mapfunde");
        isoMsg.set("127.022", "0183467663");
        isoMsg.set("127.023", "AGRZZWHA");
        channel.send(isoMsg);
        return channel.receive();
    }

    @Override
    public ISOMsg reverse(MetBankTransfer transaction) throws IOException, ISOException {
        ISOMsg m3 = new ISOMsg();
        m3.setPackager(new FlexPackager());
        m3.setMTI("0420");
        m3.set(3, "541000"); //processing code
        m3.set(4, transaction.getAmount()); //transaction amount <---
        m3.set(7, transaction.getTransactionDate()); //transmission date & time 0211152402
        m3.set(11, transaction.getStan()); //systems trace audit number
        m3.set(12, transaction.getTimeLocalTransaction()); //Time, Local Transaction
        m3.set(13, transaction.getDateLocalTransaction()); //Date, Local Transaction
        m3.set(37, transaction.getRrn());//Retrieval Reference Number <---- -->Unique for every transaction
        m3.set(41, transaction.getCardAcceptorTid()); //Card Acceptor Terminal ID
        m3.set(42, transaction.getCardAcceptorIdCode()); //Card Acceptor ID Code
        m3.set(43, transaction.getCardAcceptorLocation()); //Card Acceptor Name Location
        m3.set(49, transaction.getCurrencyCode()); //Currency Code, Transaction
        m3.set(102, transaction.getAccountDebit()); //Account Identification 1 --> Debit
        m3.set(103, transaction.getAccountCredit()); //Account Identification 2 --> Credit
        m3.set("127.019", transaction.getReceivingAccountNameRtgs());
        m3.set("127.022", transaction.getReceivingAccountNumberRtgs());
        m3.set("127.023", transaction.getReceivingBankSwiftCodeRtgs());
        channel.send(m3);
        return channel.receive();
    }

    private String reverseTransactionThaTimedOut(MetBankTransfer transfer) throws ISOException, IOException {
        ISOMsg m3 = new ISOMsg();
        m3.setPackager(new FlexPackager());
        m3.setMTI("0420");
        m3.set(3, "541000"); //processing code
        m3.set(4, transfer.getAmount()); //transaction amount <---
        m3.set(7, transfer.getTransactionDate()); //transmission date & time 0211152402
        m3.set(11, transfer.getStan()); //systems trace audit number
        m3.set(12, transfer.getTimeLocalTransaction()); //Time, Local Transaction
        m3.set(13, transfer.getDateLocalTransaction()); //Date, Local Transaction
        m3.set(37, transfer.getRrn());//Retrieval Reference Number <---- -->Unique for every transaction
        m3.set(41, transfer.getCardAcceptorTid()); //Card Acceptor Terminal ID
        m3.set(42, transfer.getCardAcceptorIdCode()); //Card Acceptor ID Code
        m3.set(43, transfer.getCardAcceptorLocation()); //Card Acceptor Name Location
        m3.set(49, transfer.getCurrencyCode()); //Currency Code, Transaction
        m3.set(102, transfer.getAccountDebit()); //Account Identification 1 --> Debit
        m3.set(103, transfer.getAccountCredit()); //Account Identification 2 --> Credit
        m3.set("127.019", transfer.getReceivingAccountNameRtgs());
        m3.set("127.022", transfer.getReceivingAccountNumberRtgs());
        m3.set("127.023", transfer.getReceivingBankSwiftCodeRtgs());
        channel.send(m3);
        ISOMsg r = channel.receive();
        System.out.println("############## Response:  " + ISOUtil.dumpString(r.pack()));
        //Update transaction with response
        transfer.setStatus(ResponseCode.valueOfCode(r.getString(39)).name());
        transfer.setResponseCode(r.getString(39));
        if (r.getString(39).equalsIgnoreCase("00")) {
            transfer.setReversed(Boolean.TRUE);
        }
        metBankTransferRepository.save(transfer);
        return r.getString(39);
    }


    @Override
    public void saveTransactions(MetBankTransfer transaction) {
        metBankTransferRepository.save(transaction);
    }

    @Override
    public void updateTransactions(MetBankTransfer transaction, ISOMsg response) {
        MetBankTransfer transfer = metBankTransferRepository.findByRrn(transaction.getRrn()).orElseThrow(
                () -> new EntityNotFoundException("Transaction does not exist")
        );
        transfer.setStatus(ResponseCode.valueOfCode(response.getString(39)).name());
        transfer.setResponseCode(response.getString(39));
        metBankTransferRepository.save(transaction);
    }
}
