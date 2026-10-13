# Lamport Chat

A multi-client chat in Java where every client keeps a Lamport logical clock, so events can be ordered without a shared physical clock.

Course project (Distributed Systems, ENICarthage, November to December 2025) by Bahaeddine Hamrouni.

## What it does

- A thread-per-client TCP server on port 5000 routes private messages between users.
- The server broadcasts the list of connected users to every client.
- The Swing client has one tab per private conversation, a user list and the current clock value.
- Lamport clock rule: increment before every send; on receive, set the clock to `max(local, received) + 1`.

## Messages

Plain-text lines over TCP. A client's first line is its username; the server broadcasts `USERLIST:<names>`; private messages are colon-separated lines carrying the sender, the target, the text and the sender's clock value.

## Build and run

Built with JDK 23. From the repository root:

```bash
javac -d out $(find src -name "*.java")
java -cp out server.ServerMain
java -cp out client.ClientMain      # run once per user, in separate terminals
```

The host and port (127.0.0.1:5000) are constants in the source. Start the server before the clients.

## Layout

```
src/common/   shared classes
src/server/   ChatServer, ClientHandler, ServerMain
src/client/   ChatClient (Lamport clock), ClientMain, ui/MultiChatWindow
docs/         project report (PDF) and slides
```

## Known limitations

- The message format splits on `:`, so a message containing `:` corrupts the clock field and stops the receiving client's listener thread.
- The sender name is supplied by the client and forwarded unchanged, so a client can impersonate another user. Usernames are not validated or unique.
- Some classes (`Protocol`, `Message`, `User`, `MessageListener`, the two config classes) are not used yet.
- The clock is displayed but messages are not sorted by it. Lamport clocks order causally related events; they cannot detect concurrent ones (vector clocks can).
- No automated tests, no TLS.
