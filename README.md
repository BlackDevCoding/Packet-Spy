# PacketSpy (Fabric 1.21.8)

Client-side packet viewer for Fabric. Saves all packets to NDJSON and serves a live **web UI**.

- Web UI at `http://127.0.0.1:8753/`
- Live stream via Server-Sent Events at `/events`
- Logs in `<gameDir>/packet-logs/packets-YYYYMMDD-HHMMSS.ndjson`
- Package: `dev.blackdev`

## Build
```bash
./gradlew build
```

## Dev Run
```bash
./gradlew runClient
```

## Web UI
Open `http://127.0.0.1:8753/`. Set a custom port with `-Dpacketspy.port=PORT`.
