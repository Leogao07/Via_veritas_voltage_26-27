# FTC SDK integration

## Upstream source

- Repository: https://github.com/FIRST-Tech-Challenge/FtcRobotController
- Release: https://github.com/FIRST-Tech-Challenge/FtcRobotController/releases/tag/v12.0
- Commit: `e14c2aeb33e84d4ea21697beeea9ac49557be870`
- FTC Maven dependencies: `12.0.0`
- Android Gradle Plugin: `8.13.2`; Gradle wrapper: `9.1.0`
- Build JDK used by this project: 21
- Compile SDK: 30; minimum device API: 24; target API: 28
- Build Tools: 35.0.0

The root build scripts, wrapper, `FtcRobotController`, `TeamCode` scaffolding,
`doc` legal notices/media, and `libs` were copied from the tagged official project. Its license is preserved
in `/LICENSE` and its original README in `FTC-SDK-README.md`. The SDK libraries
are resolved from Maven during Gradle sync; they are not copied into source control.
The public upstream debug keystore and OpMode annotation processor are included.
No additional third-party robotics libraries are needed for the current team code.

Team source migration adds only the package declaration
`org.firstinspires.ftc.teamcode` and moves both files from `src/` into the
standard TeamCode Java directory. Existing tuning and control behavior are retained.

## Build verification

Command: `./gradlew :TeamCode:assembleDebug` with JDK 21 and Android SDK.
Result: **BUILD SUCCESSFUL** on October 23, 2026 (52 tasks executed).
Both team classes compiled against SDK 12.0.0, and the complete APK was generated:
`TeamCode/build/outputs/apk/debug/TeamCode-debug.apk` (approximately 49 MB).
The source migration was also checked byte-for-byte against the previous Git
revision, excluding the added package declaration.

The build reports upstream Java 8 source/target and Gradle deprecation warnings.
Native libraries were packaged without stripping debug symbols; this did not
prevent APK generation. Robot installation and hardware testing were not performed.
The CI workflow has been added but has not yet run on GitHub.

The GitHub Actions workflow runs the same APK build for pushes and pull requests.
A successful build verifies SDK compilation and APK packaging, not robot behavior.

## Before robot operation

- Configure `leftfront`, `leftback`, `rightfront`, `rightback`, `odometry`,
  `intake`, and `plate` with the correct device types in the Robot Controller.
- Verify Pinpoint pod resolution, offsets, and encoder directions after a cold
  power cycle. The team OpMode currently does not set these values explicitly.
- Confirm wheel directions and odometry axes on the actual robot.
- Confirm plate servo model, position mode, travel, PWM range, and mechanical limits.
- Use the intended Robot Controller when installing; installation updates its app.
- Check Driver Station compatibility against the official SDK release.
- Record the build version and actual robot test results in the pull request.

## Existing code issues retained for follow-up

- Target translation and rotation velocities have an extra factor of two in
  `test1.java`, beyond the named speed constants.
- `STOP_ERROR_RAD` is 600 degrees, while wrapped heading errors cannot exceed
  180 degrees; the heading-error stop condition cannot trigger at this value.
- Drivetrain settings do not yet have startup validation.

These are existing behavior and configuration issues, not changes made by the SDK
integration. Resolve and test them before relying on the corresponding protections.
