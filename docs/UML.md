# Robot Worlds — UML Diagrams

> **Legend.** Solid boxes = classes that exist in the repo today (even if stubbed).
> Dashed/italic notes = planned for Iterations 2–3 per the README.
> `<<interface>>`, `<<abstract>>`, `<<enumeration>>`, `<<Runnable>>`, `<<Singleton>>` are stereotypes.

---

## 1. Class Diagram — Whole System

```mermaid
classDiagram
    direction LR

    %% ==================== SHARED ====================
    namespace shared {
        class Protocol {
            <<utility>>
            +CMD_LAUNCH : String
            +CMD_LOOK : String
            +CMD_FIRE : String
            +CMD_STATE : String
            +RESULT_OK : String
            +RESULT_ERROR : String
        }

        class Request {
            -robot : String
            -command : String
            -arguments : String[]
            +getRobot() String
            +getCommand() String
            +getArguments() String[]
        }

        class Response {
            -result : String
            -message : String
            -data : Object
            -state : Object
        }

        class StateTransmission {
            +encode(c : Command) String
            +decode(data : String) Command
        }
    }

    %% ==================== CLIENT ====================
    namespace client {
        class RobotClient {
            -host : String
            -port : int
            -socket : Socket
            -mapper : ObjectMapper
            -serverIn : BufferedReader
            -serverOut : PrintWriter
            +start() void
            -commandLoop() void
            -toRequest(line) Request
            -toJson(req) String
            -fromJson(json) Response
            -handleResponse(json) void
            +main(args)$ void
        }

        class NetworkHandler {
            -socket : Socket
            +connect(host, port) void
            +send(msg : String) void
            +receive() String
            +close() void
        }
    }

    %% ==================== SERVER ====================
    namespace server {
        class Server {
            +main(args)$ void
            +isHosting()$ Boolean
        }

        class RobotServer {
            -port : int
            +RobotServer(port : String)
            +RobotServer()
            +init() void
        }

        class ClientHandler {
            <<Runnable>>
            -specificSock : Socket
            +run() void
        }

        class RobotServices {
            <<Singleton>>
            -instance$ : RobotServices
            -log : Logger
            +getInstance()$ RobotServices
            +addRobot() void
            +getAllPlayers() void
            +isValid() Boolean
            +execute() void
        }

        class WorldReader {
            -prop : Properties
            +getWorldMap() String[]
        }
    }

    %% ==================== WORLD ====================
    namespace world {
        class IWorld {
            <<interface>>
            +addRobot(r : Robot) boolean
            +removeRobot(name) void
            +isBlocked(x, y) boolean
            +pathBlocked(from, to) boolean
            +getRobots() List~Robot~
        }

        class RobotWorld {
            -robots : Map~String,Robot~
            -obstacles : List~Obstacle~
            -width : int
            -height : int
            -visibility : int
        }

        class Obstacle {
            <<abstract>>
            #x : int
            #y : int
            +blocks(x, y)* boolean
        }

        class SquareObstacle
        class Pit
        class Lake
    }

    %% ==================== ROBOT ====================
    namespace robot {
        class Robot {
            -name : String
            -position : Point
            -direction : Direction
            -shields : int
            -shots : int
            -status : OperationalMode
            +move(steps) void
            +turn(dir) void
            +takeHit() void
        }

        class OperationalMode {
            <<enumeration>>
            NORMAL
            REPAIR
            RELOAD
            DEAD
        }

        class RobotMake {
            <<factory>>
            +sniper()$ Robot
            +tank()$ Robot
        }
    }

    %% ==================== COMMANDS ====================
    namespace commands {
        class Command {
            <<abstract>>
            #name : String
            #argument : String[]
            +execute(r : Robot, w : IWorld)* Response
        }

        class MoveCommand
        class LookCommand
        class FireCommand
        class RepairCommand
        class ReloadCommand
        class LaunchCommand
        class StateCommand
        class CommandFactory {
            <<factory>>
            +build(req : Request)$ Command
        }
    }

    %% ==================== RELATIONSHIPS ====================
    Server ..> RobotServer : creates
    RobotServer "1" o-- "*" ClientHandler : spawns thread per client
    ClientHandler ..> RobotServices : delegates to
    ClientHandler ..> CommandFactory : parses requests
    ClientHandler ..> StateTransmission : serializes

    CommandFactory ..> Command : returns
    Command <|-- MoveCommand
    Command <|-- LookCommand
    Command <|-- FireCommand
    Command <|-- RepairCommand
    Command <|-- ReloadCommand
    Command <|-- LaunchCommand
    Command <|-- StateCommand

    IWorld <|.. RobotWorld
    RobotWorld "1" o-- "*" Robot : contains
    RobotWorld "1" o-- "*" Obstacle : contains
    Obstacle <|-- SquareObstacle
    Obstacle <|-- Pit
    Obstacle <|-- Lake

    Robot --> OperationalMode
    RobotMake ..> Robot : creates

    Command ..> Robot : acts on
    Command ..> IWorld : queries

    RobotClient *-- Request
    RobotClient *-- Response
    RobotClient ..> NetworkHandler : (planned)
    RobotClient ..> Protocol : uses keys

    RobotServer ..> WorldReader : loads world.properties
    RobotServices ..> IWorld : owns world state
```

