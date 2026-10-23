# Deploy robot code from VS Code: macOS and Windows

This guide connects this repository's Java code to an FTC robot using a REV
Control Hub. VS Code edits the source; Gradle builds a Robot Controller Android
app (APK); ADB installs it on the Control Hub; Driver Station selects and runs
`TeleOP_v1`. Every code change requires another build and installation.

If your robot uses an Android Robot Controller phone with an Expansion Hub,
connect the computer to that phone instead. The USB-C Control Hub instructions
and wireless address below apply specifically to a Control Hub.

## 1. Use a local computer for deployment

A browser Codespace or a VS Code window connected to Codespaces runs its terminal
on a remote machine. Plugging a USB cable into your laptop does not normally make
the robot available to that terminal. Opening Codespaces in desktop VS Code still
uses the remote machine.

For this guide, clone the repository onto your own computer and open that local
folder in desktop VS Code. Check that the bottom-left remote indicator does not
say Codespaces, SSH, or Dev Container. On Windows, use a local PowerShell terminal
rather than WSL for these instructions.

If you edited in Codespaces, commit and push your intended changes first. In your
local checkout, switch to the same branch and pull it after preserving any local
work. Uncommitted cloud edits are not included in a local build.

In a local terminal, clone once:

```sh
git clone https://github.com/Leogao07/Via_veritas_voltage_26-27.git
cd Via_veritas_voltage_26-27
```

Open that folder with **File > Open Folder** in VS Code. The root must contain
`settings.gradle`, `gradlew`, `gradlew.bat`, `TeamCode`, and `FtcRobotController`.
An existing local checkout can be reused.

## 2. Install and configure the build tools once

Install Git, desktop VS Code, Android Studio, and JDK 21. On Apple Silicon Macs,
choose native Apple Silicon versions where available.

Open the same repository root in Android Studio. Follow the
[Android Studio setup guide](ANDROID-STUDIO.md) to select JDK 21 for Gradle and
install these exact project dependencies through SDK Manager:

- Android SDK Platform 30
- Android SDK Build-Tools 35.0.0 (enable Show Package Details if needed)
- Android SDK Platform-Tools, which supplies `adb`

Accept SDK licenses when prompted and complete Gradle sync while internet access
is available. Keep the repository's pinned SDK and Gradle versions. Note the
**Android SDK Location** shown in SDK Manager. Android Studio may create
`local.properties` with that location; keep this machine-specific file untracked.

VS Code's terminal needs its own Java and SDK configuration. Selecting a Gradle
JDK in Android Studio does not set the Java version in VS Code's terminal.

### macOS: zsh or bash

For a normally installed JDK 21 and the default SDK location:

```sh
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
java -version
adb version
./gradlew --version
```

If Java cannot be found, install JDK 21 or set `JAVA_HOME` to the actual JDK 21
home directory. If your SDK is elsewhere, replace `ANDROID_HOME` with the SDK
Manager location. Java and the Gradle launcher should report version 21.

These exports affect only the current terminal. To reuse them in the default
macOS zsh terminal, add the three export lines to `~/.zshrc` and open a new
terminal. If `./gradlew` reports permission denied, run `chmod +x gradlew`.

### Windows: PowerShell

