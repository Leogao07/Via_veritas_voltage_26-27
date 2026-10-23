# Android Studio team setup

Android Studio and VS Code users can work on the same repository. FTC runs as an
Android application on the robot controller; Gradle builds the shared Java source
into an APK. Each developer configures their own JDK, Android SDK, and Git login.

## 1. Accept repository access

The repository owner invites teammates through GitHub **Settings > Collaborators >
Add people**. Accept the invitation using the GitHub account you will use in Android
Studio. The required role is **collaborator**; appearing in the contributors list
alone does not grant repository access.

## 2. Clone and open the project

1. Install Git and Android Studio with support for Android Gradle Plugin **8.13.2**.
2. From Android Studio's welcome screen, choose **Get from VCS** or **Clone Repository**
   (the label depends on the IDE version), then enter:
   `https://github.com/Leogao07/Via_veritas_voltage_26-27.git`.
3. Authenticate with your invited GitHub account when prompted and clone the project.
4. Open the repository root, containing `settings.gradle`, `gradlew`,
   `FtcRobotController/`, and `TeamCode/`.

If you have already cloned the repository, use **Open** to select that directory.
Use the existing project rather than creating a new Android app or opening only
`TeamCode`. There is no need to copy team files into a separate FTC SDK project.

## 3. Configure the local build environment

1. Open **Settings > Build, Execution, Deployment > Build Tools > Gradle**
   (on macOS, Settings is under the Android Studio menu).
2. Select an installed **JDK 21** as the **Gradle JDK**, or download JDK 21 through
   the IDE. Use the project's Gradle wrapper, which specifies **Gradle 9.1.0**.
3. Open **SDK Manager** and install these packages:

   | SDK Manager section | Package |
   | --- | --- |
   | SDK Platforms | Android SDK Platform 30 (Android 11) |
   | SDK Tools | Android SDK Build-Tools 35.0.0 |
   | SDK Tools | Android SDK Platform-Tools |

   Enable **Show Package Details** if needed to select the exact Build Tools version.
4. Let Android Studio create `local.properties` with this computer's SDK path.
5. Run **Sync Project with Gradle Files** and let dependency downloads finish.
   Keep the checked-in FTC SDK and Gradle versions when the IDE offers upgrades;
   coordinate toolchain changes with the team in a separate pull request.

For terminal builds, also point `JAVA_HOME` to JDK 21 and set `ANDROID_HOME` to your
Android SDK directory, or use the generated `local.properties` for the SDK path.
The terminal's Java selection can differ from the IDE's Gradle JDK.

## 4. Build and deploy

In the IDE's Gradle tool window, run `TeamCode > Tasks > build > assembleDebug`.
If that task is not shown, run the command from the repository root in the terminal:

```sh
# macOS / Linux
./gradlew :TeamCode:assembleDebug
```

```powershell
# Windows PowerShell
.\gradlew.bat :TeamCode:assembleDebug
```

The APK is generated at `TeamCode/build/outputs/apk/debug/TeamCode-debug.apk`.
It includes both the Robot Controller application and the team's OpModes.

To deploy, connect the intended robot controller using the team's USB/ADB procedure,
select the **TeamCode** run configuration and that device, then click **Run**.
Installation updates the Robot Controller app on the selected device. Use a
compatible Driver Station from the official FTC v12.0 release. Configure the
hardware names in the [root README](../README.md#hardware-configuration-names).
After installation, select `TeleOP_v1` in the Driver Station TeleOp list.
Compilation does not verify motor directions, Pinpoint setup, or mechanism limits;
complete the [robot checks](SDK-INTEGRATION.md#before-robot-operation) on the robot.

## 5. Work with teammates

Team Java code lives in `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`.
Use the same package for new team classes. Edit `Tele_debug_v1.java` for tuning and
`TeleOP_v1.java` for the existing TeleOp behavior.

Use Android Studio's Git tools or its terminal for the same branch workflow:

1. Preserve unfinished changes before switching branches. Switch to `main` and
   pull the latest `origin/main` (`git pull --ff-only origin main`).
2. Create a new task branch, such as `alex/intake-controls`.
3. Make the change, build `TeamCode`, and review the diff.
4. Commit only the intended source/documentation changes, then push the task branch.
5. Open a pull request targeting `main`. Include build and robot-test results,
   check the **Android build** status, and ask a teammate to review before merging.
6. Pull the updated `main` before starting the next task.

VS Code users follow the same workflow and use the same Gradle wrapper. Follow
[CONTRIBUTING.md](../CONTRIBUTING.md) for active-branch updates and conflict handling.

## Local files and common setup issues

- `local.properties`, `.idea/`, `.gradle/`, and generated `build/` files are ignored.
  Each teammate keeps their own SDK paths and IDE settings. Do not force-add them.
- The tracked `libs/ftc.debug.keystore` is the official public development key and
  is needed by this build. Keep private team signing credentials out of Git.
- **Repository not found / permission denied:** accept the invitation and check
  which GitHub account the IDE or Git credential manager is using.
- **SDK location not found:** configure SDK Manager and regenerate this machine's
  `local.properties`, or set `ANDROID_HOME`.
- **JDK / Gradle compatibility error:** select JDK 21 for Gradle and use the wrapper;
  check `JAVA_HOME` too if the failure occurs in the terminal.
- **Missing dependency / sync failure:** check network access to Google Maven,
  Maven Central, and Gradle downloads, then retry sync.

## References

- [Android build JDK configuration](https://developer.android.com/build/jdks)
- [Clone a Git repository in JetBrains IDEs](https://www.jetbrains.com/help/idea/set-up-a-git-repository.html)
- [Invite GitHub collaborators](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/repository-access-and-collaboration/inviting-collaborators-to-a-personal-repository)