---

## 2. Sequence Diagram — `launch` command (Iteration 1 → 3)

```mermaid
sequenceDiagram
    autonumber
    actor U as User (CLI)
    participant C as RobotClient
    participant H as ClientHandler<br/>(per-thread)
    participant F as CommandFactory
    participant W as RobotWorld
    participant R as Robot

    U->>C: HAL launch sniper
    C->>C: toRequest() builds Request{robot,command,args}
    C->>C: toJson(request)
    C->>H: JSON line over Socket
    H->>H: parse JSON -> Request
    H->>F: build(request)
    F-->>H: LaunchCommand
    H->>W: addRobot("HAL", "sniper")
    W->>R: new Robot(name, make)
    W-->>H: Robot state (position, shields...)
    H->>H: build Response{result:OK, state:{...}}
    H-->>C: JSON line back over Socket
    C->>C: fromJson(line)
    C-->>U: prints "OK" + state
```

---

## 3. Sequence Diagram — `fire` command (Iteration 3)

```mermaid
sequenceDiagram
    autonumber
    actor U as User
    participant C as RobotClient
    participant H as ClientHandler
    participant Cmd as FireCommand
    participant W as RobotWorld
    participant R1 as Attacker Robot
    participant R2 as Victim Robot

    U->>C: HAL fire
    C->>H: {"robot":"HAL","command":"fire"}
    H->>Cmd: execute(R1, W)
    Cmd->>W: robotsInLineOfSight(R1)
    W-->>Cmd: [R2]
    Cmd->>R2: takeHit()
    R2->>R2: shields-- ; if 0 -> status=DEAD
    Cmd-->>H: Response{result:OK,<br/>data:{distance, hit:"R2"}}
    H-->>C: JSON
    C-->>U: "Hit R2 at distance 3"
```

---

## 4. State Diagram — Robot lifecycle

```mermaid
stateDiagram-v2
    [*] --> NORMAL : launch
    NORMAL --> REPAIR : repair cmd
    REPAIR --> NORMAL : repair time elapsed
    NORMAL --> RELOAD : reload cmd
    RELOAD --> NORMAL : reload time elapsed
    NORMAL --> DEAD : shields == 0
    DEAD --> [*] : removed from world
```

---

## 5. Component View

```mermaid
flowchart LR
    subgraph CLIENT["Client JVM"]
        RC[RobotClient]
        NH[NetworkHandler]
        RC --- NH
    end

    subgraph SERVER["Server JVM"]
        RS[RobotServer<br/>ServerSocket :2146]
        CH[ClientHandler<br/>Thread per client]
        SVC[RobotServices<br/>singleton]
        WORLD[RobotWorld]
        CFG[(world.properties)]

        RS -->|spawns| CH
        CH --> SVC
        SVC --> WORLD
        RS -. reads .-> CFG
    end

    NH <-->|JSON over TCP| RS
```

---

## Status snapshot — what's real vs stub

| Class | State today |
|---|---|
| `Server`, `RobotServer`, `ClientHandler` | implemented (basic echo loop) |
| `RobotClient` | implemented (Request/Response inner classes, JSON via Jackson) |
| `Command` | abstract class skeleton only — `execute()` not yet defined |
| `LookCommand`, `MoveCommand` | stubs / one-line placeholders |
| `Robot`, `IWorld`, `RobotWorld` | empty files (comment-only) |
| `OperationalMode` | enum complete |
| `WorldReader`, `RobotServices` | scaffolded |
| `Protocol`, `NetworkHandler` | placeholder comments — Iteration 1 work |
| `Obstacle`, `RobotMake`, `CommandFactory`, `StateCommand`, `FireCommand`, etc. | not yet created — Iteration 2/3 |
