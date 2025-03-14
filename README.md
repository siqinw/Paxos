Setup and Execution
1. Build Java code
In the terminal,
javac src/RPC/*.java;

2. Start the Server
In the terminal, start the server:
cd src/;
java RPC.CoordinatorServer;

Expected output:
[LOG][CoordinatorServer] <timestamp> - Coordinator Server is running...

3. Start the Client
Open a new terminal window and start the client:
cd src/;
java RPC.Client 127.0.0.1;

Expected output:
[LOG] <timestamp> - Connected to Key-Value Store RMI Server.
[LOG] <timestamp> - Pre-populating Key-Value Store with initial data...

4. Testing the Key-Value Store
You can manually enter commands to test the server:
PUT name Alice
GET name
DELETE name

Expected responses:
Response: PUT OK: name
Response: VALUE: Alice
Response: DELETE OK: name

4. Stopping the Server & Clients
- To stop the server, press `CTRL+C` in the terminal.
- To stop the client, press `CTRL+C` or type in "exit" in the terminal.
