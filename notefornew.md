# A Note for New Team Members

Welcome to **BTA Robotics Team for FTC BIOBUZZ 26–27**!

Our goal this season is to reach the **State Championship**, and hopefully go even
further to a **National Championship**. We will work toward that goal together,
one idea, practice session, and improvement at a time.

You are welcome here even if you have never written a line of code. You do not
need to understand this entire project before getting started. Our robot code is
in **TeamCode**, and I have included detailed comments in the Java files to explain
what the code does, how the settings work, and what to consider when changing them.
Take your time, read a little at a time, and ask your teammates questions.

This guide will help you find your way around. The main [README](README.md) remains
the technical reference for building the project, hardware names, and driver controls.

## Our two robot modes and their files

We organize our robot programs into two modes:

- **Auto (Autonomous):** the robot executes a programmed sequence after START.
  Its source files live in `auto/`.
- **TeleOp (Tele-Operated):** drivers operate the robot with gamepads.
  Its source files live in `teleop/`.

These two folders contain the team's current source files for each mode. Each
mode has an execution file and a Debug settings file:

- **Execution logic:** `auto_V1.java` and `TeleOP_v1.java` describe what the robot
  does: hardware initialization, the order of actions, and control decisions.
  TeleOp also reads gamepad inputs here.
- **Settings and tuning:** `auto_debug_V1.java` and `Tele_debug_v1.java` hold the
  adjustable values used by their execution files. As actions are implemented,
  these may include speeds, distances, delays, servo targets, or controller gains.
  The TeleOp helper also reports diagnostic information.

The Debug files are Java helpers used by the execution files. They are not
separate modes to select in Driver Station, and their settings are not changed
live by editing the files. Stop the robot, edit a value, rebuild, install the
updated app, and then test again.

The Auto files currently provide a starting scaffold: `auto_V1` waits for START,
reports completion, and finishes without commanding hardware. Its Debug helper
currently sets only the telemetry update interval. The autonomous route and its
motion settings will be added as the team develops them.

## Start with these files

| What you want to learn | Open this file | What you will find |
| --- | --- | --- |
| How to set up Android Studio | [docs/ANDROID-STUDIO.md](docs/ANDROID-STUDIO.md) | Accepting the invitation, cloning the project, configuring JDK/SDK, building, and deploying. |
| Where autonomous execution logic lives | [auto_V1.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/auto/auto_V1.java) | Auto entry point and the place for the programmed sequence; currently a scaffold without robot actions. |
| Where autonomous settings and tuning live | [auto_debug_V1.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/auto/auto_debug_V1.java) | Settings used by auto_V1; currently the telemetry update interval. |
| What our driver-controlled program does | [TeleOP_v1.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/teleop/TeleOP_v1.java) | TeleOp logic: gamepad input, robot position feedback, drivetrain control, intake, and plate presets. |
| Where TeleOp settings and tuning live | [Tele_debug_v1.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/teleop/Tele_debug_v1.java) | Settings for mechanisms, speed commands, controller gains, motor directions, and diagnostic displays. |
| Which hardware names and buttons we use | [README.md](README.md) | Hardware configuration names, driver controls, build instructions, and verification status. |
| How to share changes with teammates | [CONTRIBUTING.md](CONTRIBUTING.md) | Branches, commits, pull requests, reviews, and handling conflicts. |
| What still needs checking on the robot | [docs/SDK-INTEGRATION.md](docs/SDK-INTEGRATION.md) | SDK details, build verification, robot checks, and known issues to follow up. |

## Find our code in the editor

In VS Code's Explorer, or Android Studio's **Project** view, expand these folders:

```text
Via_veritas_voltage_26-27/
└── TeamCode/
    └── src/
        └── main/
            └── java/
                └── org/
                    └── firstinspires/
                        └── ftc/
                            └── teamcode/
                                ├── auto/
                                │   ├── auto_V1.java
                                │   └── auto_debug_V1.java
                                ├── teleop/
                                │   ├── TeleOP_v1.java
                                │   └── Tele_debug_v1.java
                                └── readme.md
```

Some editors combine the folders into `org.firstinspires.ftc.teamcode`. That is
the same location; expand `auto` or `teleop` to find each pair of Java files.
Their packages are `org.firstinspires.ftc.teamcode.auto` and
`org.firstinspires.ftc.teamcode.teleop`, respectively. You can also use your
editor's file search and type any of the four filenames to open it directly.

