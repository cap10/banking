package zw.co.jugaad.metbankbankingservice.util;

import lombok.extern.slf4j.Slf4j;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.NACChannel;
import org.jpos.iso.packager.ISO87APackager;
import org.jpos.iso.packager.PostPackager;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
@Slf4j
public class ISOConnection {
    private static ISOConnection isoConnection;

    private ISOConnection() {
    }

    public static ISOConnection getInstance() {

        // create object if it's not already created
        if (isoConnection == null) {
            isoConnection = new ISOConnection();
        }

        // returns the singleton object
        return isoConnection;
    }

    public ISOChannel getConnection() throws IOException, ISOException {
        try {
            Logger logger = new Logger();
            logger.addListener(new SimpleLogListener(System.out));
            ISOChannel channel = new NACChannel("172.18.4.122", 8452, new PostPackager(), null);
            ((LogSource) channel).setLogger(logger, "test-channel");
            channel.connect();
            ISOMsg m = new ISOMsg();
            m.setMTI("0800");
            m.set(7, ISODate.getDateTime(new Date()));
            m.set(11, String.valueOf(System.currentTimeMillis() % 1000000));
            m.set(13, "0315");
            m.set(15, "0315");
            m.set(37, "00001603307");
            m.set(41, "T1603307");
            m.set(70, "001");
            channel.send(m);
            ISOMsg r = channel.receive();
            return channel;
        } catch (IOException ex) {
            log.info("########################### Failing to connect: {}", ex.getMessage());
            throw new IOException("Failed to connect");
        }
    }
}



