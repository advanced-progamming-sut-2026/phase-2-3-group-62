```markdown
<div align="center">

# 🌱 Plants vs. Zombies 2 Remake 🧟‍♂️
### Advanced Programming Course Project (AP) • Sharif University of Technology

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![LibGDX](https://img.shields.io/badge/LibGDX-1.12.1-red?style=for-the-badge&logo=libgdx&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVC-blue?style=for-the-badge)
![Network](https://img.shields.io/badge/Network-Socket%20Client%2FServer-green?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Completed-success?style=for-the-badge)

<p align="center">
  A complete and feature-rich remake of <b>Plants vs. Zombies 2</b> built in pure Java using the <b>LibGDX</b> framework and a custom <b>Multiplayer Socket Engine</b>.
</p>

---

</div>

## 📌 Project Overview

This project was developed in **three distinct phases** as part of the Advanced Programming course curriculum, transforming from a raw terminal-based simulation into a full graphical and networked multiplayer game.


```

```
      [ Phase 1 ]                 [ Phase 2 ]                 [ Phase 3 ]
   Terminal Engine            LibGDX Graphic UI           Multiplayer & Server
 ┌─────────────────┐         ┌─────────────────┐         ┌────────────────────┐
 │ • OOP Core      │  ───>   │ • LibGDX Visuals│  ───>   │ • Socket Server    │
 │ • Logic & Math  │         │ • PAM Animations│         │ • Online PvP (1v1) │
 │ • CLI Turn-based│         │ • Custom Audio  │         │ • DB Sync & Chat   │
 └─────────────────┘         └─────────────────┘         └────────────────────┘

```

```

---

## 🚀 Development Phases

| Phase | Focus Area | Key Features & Deliverables |
| :--- | :--- | :--- |
| **Phase 1: Engine Logic** | Terminal Implementation | Pure object-oriented game logic, CLI board representation, plant shooting vectors, zombie pathfinding, and mathematical tick rates. |
| **Phase 2: GUI Engine** | LibGDX Graphical Interface | Full visual rendering, PAM animation integration, customized lawn shaders, dynamic HUD, audio synthesizer/manager, and seed selection cards. |
| **Phase 3: Multiplayer** | Networking & Server Sync | Multi-threaded client-server architecture, 1v1 online matchmaking, Couch Co-op, live profile sync, and in-game emote/reaction broadcasting. |

---

## 🎮 Game Modes

* **Adventure Mode**: Journey through iconic chapters with distinctive mechanics (Ancient Egypt, Frostbite Caves, Dark Ages, Big Wave Beach).
* **Mini-Games Selection**:
  * 🏺 **Vasebreaker**: Break vases to unveil hidden defenders or surprise threats.
  * 🧠 **I, Zombie**: Switch sides to deploy zombie hordes and conquer brain defenses.
  * 🎳 **Wall-nut Bowling**: Fast-paced ricochet defense using rolling nuts.
  * 💎 **Beghouled**: Match-3 puzzle integration inside the lawn grid.
  * 🧟 **Zombotany**: Battle hybrid plant-zombie enemies.
* **Couch Play (Local 1v1)**: Split-control dual input on a single machine (Keyboard vs Mouse).
* **Online Multiplayer (PvP)**: Real-time network battle with dedicated Plant and Zombie player roles.

---

## 📸 Screenshots

<div align="center">
  <table width="100%">
    <tr>
      <td width="50%" align="center">
        <img src="screenshots/gameplay_main.png" alt="Main Lawn Gameplay" width="100%"/>
        <br/><b>Main Adventure Mode</b>
      </td>
      <td width="50%" align="center">
        <img src="screenshots/multiplayer_pvp.png" alt="Online Multiplayer Battle" width="100%"/>
        <br/><b>Online 1v1 PvP Mode</b>
      </td>
    </tr>
    <tr>
      <td width="50%" align="center">
        <img src="screenshots/minigame_selection.png" alt="Mini-games Selection" width="100%"/>
        <br/><b>Mini-Games Hub</b>
      </td>
      <td width="50%" align="center">
        <img src="screenshots/emotes_panel.png" alt="In-Game Emotes Drawer" width="100%"/>
        <br/><b>Live Emote & Reaction Drawer</b>
      </td>
    </tr>
  </table>
</div>

---

## 🏗️ Architecture & Technology Stack


```

ps2/
├── model/           # Core game logic, Entities, Buffs, Cooldowns, Seasons
├── view/            # LibGDX Screens, Renderers, Scene2D UI widgets, HUDs
├── controller/      # Action handlers, PreGame loaders, State controllers
├── network/         # Client-Server protocol, Socket handlers, Packet routing
└── assets/          # Sprites, PAM animations, Background textures, SFX, Music

```

* **Core Language:** Java 17+
* **Rendering Framework:** LibGDX (SpriteBatch, ShapeRenderer, Scene2D)
* **Animation System:** PopCap PAM Engine parser
* **Network Protocol:** Custom TCP socket communication with JSON serialization
* **Architecture Pattern:** Model-View-Controller (MVC)

---

## ⚙️ Installation & Running

### Prerequisites
* **Java Development Kit (JDK):** Version 17 or higher
* **Git**

### 1. Clone the repository
```bash
git clone [https://github.com/your-username/pvz2-sharif.git](https://github.com/your-username/pvz2-sharif.git)
cd pvz2-sharif

```

### 2. Start the Dedicated Game Server

```bash
javac -d bin -sourcepath src src/network/GameServer.java
java -cp bin network.GameServer

```

### 3. Launch the Client Game

```bash
java -jar build/libs/PvZ2-Client.jar

```

---

## 👥 Contributors

| Contributor | GitHub / Handle |
| --- | --- |
| 🧑‍💻 **Kian** | `kian` |
| 🧑‍💻 **Darrrth** | `darrrth` |
| 🧑‍💻 **Arian** | `arian` |

---
