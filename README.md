# 🌱 Plants vs. Zombies 2 Remake 🧟‍♂️

### Advanced Programming Course Project (AP) • Sharif University of Technology

A complete and feature-rich remake of **Plants vs. Zombies 2** built in pure Java using the **LibGDX** framework and a custom **Multiplayer Socket Engine**.

---

## 📌 Project Overview

This project was developed in **three distinct phases** as part of the Advanced Programming course curriculum, transforming from a raw terminal-based simulation into a full graphical and networked multiplayer game.

```
          [ Phase 1 ]                 [ Phase 2 ]                 [ Phase 3 ]
       Terminal Engine            LibGDX Graphic UI           Multiplayer & Server
     ┌─────────────────┐         ┌─────────────────┐         ┌────────────────────┐
     │ • OOP Core      │  ───>   │ • LibGDX Visuals│  ───>   │ • Socket Server    │
     │ • Logic & Math  │         │ • PAM Animations│         │ • Online PvP (1v1) │
     │ • CLI Turn-based│         │ • Custom Audio  │         │ • DB Sync & Chat   │
     └─────────────────┘         └─────────────────┘         └────────────────────┘

```

---

## 🚀 Development Phases

| Phase | Focus Area | Key Features & Deliverables |
| --- | --- | --- |
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

| Main Adventure Mode | Online 1v1 PvP Mode |
| --- | --- |
|  |  |
| **Mini-Games Hub** | **Live Emote & Reaction Drawer** |
|  |  |

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
git clone https://github.com/your-username/pvz2-sharif.git
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

Developed for the Advanced Programming Course • Sharif University of Technology
