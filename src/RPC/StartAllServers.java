package RPC;

public class StartAllServers {
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down all Paxos servers...");
            // Optional: unbind RMI objects here if you want clean unregistration
        }));

        try {
            for (int i = 1; i <= 5; i++) {
                int serverId = i;
                new Thread(() -> {
                    try {
                        PaxosKeyValueStoreServer.main(new String[]{String.valueOf(serverId)});
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
                Thread.sleep(1000); // small delay
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
