# oop-ex-toy-robot-group

## Project Architecture

```text
src/main/java/za/co/wethinkcode/robots/
├── client/                     <-- NEW: Handles the Robot user interface
│   ├── RobotClient.java        # Main entry point for the client
│   └── NetworkHandler.java     # Handles socket connection to server
├── server/                     <-- EXISTING: Expand this
│   ├── RobotServer.java        # Main entry point, listens for connections
│   ├── ClientHandler.java      # Runnable/Thread to manage each connected robot
│   ├── world/                  # The "Domain": Obstacles, Map, Physics
│   │   ├── IWorld.java         # Interface for the world
│   │   └── RobotWorld.java     # Implementation of the 2D world logic
│   ├── robot/                  # Robot state and behavior
│   │   └── Robot.java          # State: position, direction, shields
│   └── commands/               # Command Pattern (Iteration 1: forward, quit, etc.)
│       ├── Command.java        # Abstract class or Interface
│       └── MoveCommand.java    # Specific command logic
└── shared/                     <-- NEW: Common code used by both
    ├── Protocol.java           # Constants for JSON keys/values
    └── RequestResponse.java    # POJOs for easy JSON serialization
```

---

### 2. The Core "Testing Model"

```text
src/test/java/za/co/wethinkcode/robots/
├── server/
│   ├── WorldTest.java          # Test if obstacles actually block movement
│   └── RobotTest.java          # Test state changes (e.g., turning left)
└── client/
    └── ProtocolTest.java       # Test if JSON messages are formatted correctly
```

---

### 3. Recommended Technical Modules
To keep the code clean as we work through Iteration 1, we organize our work into these four distinct areas:

| Module | Responsibility | Key Classes to Create |
| :--- | :--- | :--- |
| **Networking** | Managing Socket connections and Threads. | `RobotServer`, `ClientHandler` |
| **World Engine** | Handling the coordinate system and collisions. | `RobotWorld`, `Obstacle` |
| **Command Logic** | Interpreting string commands into actions. | `Command`, `CommandFactory` |
| **Data Handling** | Turning Objects into JSON and vice versa. | `JsonParser` (using Jackson or Gson) |

---

### 4. Group Strategy for the First 2 Weeks
Since its a group of 5 members, I suggest we assign the "Architecture" like this to avoid clashing in the code:

1.  **Lead Architect:** Sets up the `Command` pattern structure and the `pom.xml` dependencies (Jackson/JUnit).
2.  **Server Specialist:** Builds the `RobotServer` and the `ClientHandler` (Thread management).
3.  **World Builder:** Creates the `RobotWorld` class and the logic for the "hard-coded obstacle" required in Iteration 1.
4.  **Client Developer:** Builds the `RobotClient` that reads user input and sends it over the socket.
5.  **Quality Assurance (QA):** Responsible for the `src/test` suite and ensuring the JSON protocol matches the project requirements.


### Summary Checklist for the Team
* [ ] **Member A:** Create the `IWorld` interface and `RobotWorld` implementation in `server.world`.
* [ ] **Member B:** Create the `Robot` class (state holder) in `server.robot`.
* [ ] **Member C:** Build the `Command` classes (Forward, Back, Quit) in `server.commands`.
* [ ] **Member D:** Set up the `ServerSocket` in `RobotServer.java` to listen for connections.
* [ ] **Member E:** Set up the `RobotClient.java` to send a simple JSON string to the server.

## Iteration 2 "Redistribution" Plan


To ensure everyone contributes equally and understands the full system, we should move away from the "One person per feature" model and adopt **Cross-functional Pairing** and **Code Review rotations**.

---

### Role 1: The "Architect & Config" (1 person)
* **Focus:** Removing hard-coded values.
* **Tasks:** Implement the configuration file reader (using a `.properties` or `.json` file) for world size, visibility, repair times, and shield max.
* **Integration:** This person must provide the `Config` object to everyone else's classes.

