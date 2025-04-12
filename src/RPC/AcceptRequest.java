package RPC;

public class AcceptRequest implements java.io.Serializable {
    public AcceptRequest(int proposalNumber, String key) {
        this.proposalNumber = proposalNumber;
        this.key = key;
    }

    int proposalNumber;
    String key;
}
