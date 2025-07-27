package RPC;

public class PrepareResponse implements java.io.Serializable {
    public PrepareResponse(boolean promised) {
        this.promised = promised;
    }

    boolean promised;
}
