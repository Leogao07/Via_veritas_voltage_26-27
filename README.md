# Via Veritas Voltage

Robot source code for the **2026-2027 FIRST Tech Challenge BIOBUZZ** season.

This repository belongs to `Leogao07` and provides a shared starting point for robot programming, tuning, and code review. All project documentation, code comments, and authored driver-facing messages use English.

New to the team or to programming? Start with [A Note for New Team Members](notefornew.md)
for a welcome, a file map, and a suggested reading order.

## FTC Android Studio project

This repository includes the official FTC Robot Controller **v12.0** project and
FTC SDK **12.0.0** dependencies, pinned to upstream commit
`e14c2aeb33e84d4ea21697beeea9ac49557be870`.
Upstream: https://github.com/FIRST-Tech-Challenge/FtcRobotController/releases/tag/v12.0

| Path | Purpose |
| --- | --- |
| `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/auto/auto_V1.java` | Auto execution scaffold; no autonomous route actions yet. |
| `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/auto/auto_debug_V1.java` | Auto compile-time settings; currently the telemetry update interval. |
| `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/teleop/TeleOP_v1.java` | Team TeleOp: Pinpoint pose feedback, PD driving, intake, plate presets, and hive aiming (hold X blue, B red). |
| `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/teleop/Tele_debug_v1.java` | Compile-time settings, including the hive tag IDs for aiming, and Driver Station diagnostics. |
| `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/vision/HiveAim.java` | Aiming math: finds the middle of a hive from the Limelight angles of its visible AprilTags. Used by TeleOp. |
| `FtcRobotController/` | Official Robot Controller application library, resources, and disabled SDK examples. |
| `gradlew`, `gradlew.bat`, `gradle/` | Gradle 9.1.0 wrapper. |

The team's TeleOp program and tuning helper live together in the `teleop/`
directory under `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`, with
the package `org.firstinspires.ftc.teamcode.teleop`. This folder organization
preserves the control logic, tuning values, and Driver Station name `TeleOP_v1`.

