# CS6650 Project 4: Paxos-Based Replicated Key-Value Store

## Overview

This project implements a fault-tolerant replicated key-value store using the Paxos consensus algorithm.
Clients interact with any server to perform PUT, GET, and DELETE operations. The system ensures strong consistency even with random server failures and recoveries. Each server acts as a Proposer, Acceptor, and Learner in the Paxos protocol.

Servers randomly fail and recover to simulate realistic distributed system behavior. The client supports automatic retries in case of temporary server failures.

## Architecture

- 5 Servers (PaxosKeyValueStoreServer):
  - Run independently and participate in Paxos rounds.
  - Simulate random failure (10% per request) and self-recovery (after 0.1 seconds).
- Client (PaxosKeyValueStoreClient):
  - Connects to all servers.
  - Supports interactive command-line input for PUT, GET, DELETE.
  - Issue each request to a random server.
  - Retries operations automatically on failure (up to 10 times).
Notes
If more than 2 servers fail simultaneously, Paxos consensus may not be achieved until some servers recover.

## Files

- PaxosKeyValueStoreServer.java  
  Implementation of the replicated server with Paxos roles.

- PaxosKeyValueStoreClient.java  
  Interactive client that communicates with the servers.

- PrepareRequest.java, PrepareResponse.java, AcceptRequest.java, AcceptResponse.java  
  Message classes used in Paxos communication.

- StartAllServers.java
  Program to start all 5 servers.

## How to Compile

```bash
cd src/
javac RPC/*.java


##** Start the Server**
```bash
java RPC.StartAllServers

Each server binds to RMI Registry on port 1099 + serverId.

## **Start the Client**
```bash
java RPC.PaxosKeyValueStoreClient

Client to pre-populate the Key-Value store with data and a set of keys. do at least five of each operation: 5 PUTs, 5 GETs, 5 DELETEs.

## **Client Commands**
Interactive mode supports:

-  PUT <key> <value> — Insert or update a key.
-  GET <key> — Retrieve the value for a key.
-  DELETE <key> — Remove a key.
-  EXIT — Quit the client.

All operations automatically retry if transient errors occur.

## **Project Highlights**
-  Paxos Protocol: Ensures strong consistency with majority quorum.
-  Fault Tolerance: Random server failures and automatic recoveries.
-  Multi-threading: Java RMI inherently supports concurrent operations.
-  Logging: Timestamped client logs for all operations.
-  Retry Logic: Client retries failed requests up to 10 times.

Requirements
Java 8 or higher

No external libraries required

The proposer does not count itself as an acceptor; majority acceptance is required from acceptors.

Author
Written for Northeastern University CS6650 Distributed Systems, Spring 2025.