### Role 2: The "Physics & Collision" Team (2 people)
* **Focus:** Obstacles, Pits, and Movement logic.
* **Tasks:** * Defining the `Obstacle` types (Mountain, Lake, Pit).
    * Logic for `isPositionBlocked(x, y)` and `doesPathIntersectObstacle(from, to)`.
    * The "falling into Tumbolia" logic for pits.
* **Why 2 people?** This is the most math-heavy part. Pairing ensures the logic is sound.

### Role 3: The "Combat & Timing" Team (2 people)
* **Focus:** Shields, Weapons, and Threaded "Resting."
* **Tasks:**
    * The `repair` and `reload` commands (implementing `Thread.sleep()` without locking up the whole server).
    * The `fire` logic (calculating if a shot hits a robot within range).
    * Managing robot "Make" configurations (Sniper vs. Tank).

---


## Updated Architecture for Iteration 2
Our file structure should evolve to handle the new requirements:

```text
za.co.wethinkcode.robots.server/
├── config/
│   └── WorldConfig.java        # Loads and holds visibility, repair times, etc.
├── world/
│   ├── Obstacle.java           # Abstract class for Mountain, Lake, Pit
│   ├── SquareObstacle.java     # Implementation of rectangular obstacles
│   └── RobotWorld.java         # Now uses (0,0) as center and N/M bounds
├── robot/
│   ├── Robot.java              # Added: shields, ammo, status (NORMAL/REPAIRING)
│   └── RobotMake.java          # Factory for different models (Sniper, etc.)
└── commands/
    ├── LookCommand.java        # Uses visibility range from config
    ├── RepairCommand.java      # Uses repair time from config
    └── FireCommand.java        # Calculates hits on other robots
```
## Iteration 2 (code testing)
1. World & Movement Tests (The "Physics" Suite)
2. Robot State & Combat Tests (The "Logic" Suite)
3. Integration & Protocol Tests (The "Network" Suite)
---

## Updated Architecture for Iteration 3




In Iteration 3, the architecture shifts from "simple commands" to a **highly structured API**. The biggest change is moving away from basic string handling to a **Request/Response Pipeline** that strictly follows the JSON schema.

Here is how our package structure and logic flow should evolve to meet these requirements.

---

### Updated Package Structure
```text
za.co.wethinkcode.robots/
├── shared/
│   └── protocol/
│       ├── Request.java        # POJO for incoming JSON
│       └── Response.java       # POJO for outgoing JSON (includes 'data' and 'state')
├── server/
│   ├── robot/
│   │   ├── Robot.java          # State: Added 'shots' and 'status' (REPAIR, RELOAD, etc.)
│   │   └── RobotStatus.java    # Enum: NORMAL, REPAIR, RELOAD, DEAD
│   ├── commands/
│   │   ├── FireCommand.java    # Logic for hits/misses and range
│   │   ├── LookCommand.java    # Logic for distance/type/direction array
│   │   └── CommandFactory.java # Now maps JSON command strings to classes
│   └── util/
│       └── JsonTransformer.java # Uses Jackson to convert Objects <-> JSON
```

Iteration 3 is where our project becomes a professional-grade software system. The focus is now on **Protocol Strictness**. Since we are working in a group of 5, this iteration should be treated like an API integration project.

Here is how to distribute the work for Iteration 3 to ensure equality and total system understanding.

---

## 1. The "Protocol First" Work Distribution

### Role 1: The JSON Architect (1 Person)
* **Focus:** Serialization & Deserialization.
* **Tasks:** Create the standard `Request` and `Response` POJOs (Plain Old Java Objects). Ensure that every command output matches the JSON structure exactly (e.g., ensuring `state` is only present when the result is `OK`).
* **Success Metric:** Passing a test where a raw JSON string is converted into a Java Object and back without losing data.

