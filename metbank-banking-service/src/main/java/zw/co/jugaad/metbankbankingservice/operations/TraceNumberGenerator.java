package zw.co.jugaad.metbankbankingservice.operations;

public interface TraceNumberGenerator {


    /** Returns the next trace number. */
    public int nextTrace();

    /** Returns the last number that was generated. */
    public int getLastTrace();

}
