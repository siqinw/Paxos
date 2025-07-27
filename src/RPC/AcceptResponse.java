package RPC;

public class AcceptResponse implements java.io.Serializable {
    public AcceptResponse(boolean accepted) {
        this.accepted = accepted;
    }
    boolean accepted;
}
