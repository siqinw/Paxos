package RPC;

public class PrepareRequest implements java.io.Serializable {
    public PrepareRequest(int proposalNumber, String key) {
        this.proposalNumber = proposalNumber;
        this.key = key;
    }

    int proposalNumber;
    String key;
}