After building and installing the app, select **auto_V1** from Driver Station's
Auto/Autonomous list or **TeleOP_v1** from its TeleOp list. The execution files
use `@Autonomous` and `@TeleOp`, respectively, to choose their lists. Both Debug
files are helpers and do not appear as selectable programs.

## Read the code one piece at a time

Start with the explanation at the top of [TeleOP_v1.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/teleop/TeleOP_v1.java).
Then look for these names using your editor's text search:

1. `hardwareMap.get`: connects the names in our program to devices configured on
   the robot, such as `intake` and `plate`.
2. `waitForStart`: waits for the driver to start the program.
3. `while (opModeIsActive())`: repeats the control work while the program is running.
4. `updateMechanisms`: reads the buttons that control the intake and plate.
5. `drive`: converts movement commands into power for the four drive motors.
6. `stopDrive`: sets drive motor power to zero.

In [Tele_debug_v1.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/teleop/Tele_debug_v1.java),
start with the plate and intake settings. Each section has comments explaining
units, intended behavior, and tuning considerations. These settings take effect
when you rebuild and run the program again.

In Java, `//` starts a one-line comment, and `/* ... */` surrounds a longer comment.
Comments explain the program to people; the robot does not execute them.
For example, this declaration:

```java
public static final double INTAKE_FORWARD_POWER = 0.8;
```

stores a named setting for the intake's forward motor power. The value is a power
command, not a measured speed. You can understand this setting before learning
what every Java keyword means.

For Auto, start with [auto_V1.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/auto/auto_V1.java).
Find `waitForStart()` and the place after it for future route actions. Then open
[auto_debug_V1.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/auto/auto_debug_V1.java)
to see how the telemetry interval is kept separate from the execution logic.

It is fine to leave the position-control math for later. First understand which
mode runs, which file describes its actions, and which file supplies its settings.

## What are the other project files?

These files support the app and build process. You can return to them when you
need them; you do not need to learn them all on your first day.

| File or folder, relative to the repository root | Purpose |
| --- | --- |
| [TeamCode/build.gradle](TeamCode/build.gradle) | Builds the app containing our team code. |
| [TeamCode/src/main/AndroidManifest.xml](TeamCode/src/main/AndroidManifest.xml) | Android app configuration for the team module. |
| [TeamCode/src/main/res/](TeamCode/src/main/res/) | Team module resources, including strings and camera calibration configuration. |
| [TeamCode/lib/](TeamCode/lib/) | Supporting tooling shipped with the official project. |
| [FtcRobotController/](FtcRobotController/) | Official FTC application code, resources, and example programs. |
| [FtcRobotController/src/main/java/org/firstinspires/ftc/robotcontroller/external/samples/](FtcRobotController/src/main/java/org/firstinspires/ftc/robotcontroller/external/samples/) | Official examples you can read to learn about FTC programming. |
| [build.gradle](build.gradle) | Shared build plugin configuration. |
| [build.common.gradle](build.common.gradle) | Common Android build and signing settings. |
| [build.dependencies.gradle](build.dependencies.gradle) | Lists the FTC SDK libraries the project downloads. |
| [settings.gradle](settings.gradle) | Tells Gradle which modules belong to this project. |
| [gradle.properties](gradle.properties) | Gradle and Android build options. |
| [gradlew](gradlew), [gradlew.bat](gradlew.bat), and [gradle/](gradle/) | Run the project's chosen Gradle version on different computers. |
| [libs/](libs/) | Includes the official shared development signing key. |
| [.gitignore](.gitignore) | Lists generated and local files that Git should leave out. |
| [.github/workflows/android-build.yml](.github/workflows/android-build.yml) | Automatically checks APK builds on GitHub. |
| [.github/pull_request_template.md](.github/pull_request_template.md) | Prompts you to describe and verify a proposed change. |
| [docs/FTC-SDK-README.md](docs/FTC-SDK-README.md) | The official SDK's original README and release notes. |
| [LICENSE](LICENSE) and [doc/legal/](doc/legal/) | License and legal notices shipped with the SDK project. |

`local.properties`, `.idea/`, `.gradle/`, and `build/` folders may appear on your
computer after setup. They contain local settings or generated files and are
excluded from Git. Teammates create their own copies during setup and builds.

## Your first contribution

A useful first contribution can be as small as improving a confusing comment,
recording a test result, or helping another teammate find a file.

For your first code change, pick one small task with a teammate. Follow
[CONTRIBUTING.md](CONTRIBUTING.md) to create a branch, review what changed, build
the project, and open a pull request. Have a teammate help with robot testing
before treating a change as ready to drive.

You do not need to understand everything today. Start with one file and one
question, and we will build from there together.
