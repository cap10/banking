package zw.co.jugaad.metbankbankingservice;

import lombok.Data;

@Data
public class ResponseMessage {

    private String remoteReference;

    private String code;

    private String description;
}
