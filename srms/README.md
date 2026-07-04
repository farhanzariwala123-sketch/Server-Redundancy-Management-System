# Server Redundancy Management System (SRMS)


## Team
FARHAN ZARIWALA 300222668 farhan.zariwala@student.ufv.ca

## Quick run
Run the top-level script to start monitor, three servers, and admin client:
```bash
./scripts/run.sh


SRMS consists of:
- Monitor: tracks heartbeats and triggers deterministic failover (lowest ID).
- Server instances: three independent JVMs (Primary/Backup roles).
- Client: discovers primary via monitor and sends PROCESS job-id.
- Admin: view logs and trigger manual failover.

Requirements
- Java 11+ (tested on OpenJDK 11)
- Ports used: Monitor 9000, Server1 9101, Server2 9102, Server3 9103

Build
# with javac
javac -d out src/**/*.java

Run (detailed)

start monitor
java -cp out MonitorMain 9000

start servers
java -cp out ServerMain 1 9001 9000  
java -cp out ServerMain 2 9002 9000
java -cp out ServerMain 3 9003 9000

start client
java -cp out ClientMain

Scripts
- scripts/run.sh — starts monitor + 3 servers 
- scripts/kill-primary.sh — kills current primary JVM
- scripts/delay-heartbeat.sh — toggles artificial heartbeat delay
- scripts/run_client - starts the client
- scripts/stop-all.sh - stops everything
- scripts/build.sh

Design notes
- Election: Monitor chooses lowest alive server ID as new primary.
- Heartbeats: Servers send heartbeat every 1s; monitor times out after 5 missed heartbeats (configurable).
- Split-brain avoidance: Monitor is authoritative; servers only promote after receiving PROMOTE from monitor.

Functional & Non-functional requirements mapping
- FR1: Client request served by current primary — implemented in PrimaryHandler.
- FR2: Backup election/promotion — implemented in Monitor.promoteLowestAlive().
- NFR1: Detect primary failure within 5 heartbeats — configured in MonitorConfig.
- NFR2: (Not tested) Resume serving within 2s after failover — not executed in tests.

Testing performed
- Normal operation: start monitor + 3 servers, client requests succeed.
- (Not tested) Primary crash and NFR2 scenario — documented in limitations.

Logs
- Logs are written to logs/server-<id>.log and logs/monitor.log.
- Each entry includes ISO timestamp and event type (HEARTBEAT, PROMOTE, REQUEST).

Files in ZIP
- src/ — Java source
- logs - logs of all terminals
- bin - 
- scripts/ — run/kill/simulate scripts
- docs/ — UML diagrams (PNG)
- report.pdf — project report (max 5 pages)
- README.md

Known limitations
- NFR2 and FR2 failover tests were not executed; see report.pdf for details.