Replace the example JDK path with your installed JDK 21 directory. The SDK path
below is the usual default; check it against SDK Manager.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:Path = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"
java -version
adb version
.\gradlew.bat --version
```

These settings affect only the current PowerShell session. For persistent setup,
open **Edit environment variables for your account**, set `JAVA_HOME` and
`ANDROID_HOME`, and add `%JAVA_HOME%\bin` and `%ANDROID_HOME%\platform-tools` to
your user `Path`. Restart VS Code afterward. If a system Java entry still takes
priority, check `Get-Command java` and correct the path order.

### Differences at a glance

| Task | macOS | Windows PowerShell |
| --- | --- | --- |
| Gradle wrapper | `./gradlew` | `.\gradlew.bat` |
| Default Android SDK | `~/Library/Android/sdk` | `%LOCALAPPDATA%\Android\Sdk` |
| Set a session variable | `export NAME="value"` | `$env:NAME = 'value'` |
| Locate ADB | `command -v adb` | `Get-Command adb` |
| USB setup | Usually no extra USB driver | An appropriate device USB driver may be needed |

The Java source, hardware names, APK, ADB commands, and Driver Station procedure
are the same on both systems.

## 3. Connect the Control Hub over USB

1. Power the robot through its normal battery and power switch; do not rely on
   the programming cable to power the robot.
2. Connect a data-capable USB cable from the computer to the Control Hub's
   **USB-C port**. Connect to the Control Hub, rather than the Driver Hub or
   Expansion Hub's programming port.
3. In the local VS Code terminal, run:

   ```sh
   adb devices -l
   ```

4. Confirm that the intended controller appears with status `device`. For example:

   ```text
   List of devices attached
   EXAMPLE_SERIAL    device
   ```

   `EXAMPLE_SERIAL` is a placeholder. Your device will have its own serial number.
   An empty list, `offline`, or `unauthorized` does not indicate a ready connection.

If Windows does not recognize the Hub, inspect Device Manager and follow the
manufacturer's USB driver instructions. On either system, try a known data cable
and another USB port before changing software settings. If authorization is
required, approve the debugging prompt on the controller when available; use
REV's device management instructions for a screenless Control Hub.

## 4. Build and install the app

Keep exactly the intended controller connected to ADB for the simple Gradle
installation below; disconnect unrelated phones, emulators, or wireless targets.
Installing updates the Robot Controller application on that device.

Run from the repository root, stopping if a command fails.

macOS:

```sh
./gradlew :TeamCode:assembleDebug
./gradlew :TeamCode:installDebug
```

Windows PowerShell:

```powershell
.\gradlew.bat :TeamCode:assembleDebug
.\gradlew.bat :TeamCode:installDebug
```

The build should report `BUILD SUCCESSFUL`. The APK is:

```text
TeamCode/build/outputs/apk/debug/TeamCode-debug.apk
```

The installation must also succeed. A successful build alone does not put code on
the robot. The Robot Controller app may restart and Driver Station may briefly
disconnect during deployment. Let the controller finish restarting.

If you must keep multiple ADB devices connected, build first and install the APK
explicitly to the intended serial, replacing `ACTUAL_SERIAL` with the value from
`adb devices -l` (same command on both systems):

```sh
adb -s ACTUAL_SERIAL install -r "TeamCode/build/outputs/apk/debug/TeamCode-debug.apk"
```

## 5. Configure the robot and run TeleOp

1. Use a Driver Station compatible with this project's official FTC SDK release
   (currently SDK 12.0.0). Pair/connect it to the intended Robot Controller.
2. In Driver Station's robot configuration screen, create or select the correct
   configuration. Assign the actual connected devices their exact names below,
   select the appropriate device types, save, and activate the configuration.

   | Name | Device |
   | --- | --- |
   | `leftfront` | Left-front drive motor |
   | `leftback` | Left-back drive motor |
   | `rightfront` | Right-front drive motor |
   | `rightback` | Right-back drive motor |
   | `odometry` | goBILDA Pinpoint |
   | `intake` | Intake motor |
   | `plate` | Position servo |
   | `intakesensor` | Intake analog distance sensor |
   | `outtakesensor` | Outtake analog distance sensor |
   | `Flywheel` | Flywheel motor |
   | `elevator` | Elevator motor |
   | `Spoon` | Spoon servo |
   | `Camera_angle` | Camera angle servo |

   Preserve the exact spelling and capitalization, including `Flywheel`, `Spoon`,
   and `Camera_angle`. Match the real ports and wiring; this repository does not
   record every port assignment. The current OpMode does not read either distance sensor, so they
   do not yet trigger automatic actions. It also does not initialize or control
   `Flywheel`, `elevator`, `Spoon`, or `Camera_angle`; their control settings and
   gamepad assignments remain to be specified.
3. Before running, complete the [robot checks](SDK-INTEGRATION.md#before-robot-operation),
   including motor direction, Pinpoint pod configuration, and plate servo limits.
   Support the robot with its drive wheels clear and keep mechanisms clear.
   INIT can initialize hardware, so prepare before pressing it.
4. Assign the gamepads in Driver Station. In the TeleOp list select `TeleOP_v1`,
   press **INIT**, inspect telemetry/errors, and then press **START**.
5. Release gamepad 1's left bumper after starting, then hold it to enable driving.
   Use small stick movements initially. Releasing the bumper stops drive power.
   Centered sticks with the bumper held can still command pose corrections.
6. Test each mechanism separately, then use **STOP** when finished.

Current controls:

| Input | Action |
| --- | --- |
| Gamepad 1 left bumper | Hold to enable PD driving after the startup release |
| Gamepad 1 left stick | Move the virtual translation target |
| Gamepad 1 right-stick X | Change target heading |
| Gamepad 2 Y / X / A / B | Plate targets: 27 / 117 / 207 / 297 degrees |
| Gamepad 2 left / right bumper | Intake forward / reverse; release to stop |

These preset values are code settings, not verified mechanical calibration. Build
success does not establish correct robot behavior. Existing tuning concerns and
robot test status are recorded in [SDK integration notes](SDK-INTEGRATION.md).

## 6. Optional: deploy over Control Hub Wi-Fi

Establish USB deployment first. Download dependencies and complete an initial
build while you have internet access; the robot network may have no internet.

1. Connect the local computer to the Control Hub's Wi-Fi using the team's network
   name and password.
2. Check the address shown by Driver Station's **Program & Manage** information.
   A standard Control Hub normally uses `192.168.43.1`.
3. With that standard address, run locally on either OS:

   ```sh
   adb connect 192.168.43.1:5555
   adb devices -l
   ```

4. Confirm the wireless target has status `device`. If USB is also connected, use
   the explicit install command below to select the wireless target. Build again
   with your OS's `assembleDebug` command after source changes, then install:

   ```sh
   adb -s 192.168.43.1:5555 install -r "TeamCode/build/outputs/apk/debug/TeamCode-debug.apk"
   ```

5. When finished, disconnect the ADB session:

   ```sh
   adb disconnect 192.168.43.1:5555
   ```

Use the actual address if it differs. A cloud Codespace normally cannot reach the
Hub merely because your laptop joined its Wi-Fi. Return to your usual network
when you need internet access again.

## 7. Daily edit and deployment cycle

1. Preserve local changes and synchronize the intended Git branch.
2. Edit `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/teleop/TeleOP_v1.java`
   for behavior, or `Tele_debug_v1.java` in that same `teleop/` directory for tuning
   and diagnostic settings. Both files use the Java package
   `org.firstinspires.ftc.teamcode.teleop`.
3. Build, connect/check the intended ADB device, and install again.
4. Reconnect Driver Station, select the OpMode, and test the change.
5. Record what was built and actually tested. Follow [CONTRIBUTING.md](../CONTRIBUTING.md)
   when committing and opening a pull request.

Pushing to GitHub or passing the GitHub Actions build does not automatically
install anything on the robot.

## 8. Troubleshooting

| Symptom | Next action |
| --- | --- |
| `adb` not found / not recognized | Install Platform-Tools, check `ANDROID_HOME`, add its `platform-tools` directory to `PATH`, and reopen the terminal. |
| Wrong Java version / Gradle cannot start | Check `java -version` and the Gradle version command; point `JAVA_HOME` and the front of `PATH` to JDK 21. |
| SDK location not found | Set `ANDROID_HOME` to SDK Manager's actual path or let Android Studio generate `local.properties`. |
| Missing SDK package / license error | Install the exact packages in section 2 and accept licenses through SDK Manager. |
| No device in `adb devices` | Verify the terminal is local, Hub power, USB-C port, data cable, and Windows driver if applicable. |
| `offline` | Wait for startup; reconnect the cable. If needed run `adb kill-server`, then `adb start-server`, and check again. |
| `unauthorized` | Complete the controller's debugging authorization before installing. |
| More than one device | Select a serial with `adb -s ACTUAL_SERIAL install -r ...` as shown above. |
| Signature / version conflict during install | Check the installed RC app and team's SDK/signing setup; coordinate recovery before uninstalling, which can remove app data. |
| `TeleOP_v1` missing | Confirm installation succeeded on the intended Control Hub and Robot Controller restarted; verify you built the branch containing the OpMode. |
| Hardware lookup error during INIT | Check the active configuration's exact names and device types against section 5. |
| Robot does not drive after START | Check gamepad assignment; release then hold gamepad 1 left bumper, inspect telemetry and Pinpoint feedback. |
| Wi-Fi ADB cannot connect | Check the computer's robot Wi-Fi connection and actual Program & Manage address; try USB again. |

## References and verification

- [FTC: create and run an Android Studio OpMode](https://ftc-docs.firstinspires.org/en/latest/programming_resources/tutorial_specific/android_studio/creating_op_modes/Creating-and-Running-an-Op-Mode-%28Android-Studio%29.html)
- [FTC: manage a Control Hub and wireless ADB](https://ftc-docs.firstinspires.org/en/latest/programming_resources/shared/managing_control_hub/Managing-a-Control-Hub.html)
- [Android: SDK environment variables](https://developer.android.com/tools/variables)
- [Android: ADB and device selection](https://developer.android.com/tools/adb)

This guide documents the deployment procedure. It does not record a successful
macOS/Windows installation or robot test; see the repository's existing
[verification status](../README.md#verification-status) for completed checks.