The `vision/` directory holds `HiveAim.java` (package
`org.firstinspires.ftc.teamcode.vision`), the math TeleOp uses to aim at a hive.
It is a helper class, not an OpMode. See [TeleOp hive aiming](#teleop-hive-aiming).

The `auto/` directory contains the Auto program and its tuning helper, using the
package `org.firstinspires.ftc.teamcode.auto`. The execution files contain control
logic; each mode's Debug helper supplies its adjustable settings. After building
and installing the app, `auto_V1` appears in Driver Station's Auto list. It is a
starting scaffold that waits for START, reports completion, and exits without
commanding hardware. See the [new team member guide](notefornew.md#our-two-robot-modes-and-their-files)
for the two-mode file map.

The SDK supplies the existing `com.qualcomm.hardware.gobilda.GoBildaPinpointDriver`;
no extra Pinpoint library is required.

## Build and deploy

Using VS Code? Follow the [step-by-step macOS and Windows robot deployment guide](docs/VSCODE-ROBOT-DEPLOYMENT.md)
for local setup, USB or Wi-Fi connection, build/install commands, hardware
configuration, first TeleOp run, and troubleshooting. It also explains how to
move from Codespaces to a local computer for robot deployment.

### Quick start: connect to the robot from VS Code on a Mac

One-time setup:

1. Install Android Studio. It provides the Android SDK in `~/Library/Android/sdk`
   and the `adb` tool. You do not need to use it for editing.
2. Install **JDK 21**, for example Eclipse Temurin 21 from https://adoptium.net
   (choose the aarch64 build on Apple Silicon Macs).
3. Add these lines to `~/.zshrc`, then quit VS Code (Cmd+Q) and reopen it:

   ```sh
   export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
   export ANDROID_HOME="$HOME/Library/Android/sdk"
   export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
   ```

4. Check the setup in VS Code's terminal. `java -version` should report 21, and
   `adb version` should print a version number.
5. Run the first build while you have internet access. If the SDK licenses are
   accepted, Gradle downloads the missing SDK Platform 30 and Build-Tools 35.0.0
   automatically:

   ```sh
   ./gradlew assembleDebug
   ```

Each time you deploy:

1. Power on the robot and connect the Control Hub to the Mac with a USB-C data cable.
2. Run `adb devices`. The Control Hub should be listed with status `device`.
3. Build and install:

   ```sh
   ./gradlew assembleDebug && adb install -r TeamCode/build/outputs/apk/debug/TeamCode-debug.apk
   ```

4. On the Driver Station, wait for the robot to reconnect, select the `2026-27`
   configuration if needed, choose your OpMode, then press INIT and START.

Every OpMode in `TeamCode`, TeleOp and Autonomous alike, is packaged into this one
app, so this single command deploys all of them. Driver Station lists each class
under Autonomous or TeleOp according to its `@Autonomous` or `@TeleOp` annotation.
The `debug` build type only means the app is signed with the shared
`libs/ftc.debug.keystore`; it is the normal build for FTC practice and competition.

Deploying over Wi-Fi instead of USB:

1. Join the Mac to the robot's Wi-Fi network, which has the Control Hub's name
   (`32898-RC`).
2. Connect ADB to the Control Hub and confirm it is listed with status `device`:

   ```sh
   adb connect 192.168.43.1:5555
   adb devices
   ```

3. Build and install with the same command as for USB.
4. When finished, run `adb disconnect 192.168.43.1:5555`.

The robot network has no internet access. Builds normally work offline after the
first successful build; if Gradle reports a download failure, use
`./gradlew assembleDebug --offline` or build on a network with internet first.
If USB is also connected, `adb install` reports more than one device; select the
Wi-Fi target with `adb -s 192.168.43.1:5555 install -r TeamCode/build/outputs/apk/debug/TeamCode-debug.apk`.
`192.168.43.1` is the default Control Hub address; if it does not connect, check
the address on Driver Station's Program & Manage page.

Common problems:

| Message | Fix |
| --- | --- |
| `command not found: adb` | Check the `~/.zshrc` lines above and reopen VS Code. |
| `adb devices` shows nothing | Check robot power and use a USB-C cable that carries data, not just charging. |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE: signatures do not match` | The hub has an app signed with a different key, such as the factory-installed Robot Controller. Run `adb uninstall com.qualcomm.ftcrobotcontroller` once, then install again. Robot configurations in the hub's `FIRST` folder are kept. Builds from this repository share `libs/ftc.debug.keystore`, so teammates' builds replace each other without this step. |
| `XXXX-DS does not match YYYY-RC` | The Driver Station and Control Hub names use different team numbers. Our team number is 32898, so the names must be `32898-DS` and `32898-RC`. Rename the wrong device from the Driver Station menu > Settings. |
| `no voltage sensor` | No hardware configuration is active. Open Configure Robot and activate `2026-27`. |

New teammates: follow the [Android Studio setup and collaboration guide](docs/ANDROID-STUDIO.md)
for cloning, JDK/SDK configuration, building, deployment, and the shared Git workflow.

1. Open this repository's root directory in Android Studio. Use a version supporting
   Android Gradle Plugin **8.13.2** and select **JDK 21** as the Gradle JDK.
2. Install Android SDK Platform **30**, Build Tools **35.0.0**, and Platform Tools
   through SDK Manager. Allow Gradle sync to download the FTC dependencies.
3. Let Android Studio create `local.properties` with your machine's SDK path, or set
   `ANDROID_HOME`. Do not commit machine-specific paths.
4. Build the `TeamCode` module. It produces the complete Robot Controller APK.
5. Connect the robot controller using the team's USB/ADB deployment procedure,
   select `TeamCode`, and run it on that device. Use a compatible Driver Station
   from the same official SDK release.
6. Match the hardware configuration below and verify behavior on the robot.

Command-line build (Windows: use `gradlew.bat`):

```sh
./gradlew :TeamCode:assembleDebug
```

APK: `TeamCode/build/outputs/apk/debug/TeamCode-debug.apk`.
With the intended robot controller connected and selected through ADB:

```sh
./gradlew :TeamCode:installDebug
```

The upstream public `libs/ftc.debug.keystore` is included because the official build
requires it. It is a shared development key, not a private team signing credential.
Do not commit private signing keys or passwords.

See [SDK integration notes](docs/SDK-INTEGRATION.md) for provenance, verification,
and robot checks, and [the upstream SDK README](docs/FTC-SDK-README.md) for release notes.

### Hardware configuration names

| Name | Device |
| --- | --- |
| `leftfront` | Left-front drive motor |
| `leftback` | Left-back drive motor |
| `rightfront` | Right-front drive motor |
| `rightback` | Right-back drive motor |
| `odometry` | goBILDA Pinpoint device |
| `intake` | Intake motor |
| `plate` | Position servo |
| `intakesensor` | goBILDA analog distance sensor at the intake; voltage output |
| `outtakesensor` | goBILDA analog distance sensor at the outtake; voltage output |
| `Flywheel` | Flywheel motor |
| `elevator` | Elevator motor |
| `Spoon` | Spoon servo |
| `Camera_angle` | Camera angle servo |
| `limelight` | Limelight 3A (USB); pipeline 0 is the AprilTag pipeline |

Use the exact spelling and capitalization shown above in the Robot Controller
hardware configuration.

The current OpMode preserves the existing Pinpoint pod configuration. If another OpMode supplies encoder resolution, offsets, or directions, bring its verified setup into this OpMode as appropriate for the actual robot.

### Additional mechanism devices

`Flywheel` and `elevator` are motors; `Spoon` and `Camera_angle` are servos;
`limelight` is the Limelight 3A camera. These names and device types are recorded
for the hardware configuration. The current `TeleOP_v1.java` does not control
`Flywheel`, `elevator`, or `Spoon`. For [hive aiming](#teleop-hive-aiming) it reads
`limelight` and moves `Camera_angle` to its aim position at START; both are
optional, and TeleOp still runs if either is missing from the configuration.
Models, hub/port assignments, motor directions and power settings, servo ranges
and starting positions, and the remaining driver controls have not been specified yet.

### Intake and outtake sensor signals

The team reports that both sensors have been added to the Robot Controller
hardware configuration using the exact names `intakesensor` and `outtakesensor`.
Both are goBILDA analog distance sensors that report a voltage. The team's stated
signal meanings apply to both sensors:

| Reported voltage | Meaning |
| --- | --- |
| 0 V | No object detected |
| 3 V | Object detected |

These are the team's supplied detection states, not a voltage-to-distance
calibration. The exact sensor models, hub/port assignments, and handling of
intermediate voltages have not been recorded yet.

The current `TeleOP_v1.java` does not read these sensors. This records the hardware
setup for future development; sensor telemetry and mechanism automation remain to
be implemented and tested.

### Current driver controls

| Input | Action |
| --- | --- |
| Gamepad 1 A | Release after starting, then hold to enable PD driving. Releasing stops drive power. |
| Gamepad 1 left stick | Move the virtual translation target using the target heading as the reference. |
| Gamepad 1 right-stick X | Change the target heading. |
| Gamepad 1 X / B | Hold to aim at the nearest blue / red hive. Also enables PD like A; the left stick still moves the robot. See [TeleOp hive aiming](#teleop-hive-aiming). |
| Gamepad 2 Y / X / A / B | Select plate targets of 27 / 117 / 207 / 297 degrees. |
| Gamepad 2 left / right bumper | Run the intake forward / reverse; release to stop. Forward takes priority if both are held. |

These values describe the current code, not a hardware calibration. Tune settings in `Tele_debug_v1.java`. Releasing A resets the translation target and preserves the heading target.

### TeleOp hive aiming

Hold **gamepad 1 X** to aim at the nearest blue hive or **gamepad 1 B** to aim at
the nearest red hive. The robot turns until the middle of that hive is straight
ahead of the Limelight and keeps tracking it while the button is held. Release
the button to return to normal driving.

- Holding X or B also enables PD driving, like holding A: the robot holds its
  position and the left stick still moves it, while aiming replaces the right
  stick. If both buttons are held, blue wins.
- The nearest hive is the one whose visible tags look biggest. The robot stays on
  that hive while the button is held; if it is out of view for more than 0.5 s,
  the nearest visible hive of the same color is chosen instead. Meanwhile the
  robot holds its last aimed heading.
- Aiming turns at up to TeleOp's normal maximum turn rate, about 110°/s.
- At START, `Camera_angle` moves to its aim position so the Limelight looks at the
  tags. Without a `limelight` in the configuration, X and B do nothing and the
  rest of TeleOp works as before.

#### Hives and their tags

| Hive | Color | Button | AprilTag IDs, left to right |
| --- | --- | --- | --- |
| 1 | Blue | X | 42, 43, 44, 45 |
| 2 | Blue, audience side | X | 38, 39, 40, 41 |
| 3 | Red | B | 30, 31, 32, 33 |
| 4 | Red, audience side | B | 34, 35, 36, 37 |

Each hive has four AprilTags in a row. `HIVE_TAG_IDS` in `Tele_debug_v1.java`
lists them **left to right as seen from the robot facing the hive**, by their place
on the field rather than by number. The order tells the program where each tag
sits in the row, so a wrong order makes the aim wrong or gets the view rejected.

#### How the middle of a hive is found

The Limelight reports each visible tag's horizontal angle, `tx`.
[`vision/HiveAim.java`](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/vision/HiveAim.java)
uses the fact that the four tags lie on a straight line to work out the angle to
the middle of the row, even when only some of the tags are visible:

| Tags in view | How the middle is found | Accuracy (simulated, inside the camera view) |
| --- | --- | --- |
| 3 or 4 | Least-squares fit of the row that also corrects for perspective when the hive is seen at an angle | Exact, apart from Limelight noise (about 0.1°) |
| 2 | Straight line through the two tags | Usually within 1°; up to about 3° when close to the hive and at a steep angle |
| 1 | The tag is shifted by its place in the row. The shift uses the tag spacing measured the last time two or more tags were in view, scaled by how big the tag looks, so it follows distance changes. Until two tags have been seen, the robot centers on the single tag. | Usually within 1°; up to about 4° |

Tracking therefore starts as soon as any one tag of the chosen color is visible,
and each extra tag makes the aim more accurate. If the visible tags' angles do not
increase from left to right, that view is ignored.

#### Aiming settings

`DRIVE_ENABLE_BUTTON` is with the other button settings at the top of
`Tele_debug_v1.java`; the rest are in its HIVE AIMING section.

| Setting | Default | Meaning |
| --- | --- | --- |
| `DRIVE_ENABLE_BUTTON` | `A` | Gamepad 1 button held to enable PD driving. |
| `AIM_GAMEPAD`, `AIM_BLUE_BUTTON`, `AIM_RED_BUTTON` | `1`, `X`, `B` | Gamepad and buttons that aim at blue and red hives. |
| `HIVE_TAG_IDS` | the table above | Each hive's tag IDs, left to right. Empty rows are ignored. |
| `BLUE_HIVES`, `RED_HIVES` | `{1, 2}`, `{3, 4}` | Hive numbers (rows of `HIVE_TAG_IDS`, counting from 1) that each button tracks. |
| `HIVE_TAG_POSITIONS_CM` | `{-30, -10, 10, 30}` | Tag centers along the row, measured from the middle of the hive; left is negative. Only the relative spacing matters, so the defaults fit four equally spaced tags centered on the hive. |
| `LIMELIGHT_PIPELINE` | `0` | Limelight pipeline used; pipeline 0 is the AprilTag pipeline. |
| `LIMELIGHT_MAX_STALENESS_MS` | `100` | Limelight results older than this are ignored. |
| `AIM_OFFSET_DEG` | `0` | Moves the aim point; positive aims to the robot's right. Use it if the camera or shooter is off the robot's centerline. |
| `AIM_TURN_SIGN` | `+1` | Set to `-1` only if aiming turns the robot away from the hive. |
| `AIM_LOST_MS` | `500` | How long the chosen hive may be out of view before another is chosen. |
| `AIM_CAMERA_ANGLE_POSITION` | `1.0` | `Camera_angle` servo position set at START. |

#### Aiming telemetry

| Driver Station shows | Meaning |
| --- | --- |
| `Ready: blue hive 1, red hive none` | Not aiming. The nearest hive of each color that could be aimed at right now. |
| `Tracking blue hive 1 with 3 tags, error 0.42 deg` | Aiming. The error is the angle still left to the middle of the hive. |
| `...; centering on the tag until 2 are seen` | Only one tag is visible and the tag spacing has not been measured yet. |
| `Searching for a red hive tag` | The button is held, but no tag of that color is in view. |
| `Hive 3 out of view; holding heading` | The chosen hive was lost briefly; the robot keeps its last aimed heading. |
| `Limelight not found; aim buttons disabled` | The active configuration has no `limelight`. |
| `Hive 3 tag tx (deg), left to right: 30: -8.1  31: -2.9 ...` | The visible tags of a hive and their angles. The values must increase from left to right; `OUT OF ORDER` means that hive's IDs are listed in the wrong order. |

#### Checking the aiming on the robot

1. Select `TeleOP_v1` and press INIT. Point the camera at each hive in turn and
   check that its `Hive N tag tx` values increase from left to right.
2. Press START, release all buttons, then hold X or B near a hive. For a first
   test, raise the drive wheels or clear the area around the robot.
3. If the robot turns away from the hive, set `AIM_TURN_SIGN = -1`. If it always
   settles to one side of the middle, adjust `AIM_OFFSET_DEG`.
4. After editing `Tele_debug_v1.java`, save the file before building, because the
   build uses only saved files. Then rebuild and install.

## Work with the team

Clone the repository after accepting a collaborator invitation:

```sh
git clone https://github.com/Leogao07/Via_veritas_voltage_26-27.git
cd Via_veritas_voltage_26-27
```

Start each task from an updated `main`, then create a new branch:

```sh
git switch main
git pull --ff-only origin main
git switch -c leo/drive-tuning
```

After editing and reviewing your changes:

```sh
git diff
git add TeamCode/src/main/java/org/firstinspires/ftc/teamcode
git commit -m "Tune drive settings"
git push -u origin leo/drive-tuning
```

Open a pull request targeting `main` and include the build and robot-test results. Use a fresh branch name for each task. See [CONTRIBUTING.md](CONTRIBUTING.md) for invitations, synchronization, and conflict handling.

## Season resources

- [Official BIOBUZZ game and season overview](https://www.firstinspires.org/programs/ftc/game-and-season)
- [Official 2026-2027 game materials and competition manual](https://ftc-resources.firstinspires.org/ftc/game)

Use the current official manual and team updates when making game-specific decisions. This repository does not replace those resources.

## Verification status

- Initial Java import: checked against the supplied files.
- English documentation: reviewed during repository setup.
- FTC SDK 12.0.0 compilation and debug APK packaging: passed on October 23, 2026.
- GitHub Actions APK build: [passed for commit `ec9cf0c`](https://github.com/Leogao07/Via_veritas_voltage_26-27/actions/runs/39948262006).
- Robot testing: not performed as part of repository setup.
- TeleOp hive aiming: blue and red hive tracking tested on the robot on October 30, 2026.
