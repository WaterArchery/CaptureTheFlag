# CaptureTheFlag

A simple Capture The Flag minigame plugin for Paper servers. Two teams (Red and Blue) try to steal the other team's flag and bring it back to their base.

## Requirements

- **Java 21**
- **Paper 1.21.11**

## How to Build

```bash
git clone https://github.com/WaterArchery/CaptureTheFlag.git
cd CaptureTheFlag
./gradlew build
```

## How to Use

1. Put the jar file into your server's `plugins/` folder and restart the server.
2. Create a game:
   ```
   /ctf create <gameId>
   ```
3. Go to where you want each team to spawn and set the locations:
   ```
   /ctf set-location <gameId> RED
   /ctf set-location <gameId> BLUE
   ```
4. Go to where you want each flag to be and set them:
   ```
   /ctf set-flag <gameId> RED
   /ctf set-flag <gameId> BLUE
   ```
5. Players can join with `/ctf join <gameId> [RED|BLUE]`.
6. Start the game with `/ctf start <gameId>`.

## Commands

| Command | What it does | Permission |
|---|---|---|
| `/ctf join <game> [team]` | Join a game | — |
| `/ctf leave` | Leave the game you are in | — |
| `/ctf score` | See the current score | — |
| `/ctf create <game>` | Create a new game | `ctf.admin.create` |
| `/ctf start <game>` | Start a game | `ctf.admin.start` |
| `/ctf stop <game>` | Stop a running game | `ctf.admin.stop` |
| `/ctf set-flag <game> <team>` | Set flag location for a team | `ctf.admin.setflag` |
| `/ctf set-location <game> <team>` | Set spawn point for a team | `ctf.admin.setlocation` |
| `/ctf info <game>` | Show game setup info | `ctf.admin.info` |
| `/ctf reload` | Reload config files | `ctf.admin.reload` |

All admin commands also work with the `ctf.admin.*` permission.
