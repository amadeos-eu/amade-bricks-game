# amade-bricks-game

**amade-bricks-game** is the classic wall of bricks on the [amadeOS](https://github.com/amadeos-eu/amadeos) desktop: break every brick with ball and paddle. Built on [amade-game-engine](https://github.com/amadeos-eu/amade-game-engine); runs on other operating systems as well.

![License](https://img.shields.io/badge/license-EUPL--1.2-blue)
![Java](https://img.shields.io/badge/Java-25-orange)

## Playing

- **Enter**, **Space** or a **click** (or F2) starts a game. Move the paddle with the **arrow keys** (or A and D) or the **mouse**, launch the ball with **Space** or a **click**.
- Where the ball meets the paddle decides its angle: the middle sends it straight up, the edges steeply. Every hit makes the ball faster.
- Coloured bricks break at the first hit, **silver** ones take two, **gold** ones never break. Clear all breakable bricks to reach the next level; there are five.
- Three balls per game. **P** or **Esc** pauses, and so does leaving the window.
- **Best scores**, ten of them.
- **F11** switches to full screen and back. *Game → Graphics* chooses how the field reaches the screen: **Direct** (sharp, at the screen's full size) or **Pixels** (the field at 640 × 400, enlarged with hard square pixels). **F12** shows how long each frame takes.
- Sound effects can be switched off under *Game → Sound*.
- On the title screen the computer plays.

The keys can be changed in `~/.config/amadeos/bricks/bricks.properties`, with the names Java gives them, several separated by spaces:

```properties
keys.left=LEFT A
keys.right=RIGHT D
keys.launch=SPACE UP
keys.pause=P ESCAPE
```

Texts in English, Deutsch, Français, Español and Italiano.

## Levels

The levels are text files in [`src/main/resources/eu/amadeos/games/bricks/levels`](src/main/resources/eu/amadeos/games/bricks/levels), `01.txt` to `05.txt`: one line per row of fifteen bricks, `r o y g b v` for the colours, `s` for silver, `X` for gold, `.` for a gap, `#` for a comment. A new level is a new file with the next number.

## Files

| What | Where |
|---|---|
| Settings (graphics, sound, keys) | `~/.config/amadeos/bricks/bricks.properties` |
| Name for high scores | `~/.config/amadeos/bricks/player.properties` |
| Best scores | `~/.local/share/amadeos/games/bricks/classic.scores` |

## Building

```bash
./gradlew test          # the window test needs a display (xvfb-run on servers)
./gradlew run
./gradlew benchmark     # frame times in full screen, in both render modes
./gradlew deb           # build/distributions/amade-bricks-game_<version>_all.deb
./gradlew installToAmadeOs   # copies the package to ../amadeos/config/packages.chroot/
```

The sprites are drawn by `tools/MakeSprites.java` at four times their size in the game; run it again only to change them (see the comment at its top). `./gradlew screenshots -Pshots=<folder>` draws the handbook's screenshots.

With `../amade-game-engine` (and `../amade-commons`) checked out next to this repository, Gradle builds those checkouts instead of the JitPack versions.

## Structure

- `domain`: the rules without user interface or time of their own. `Game` (paddle, ball, bricks, collisions swept over the whole step, score, lives, levels; it reports `Event`s), `Level` (a wall read from text), `BrickType`, `Brick`, `Ball`, `Paddle`, `Field` (sizes and speeds), `Autopilot` (plays the title screen's demo), `GameResult`.
- `repository`: `LevelFiles` (the level files), `BricksSettings` (graphics, sound, keys).
- `ui.game`: the running game on the loop thread. `BricksGame` (loop, controls, scenes: title, game, pause, end), `FieldPainter` (background, bricks, paddle and ball as sprites, particles), `Hud` (score, level, lives and texts, drawn over the world), `Effects` (events become splinters and sounds), `SoundBank`, `Arena` (what the presenter may ask of the game).
- `ui.main`: Model-View-Presenter for everything around the field: `BricksPresenter`, `BricksView`, `BricksWindow`.

From the engine: `GameLoop`, `GameCanvas` (two layers, render modes, frame times), `Sprite`, `Sweep` and `Box`, `Particles`, `Sound` and `Mixer`, `Keyboard` and `Controls`, `SceneStack`, `FullScreen`, high scores.

Comments of the form `// --8<-- [start:name]` mark the excerpts the [Game Developer's Handbook](https://datazuul.github.io/amade-game-engine/) quotes; chapter 21 is about this game.

## Licence

[EUPL-1.2](LICENSE)
