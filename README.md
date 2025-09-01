# PacketSpy

A client-side Fabric mod for Minecraft 1.21.8 that opens a separate, dark-themed window and shows every network packet in and out while also writing an NDJSON log to disk.

- Live table: time, direction (IN/OUT), packet class
- Always-on-top toggle, quick filter by class, row striping
- File logs at `<gameDir>/packet-logs/packets-YYYYMMDD-HHMMSS.ndjson`
- Works even in headless runs (UI disabled; file logging continues)
- Author: dev.blackdev

## Requirements
- Minecraft 1.21.8
- Fabric Loader 0.17.x
- Fabric API for 1.21.8
- JDK 21

## Build
```bash
gradle build
```

## Run (dev)
```bash
gradle runClient
```
Optional:
```bash
JAVA_TOOL_OPTIONS="--enable-native-access=ALL-UNNAMED -Djava.awt.headless=false"
```

## Install (game)
Copy the JAR from `build/libs/` into your Minecraft `mods/` folder.

## Logs
NDJSON lines like:
```json
{"ts":"2025-09-01T18:40:17Z","direction":"IN","class":"net.minecraft.network.packet.s2c.play.GameJoinS2CPacket","details":"..."}
```

## UI
- Filter: case-insensitive substring match on the packet class
- Pin toggles Always on Top
- Clear removes current table rows

## CI and Releases
Every push builds the mod and publishes a GitHub Release tagged `v<mod_version>-XY`, where `XY` is a random alphanumeric suffix.
