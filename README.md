# Via Veritas Voltage

Robot source code for the **2026-2027 FIRST Tech Challenge BIOBUZZ** season.

This repository belongs to `Leogao07` and provides a shared starting point for robot programming, tuning, and code review. All project documentation, code comments, and authored driver-facing messages use English.

## Current code

| File | Purpose |
| --- | --- |
| `src/test1.java` | TeleOp with Pinpoint-based pose feedback, PD drive control, intake control, and plate presets. |
| `src/DriveDebug.java` | Drive tuning, motor directions, mechanism controls, and Driver Station diagnostics. |

The Java files were imported from the supplied working copies on October 22, 2026. Their control logic and tuning values have not been changed during repository setup.

**This is a source-only repository, not a complete FTC Android Studio project.** It does not yet include the FTC SDK, Gradle configuration, autonomous routines, or a verified robot build. Season labeling does not establish competition-rule compliance.

## Use in the team's FTC project

1. Open the team's FTC Robot Controller project in Android Studio.
2. Copy both Java files into the same source package in `TeamCode`.
3. The supplied files have no `package` declaration. Add the same declaration to both files if required by the destination package, for example `package org.firstinspires.ftc.teamcode;`.
4. Use an FTC SDK that provides `com.qualcomm.hardware.gobilda.GoBildaPinpointDriver`. Confirm compatibility with the team's installed SDK and hardware.
5. Match the Robot Controller hardware configuration to the names below.
6. Build the complete FTC project and verify behavior on the team's robot before treating the code as tested.

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

The current OpMode preserves the existing Pinpoint pod configuration. If another OpMode supplies encoder resolution, offsets, or directions, bring its verified setup into this OpMode as appropriate for the actual robot.

### Current driver controls

| Input | Action |
| --- | --- |
| Gamepad 1 left bumper | Release after starting, then hold to enable PD driving. Releasing stops drive power. |
| Gamepad 1 left stick | Move the virtual translation target using the target heading as the reference. |
| Gamepad 1 right-stick X | Change the target heading. |
| Gamepad 2 Y / X / A / B | Select plate targets of 27 / 117 / 207 / 297 degrees. |
| Gamepad 2 left / right bumper | Run the intake forward / reverse; release to stop. Forward takes priority if both are held. |

These values describe the current code, not a hardware calibration. Tune settings in `DriveDebug.java`. Releasing the drive bumper resets the translation target and preserves the heading target.

## Work with the team

Clone the repository after accepting a collaborator invitation:

```sh
git clone https://github.com/Leogao07/Via_veritas_voltage.git
cd Via_veritas_voltage
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
git add src/DriveDebug.java src/test1.java
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
- FTC SDK compilation: not yet performed; a complete FTC project is required.
- Robot testing: not performed as part of repository setup.
