package zw.co.jugaad.metbankbankingservice.api;


import lombok.extern.slf4j.Slf4j;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zw.co.jugaad.metbankbankingservice.ResponseMessage;
import zw.co.jugaad.metbankbankingservice.model.MetBankTransfer;
import zw.co.jugaad.metbankbankingservice.model.ResponseCode;
import zw.co.jugaad.metbankbankingservice.model.SwiftCode;
import zw.co.jugaad.metbankbankingservice.operations.SimpleTraceGenerator;
import zw.co.jugaad.metbankbankingservice.operations.TransactionService;
import zw.co.jugaad.metbankbankingservice.repository.SwiftCodeRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@Slf4j
@RequestMapping("/api/v1/bank-transactions")
public class BankTransactionController {

    private final TransactionService transactionService;

    private final SwiftCodeRepository swiftCodeRepository;

    private final SimpleTraceGenerator traceNumber;

    public BankTransactionController(TransactionService transactionService, SwiftCodeRepository swiftCodeRepository, SimpleTraceGenerator traceNumber) {
        this.transactionService = transactionService;
        this.swiftCodeRepository = swiftCodeRepository;
        this.traceNumber = traceNumber;
    }

    @PostMapping("/bank-to-wallet/{mobile}/{debitAccount}/{amount}")
    public ResponseEntity<ResponseMessage> bankToWallet(
            @PathVariable("mobile") String mobile,
            @PathVariable("debitAccount") String debitAccount,
            @PathVariable("amount") BigDecimal amount) throws Exception {
        log.info("############### Bank to Wallet Request: {}, {}, {}", mobile, debitAccount, amount);

        Random random = new Random();
        MetBankTransfer transaction = new MetBankTransfer();
        transaction.setMti("0200");
        transaction.setProcessingCode("540000");
        transaction.setAmount(amount.multiply(new BigDecimal(100)).toString());
        transaction.setTransactionDate(ISODate.formatDate(new Date(), "MMddyyhhmm"));
        transaction.setStan(ISODate.formatDate(new Date(), "yyhhmm"));
        transaction.setTimeLocalTransaction(ISODate.formatDate(new Date(), "yyhhmm"));
        transaction.setDateLocalTransaction(ISODate.formatDate(new Date(), "MMdd"));
        transaction.setRrn(ISOUtil.padleft(String.valueOf(traceNumber.nextTrace()), 12, '0'));
        transaction.setCardAcceptorTid("CASHMET");
        transaction.setCardAcceptorIdCode(mobile);
        transaction.setCardAcceptorLocation("Bank to Wallet");
        transaction.setCurrencyCode("932");
        transaction.setAccountDebit(debitAccount);
        transaction.setAccountCredit("1000010428");
        ISOMsg isoMsg = null;

        try {
            isoMsg =
                    CompletableFuture.supplyAsync(() ->

                    {
                        try {
                            return transactionService.b2W(transaction);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ISOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).get(20, TimeUnit.SECONDS);

        } catch (TimeoutException ex) {
            transaction.setStatus("TIMEOUT");
            transactionService.saveTransactions(transaction);
            throw new Exception("Transaction timed out");
        }

        transaction.setResponseCode(isoMsg.getString(39));
        transaction.setStatus("COMPLETE");
        transactionService.saveTransactions(transaction);
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setRemoteReference(isoMsg.getString(37));
        responseMessage.setCode(isoMsg.getString(39));
        responseMessage.setDescription(ResponseCode.valueOfCode(isoMsg.getString(39)).name());
        return ResponseEntity.ok(responseMessage);

    }

    @PostMapping("/wallet-to-bank/{mobile}/{creditAccount}/{amount}")
    public ResponseEntity<ResponseMessage> walletToBank(@PathVariable("mobile") String mobile,
                                                        @PathVariable("creditAccount") String creditAccount,
                                                        @PathVariable("amount") BigDecimal amount) throws Exception {
        log.info("############### Wallet to Bank Request: {}, {}, {}", mobile, creditAccount, amount);


        MetBankTransfer transaction = new MetBankTransfer();
        transaction.setMti("0200");
        transaction.setProcessingCode("540000");
        transaction.setAmount(amount.multiply(new BigDecimal(100)).toString());
        transaction.setTransactionDate(ISODate.formatDate(new Date(), "MMddyyhhmm"));
        transaction.setStan(ISODate.formatDate(new Date(), "yyhhmm"));
        transaction.setTimeLocalTransaction(ISODate.formatDate(new Date(), "yyhhmm"));
        transaction.setDateLocalTransaction(ISODate.formatDate(new Date(), "MMdd"));
        transaction.setRrn(ISOUtil.padleft(String.valueOf(traceNumber.nextTrace()), 12, '0'));
        transaction.setCardAcceptorTid("CASHMET");
        transaction.setCardAcceptorIdCode(mobile);
        transaction.setCardAcceptorLocation("Wallet to Bank");
        transaction.setCurrencyCode("932");
        transaction.setAccountDebit("1000010428");
        transaction.setAccountCredit(creditAccount);
        ISOMsg isoMsg = null;
        try {
            isoMsg =
                    CompletableFuture.supplyAsync(() ->

                    {
                        try {
                            return transactionService.w2B(transaction);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ISOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).get(20, TimeUnit.SECONDS);

        } catch (TimeoutException ex) {
            transaction.setStatus("TIMEOUT");
            transactionService.saveTransactions(transaction);
            throw new Exception("Transaction timed out");
        }

        transaction.setResponseCode(isoMsg.getString(39));
        transaction.setStatus("COMPLETE");
        transactionService.saveTransactions(transaction);
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setRemoteReference(isoMsg.getString(37));
        responseMessage.setCode(isoMsg.getString(39));
        responseMessage.setDescription(ResponseCode.valueOfCode(isoMsg.getString(39)).name());
        return ResponseEntity.ok(responseMessage);


    }

    @PostMapping("/settle-fees/{mobile}/{creditAccount}/{amount}")
    public ResponseEntity<ResponseMessage> settleFeesToBank(@PathVariable("mobile") String mobile,
                                                            @PathVariable("amount") BigDecimal amount) throws Exception {
        log.info("############### Wallet to Bank Request: {}, {}, {}", mobile, amount);


        MetBankTransfer transaction = new MetBankTransfer();
        transaction.setMti("0200");
        transaction.setProcessingCode("540000");
        transaction.setAmount(amount.multiply(new BigDecimal(100)).toString());
        transaction.setTransactionDate(ISODate.formatDate(new Date(), "MMddyyhhmm"));
        transaction.setStan(ISODate.formatDate(new Date(), "yyhhmm"));
        transaction.setTimeLocalTransaction(ISODate.formatDate(new Date(), "yyhhmm"));
        transaction.setDateLocalTransaction(ISODate.formatDate(new Date(), "MMdd"));
        transaction.setRrn(ISOUtil.padleft(String.valueOf(traceNumber.nextTrace()), 12, '0'));
        transaction.setCardAcceptorTid("CASHMET");
        transaction.setCardAcceptorIdCode(mobile);
        transaction.setCardAcceptorLocation("Fees Settlement");
        transaction.setCurrencyCode("932");
        transaction.setAccountDebit("1000010428");
        transaction.setAccountCredit("PL52021");
        ISOMsg isoMsg = null;
        try {
            isoMsg =
                    CompletableFuture.supplyAsync(() ->

                    {
                        try {
                            return transactionService.w2B(transaction);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ISOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).get(20, TimeUnit.SECONDS);

        } catch (TimeoutException ex) {
            transaction.setStatus("TIMEOUT");
            transactionService.saveTransactions(transaction);
            throw new Exception("Transaction timed out");
        }

        transaction.setResponseCode(isoMsg.getString(39));
        transaction.setStatus("COMPLETE");
        transactionService.saveTransactions(transaction);
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setRemoteReference(isoMsg.getString(37));
        responseMessage.setCode(isoMsg.getString(39));
        responseMessage.setDescription(ResponseCode.valueOfCode(isoMsg.getString(39)).name());
        return ResponseEntity.ok(responseMessage);


    }

    @PostMapping("/tax-fees/{bank}/{creditAccount}/{amount}")
    public ResponseEntity<ResponseMessage> settleTaxToBank(
            @PathVariable("bank") String bank,
            @PathVariable("creditAccount") String creditAccount,
            @PathVariable("amount") BigDecimal amount) throws Exception {
        log.info("############### Wallet to Bank Request: {}, {}, {}", bank, creditAccount, amount);


        MetBankTransfer transaction = new MetBankTransfer();
        transaction.setMti("0200");
        transaction.setProcessingCode("540000");
        transaction.setAmount(amount.multiply(new BigDecimal(100)).toString());
        transaction.setTransactionDate(ISODate.formatDate(new Date(), "MMddyyhhmm"));
        transaction.setStan(ISODate.formatDate(new Date(), "yyhhmm"));
        transaction.setTimeLocalTransaction(ISODate.formatDate(new Date(), "yyhhmm"));
        transaction.setDateLocalTransaction(ISODate.formatDate(new Date(), "MMdd"));
        transaction.setRrn(ISOUtil.padleft(String.valueOf(traceNumber.nextTrace()), 12, '0'));
        transaction.setCardAcceptorTid("CASHMET");
        transaction.setCardAcceptorIdCode("ZIMRA");
        transaction.setCardAcceptorLocation("Tax Settlement");
        transaction.setCurrencyCode("932");
        transaction.setAccountDebit("1000010428");
        transaction.setAccountCredit("ZWL1260500010001");
        transaction.setReceivingAccountNameRtgs("ZIMRA");
        transaction.setReceivingAccountNumberRtgs(creditAccount);
        SwiftCode swiftCode = swiftCodeRepository.findById(bank).get();
        transaction.setReceivingBankSwiftCodeRtgs(swiftCode.getSwift());
        ISOMsg isoMsg = null;
        try {
            isoMsg =
                    CompletableFuture.supplyAsync(() ->

                    {
                        try {
                            return transactionService.settleMerchantBiller(transaction);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ISOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).get(20, TimeUnit.SECONDS);

        } catch (TimeoutException ex) {
            transaction.setStatus("TIMEOUT");
            transactionService.saveTransactions(transaction);
            throw new Exception("Transaction timed out");
        }

        transaction.setResponseCode(isoMsg.getString(39));
        transaction.setStatus("COMPLETE");
        transactionService.saveTransactions(transaction);
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setRemoteReference(isoMsg.getString(37));
        responseMessage.setCode(isoMsg.getString(39));
        responseMessage.setDescription(ResponseCode.valueOfCode(isoMsg.getString(39)).name());
        return ResponseEntity.ok(responseMessage);


    }

    @PostMapping("/settle-biller-merchant/{merchant}/{bank}/{creditAccount}/{amount}")
    public ResponseEntity<ResponseMessage> settleBillerMerchantToBank(@PathVariable("merchant") String merchant,
                                                            @PathVariable("bank") String bank,
                                                            @PathVariable("creditAccount") String creditAccount,
                                                            @PathVariable("amount") BigDecimal amount) throws Exception {
        log.info("############### Wallet to Bank Request: {}, {}, {}", merchant, bank, creditAccount, amount);


        MetBankTransfer transaction = new MetBankTransfer();
        transaction.setMti("0200");
        transaction.setProcessingCode("540000");
        transaction.setAmount(amount.multiply(new BigDecimal(100)).toString());
        transaction.setTransactionDate(ISODate.formatDate(new Date(), "MMddyyhhmm"));
        transaction.setStan(ISODate.formatDate(new Date(), "yyhhmm"));
        transaction.setTimeLocalTransaction(ISODate.formatDate(new Date(), "yyhhmm"));
        transaction.setDateLocalTransaction(ISODate.formatDate(new Date(), "MMdd"));
        transaction.setRrn(ISOUtil.padleft(String.valueOf(traceNumber.nextTrace()), 12, '0'));
        transaction.setCardAcceptorTid("CASHMET");
        transaction.setCardAcceptorIdCode(merchant);
        transaction.setCardAcceptorLocation("Biller/Merchant Settlement");
        transaction.setCurrencyCode("932");
        transaction.setAccountDebit("1000010428");
        SwiftCode swiftCode = swiftCodeRepository.findById(bank).get();
        if (swiftCode.getBank().equalsIgnoreCase("METBANK")) {
            transaction.setAccountCredit(creditAccount);
        } else {
            transaction.setAccountCredit(creditAccount);
            transaction.setAccountCredit("ZWL1260500010001");
            transaction.setReceivingAccountNameRtgs(merchant);
            transaction.setReceivingAccountNumberRtgs(creditAccount);
            SwiftCode codes = swiftCodeRepository.findById(bank).get();
            transaction.setReceivingBankSwiftCodeRtgs(codes.getSwift());
        }
        ISOMsg isoMsg = null;
        try {
            isoMsg =
                    CompletableFuture.supplyAsync(() ->

                    {
                        try {
                            return transactionService.settleMerchantBiller(transaction);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ISOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).get(20, TimeUnit.SECONDS);

        } catch (TimeoutException ex) {
            transaction.setStatus("TIMEOUT");
            transactionService.saveTransactions(transaction);
            throw new Exception("Transaction timed out");
        }

        transaction.setResponseCode(isoMsg.getString(39));
        transaction.setStatus("COMPLETE");
        transactionService.saveTransactions(transaction);
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setRemoteReference(isoMsg.getString(37));
        responseMessage.setCode(isoMsg.getString(39));
        responseMessage.setDescription(ResponseCode.valueOfCode(isoMsg.getString(39)).name());
        return ResponseEntity.ok(responseMessage);


    }
}
