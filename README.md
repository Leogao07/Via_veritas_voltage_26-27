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
| `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/TeleOP_v1.java` | Team TeleOp: Pinpoint pose feedback, PD driving, intake, and plate presets. |
| `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Tele_debug_v1.java` | Compile-time settings and Driver Station diagnostics. |
| `FtcRobotController/` | Official Robot Controller application library, resources, and disabled SDK examples. |
| `gradlew`, `gradlew.bat`, `gradle/` | Gradle 9.1.0 wrapper. |

The team Java files were moved from `src/` into `TeamCode` and given the package
`org.firstinspires.ftc.teamcode`. Their control logic and tuning values are unchanged.
The SDK supplies the existing `com.qualcomm.hardware.gobilda.GoBildaPinpointDriver`;
no extra Pinpoint library is required.

## Build and deploy

Using VS Code? Follow the [step-by-step macOS and Windows robot deployment guide](docs/VSCODE-ROBOT-DEPLOYMENT.md)
for local setup, USB or Wi-Fi connection, build/install commands, hardware
configuration, first TeleOp run, and troubleshooting. It also explains how to
move from Codespaces to a local computer for robot deployment.

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

Use the exact spelling and capitalization shown above in the Robot Controller
hardware configuration.

The current OpMode preserves the existing Pinpoint pod configuration. If another OpMode supplies encoder resolution, offsets, or directions, bring its verified setup into this OpMode as appropriate for the actual robot.

### Additional mechanism devices

`Flywheel` and `elevator` are motors; `Spoon` and `Camera_angle` are servos.
These names and device types are recorded for the hardware configuration.
The current `TeleOP_v1.java` does not initialize or control these four devices.
Their models, hub/port assignments, motor directions and power settings, servo
ranges and starting positions, and driver controls have not been specified yet.

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
| Gamepad 1 left bumper | Release after starting, then hold to enable PD driving. Releasing stops drive power. |
| Gamepad 1 left stick | Move the virtual translation target using the target heading as the reference. |
| Gamepad 1 right-stick X | Change the target heading. |
| Gamepad 2 Y / X / A / B | Select plate targets of 27 / 117 / 207 / 297 degrees. |
| Gamepad 2 left / right bumper | Run the intake forward / reverse; release to stop. Forward takes priority if both are held. |

These values describe the current code, not a hardware calibration. Tune settings in `Tele_debug_v1.java`. Releasing the drive bumper resets the translation target and preserves the heading target.

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