### Role 2: The Combat & Hit Logic Team (2 People)
* **Focus:** The `fire` command and health management.
* **Tasks:** * Implementing the "Line of Sight" for shots.
    * Logic to identify *which* robot was hit (the `data` map in the `fire` response requires the victim's name and distance).
    * Managing the `DEAD` status and removing robots from the world.
* **Why 2 people?** This involves interaction between two different robot objects, which is prone to threading bugs.

### Role 3: The "Sensors" & Visibility Specialist (1 Person)
* **Focus:** The `look` command.
* **Tasks:** * Logic to detect `EDGE`, `OBSTACLE`, and `ROBOT` in four directions.
    * Ensuring the response uses **absolute directions** (NORTH, SOUTH, etc.) regardless of which way the robot is facing.
* **Success Metric:** A robot at `(0,0)` facing SOUTH correctly reports an obstacle at `(0, 5)` as being "SOUTH" at distance 5.

### Role 4: The Integration & State Manager (1 Person)
* **Focus:** The `state` command and status transitions.
* **Tasks:** * Ensuring the `status` string correctly flips between `NORMAL`, `REPAIR`, and `RELOAD`.
    * Handling the `launch` command logic: finding an empty space and rejecting duplicate names.

---

## 2. Critical Testing Strategy for Iteration 3

For Iteration 3, our tests should be **Data-Driven**. We should test the *Strings* coming out of our server.

### A. The "Launch" Validation Test
* **Scenario:** Launch "RobotA". Then try to launch another "RobotA".
* **Expected:** The JSON response must have `"result": "ERROR"` and `"message": "Too many of you in this world"`.

### B. The "Ghost Shot" Test (Fire)
* **Scenario:** Robot A fires at Robot B, but Robot B has 0 shields.
* **Expected:** Robot B's status should change to `DEAD`. Any subsequent command sent by Robot B's client should return an error or show the status as `DEAD`.

### C. The "Look" Range Test
* **Scenario:** Set world visibility to 5. Place an obstacle at 6 steps away.
* **Expected:** The `look` response array should *not* contain that obstacle.
---

## Critical Testing Strategy for Iteration 4



With the strict protocol, multi-threading, and core commands handled, our team is 90% of the way to a "Working" system. However, to move from "it works" to "it's professional," there are **four final layers** our team should address before the final demo.

Think of these as the "Polish and Defense" phase.

---

## 1. The "Observer" (Logging)
The project brief mentions **Logging** as an optional research topic. In a group project, this is actually essential for debugging.
* **The Task:** Implement a logger (like `Log4j` or a simple custom `Logger` class) that writes all incoming and outgoing JSON to a file or a dedicated "Admin" console.
* **Why?** When the Client team says "The server crashed," you can look at the logs and see exactly which JSON message caused the failure.

## 2. Advanced User Experience (UI)
The brief allows for "any UI we wish."
* **The "Pro" Move:** Create a **Server-Side GUI** (using JavaFX or a simple Swing window) that shows a visual map of the world.

## 3. Graceful Shutdown & Persistence
What happens when the Server needs to restart?
* **Robot Disconnects:** If a client crashes, the server should detect the broken socket and remove that robot from the world immediately (so it doesn't become a "ghost" robot).
* **The `quit` Command:** When the admin types `quit` on the server, it must send a final message to all clients telling them the world is ending before closing the sockets.

## 4. The "Robot War" Tournament Strategy
Since the final goal might be a tournament, our team needs to think about **Robot AI**.
* **The Task:** Build a "Smart Client." Instead of a human typing commands, can we write a Client that uses the `look` and `fire` commands automatically to hunt other robots?
---

### Final "Divide and Conquer" for the Last Week

| Member | The "Final Polish" Responsibility |
| :--- | :--- |
| **Member 1** | **Integration:** Final check that all JSON keys match the protocol perfectly. |
| **Member 2** | **Performance:** Stress test the server with 20+ robots to ensure no "Deadlocks." |
| **Member 3** | **UI/UX:** Build a "Dump" command that prints a pretty ASCII map of the world. |
| **Member 4** | **Documentation:** Finalize the Wiki (Class, Domain, and Component diagrams). |
| **Member 5** | **Presentation:** Prepare the demo script and the poster for the final showcase. |

