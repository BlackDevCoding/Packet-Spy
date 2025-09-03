
# Packet-Spy

![Project Banner](assets/packetspy/banner.png)

Client-side Fabric mod that logs **every inbound and outbound Minecraft packet**, saves them to NDJSON, and serves a **live web UI** on localhost.

[![License](https://img.shields.io/github/license/BlackDevCoding/Packet-Spy?style=for-the-badge)](MIT)
[![Release](https://img.shields.io/github/v/release/BlackDevCoding/Packet-Spy?display_name=tag&sort=semver&style=for-the-badge)](https://github.com/BlackDevCoding/Packet-Spy/releases)
[![CI](https://img.shields.io/github/actions/workflow/status/BlackDevCoding/Packet-Spy/release.yml?style=for-the-badge)](https://github.com/BlackDevCoding/Packet-Spy/actions)
[![Issues](https://img.shields.io/github/issues/BlackDevCoding/Packet-Spy?style=for-the-badge)](https://github.com/BlackDevCoding/Packet-Spy/issues)
[![Pull Requests](https://img.shields.io/github/issues-pr/BlackDevCoding/Packet-Spy?style=for-the-badge)](https://github.com/BlackDevCoding/Packet-Spy/pulls)

## Table of Contents
- [Quick Start](#quick-start)
- [✨ Features](#-features)
- [🛠️ Installation](#️-installation)
- [Usage](#usage)
- [Screenshots](#screenshots)
- [🗂️ Project Structure](#️-project-structure)
- [Configuration](#-configuration)
- [API Reference](#api-reference)
- [Testing](#testing)
- [Deployment](#deployment)
- [🗺️ Roadmap](#️-roadmap)
- [Contributing](#contributing)
- [Bug Reports](#bug-reports)
- [Feature Requests](#feature-requests)
- [Documentation Issues](#documentation-issues)
- [General Issues](#general-issues)
- [Changelog](#changelog)
- [❓ FAQ](#-faq)
- [Related Projects](#related-projects)
- [Acknowledgments](#acknowledgments)
- [License](#license)
- [Contact](#contact)

---

## Quick Start

```bash
git clone https://github.com/BlackDevCoding/Packet-Spy.git
cd Packet-Spy
./gradlew build
./gradlew runClient
```

**Open the web UI:** `http://127.0.0.1:8753/` (falls back to the next free port up to `8762`).

---

## ✨ Features
- Full packet capture (IN/OUT).
- Live Web UI via SSE with filtering, pause, clear.
- Disk logging at `<gameDir>/packet-logs/packets-YYYYMMDD-HHMMSS.ndjson`.
- Fabric-native mixins on `ClientConnection`.
- Zero telemetry (strictly local).

---

## ️ Installation

**Prereqs:** Minecraft 1.21.8, Fabric Loader ≥ 0.17.2, Fabric API `0.132.0+1.21.8`, Java 21+.

End-users:
1. Download the latest release JAR.
2. Put it in `mods/`.
3. Launch Minecraft (Fabric), open `http://localhost:8753/`.

---

## Usage

- Live stream: `GET /events`
- Health: `GET /health`
- Static: `/`, `/style.css`, `/app.js`

Change port: `-Dpacketspy.port=8799`.

---

## Screenshots
_Add screenshots or a short GIF of the web UI here._

---

## ️ Project Structure
```
src/
├─ main/
│  ├─ java/dev/blackdev/
│  │  ├─ PacketSpyMod.java
│  │  ├─ log/PacketLogger.java
│  │  ├─ mixin/ClientConnectionMixin.java
│  │  └─ web/WebServer.java
│  └─ resources/
│     ├─ fabric.mod.json
│     ├─ mixins.packetspy.json
│     ├─ assets/packetspy/icon.png
│     └─ web/ (index.html, style.css, app.js)
.github/workflows/release.yml
build.gradle
gradle.properties
settings.gradle
```

---

## Configuration
`-Dpacketspy.port=8799`

---

## API Reference
`GET /events` → Server-Sent Events  
`GET /health` → `200 ok`

---

## Testing
`./gradlew runClient`

---

## Deployment
Automated GitHub Releases via CI tag `v<mod_version>-XY`.

---

## ️ Roadmap
- WebSocket control
- Persistent UI settings
- Packet payload preview

---

## Contributing
PRs welcome — keep changes focused and documented.

---

## Bug Reports / Feature Requests / Questions
Use the issue templates on GitHub.

---

## Changelog
See Releases.

---

## ❓ FAQ
**Localhost:** `http://127.0.0.1:8753/`  
**Uploads:** None, local only.  
**Java:** 21 recommended.

---

## Related Projects
Fabric API, SpongePowered Mixin.

---

## Acknowledgments
Thanks to the Fabric, Yarn, and Mixin communities.

---

## License
MIT
