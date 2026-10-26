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

## Connect VS Code to GitHub and make your first commit

I am more familiar with VS Code; if you use another development environment,
please search Google or ask an AI assistant for help.

Already installed VS Code and received a collaborator invitation? Follow these
steps on your own computer. Setup steps 1–4 normally only need to be done once;
steps 5–8 are the workflow for each new task.

### 1. Understand what the tools do

| Term | Meaning |
| --- | --- |
| VS Code | The editor where you read, change, and save files. |
| Git | The tool on your computer that records versions of your files. |
| GitHub | The website where the team shares its Git repository. |
| Repository / repo | This project's files and their Git history. |
| Clone | Download a working copy of the repository, including its history. |
| Branch | A separate line of work for a task, such as improving an Auto comment. |
| Stage / `git add` | Choose which saved changes to include in the next commit. |
| Commit | Record the staged changes in your local Git history. |
| Push | Upload your local commits to GitHub. |
| Pull request / PR | Propose merging your branch into the team's `main` branch. |

**Saving a file, committing, and pushing are three separate actions.** Saving
updates your working file; committing records a local version; pushing shares
the commits on GitHub. A push to your task branch does not merge it into `main`.

### 2. Accept the invitation and install Git

1. Sign in to GitHub with the account that received the collaborator invitation.
2. Open the invitation from your email or GitHub notifications and accept it.
3. Visit the [team repository](https://github.com/Leogao07/Via_veritas_voltage_26-27).
   Confirm that you can see its files while signed in with that account.
4. Install [Git](https://git-scm.com/downloads) if it is not already installed.
   On Windows, allow Git to be used from the command line during installation.
5. Restart VS Code, then select **Terminal > New Terminal**.
6. In the terminal, run:

   ```sh
   git --version
   ```

A version number means the terminal can find Git. If the command is not found,
finish installing Git and restart VS Code. Windows PowerShell, macOS Terminal,
and the VS Code terminal can all run the Git commands below.

### 3. Set your commit name and email

Run these commands once, replacing the example name and email with your own:

```sh
git config --global user.name "Your Name"
git config --global user.email "your-email@example.com"
```

Use an email verified on your GitHub account, or the exact GitHub-provided
`noreply` email shown under **GitHub Settings > Emails**. Your name can be your
real name; it does not have to match your GitHub username. `--global` applies
these settings to your user account on this computer.

Check them with:

```sh
git config --global user.name
git config --global user.email
```

These settings identify the author of a commit. **They do not sign you in to
GitHub or grant access to the repository.**

### 4. Clone the repository and open it in VS Code

1. Press **Ctrl + Shift + P** on Windows/Linux or **Cmd + Shift + P** on macOS
   to open VS Code's Command Palette.
2. Type and select **Git: Clone**.
3. Paste this repository URL:

   ```text
   https://github.com/Leogao07/Via_veritas_voltage_26-27.git
   ```

4. Choose a parent folder on your computer where you want to keep the project.
   Git creates the `Via_veritas_voltage_26-27` folder inside it.
5. If prompted to authenticate, follow the browser sign-in and authorization
   steps using the GitHub account that accepted the invitation.
6. When cloning finishes, click **Open** to open the cloned folder.

You can also clone from a terminal opened in your chosen parent folder:

```sh
git clone https://github.com/Leogao07/Via_veritas_voltage_26-27.git
cd Via_veritas_voltage_26-27
```

Then use **File > Open Folder** in VS Code to open that folder. Choose either
the Command Palette method or the terminal method; one clone is enough. If you
already have a local clone, open that existing folder instead.

Open the repository root containing `TeamCode/`, `FtcRobotController/`,
`settings.gradle`, and `notefornew.md`. In **Terminal > New Terminal**, run:

```sh
git status
git remote -v
```

`git status` should report your current branch, normally `main`, and initially
say `nothing to commit, working tree clean`. In `git remote -v`, `origin` should
point to the team repository URL above. Cloning sets up `origin` automatically.
Downloading a ZIP gives you files without this Git connection or history; use
a clone for collaboration.

GitHub HTTPS Git operations do not accept your GitHub account password. Use
VS Code's or your Git credential manager's browser sign-in when available. If
your credential flow asks for a token, follow GitHub's
[HTTPS authentication guidance](https://docs.github.com/en/get-started/git-basics/why-is-git-always-asking-for-my-credentials).
Keep tokens out of project files and repository URLs. Signing in to the GitHub
website or a chat connector does not necessarily sign in your terminal's Git.

### 5. Start a task on your own branch

Before editing, check that `git status` shows a clean working tree. If you have
unfinished changes, preserve them on their current task branch before switching.
Then run:

```sh
git switch main
git pull --ff-only origin main
git switch -c alex/auto-comment
```

Replace `alex/auto-comment` with your own name and a short task description,
and use that same branch name in the later push command. Each new task should
get a fresh branch name.

- `git switch main` selects the team's main branch in your local clone.
- `git pull --ff-only origin main` downloads and applies the latest team changes.
  It stops if your local history needs a more involved merge.
- `git switch -c alex/auto-comment` creates your task branch and switches to it.

If a command reports an error, resolve it before continuing. When returning to
an existing task branch, use `git switch alex/auto-comment` without `-c`.

### 6. Edit, review, stage, and commit

For a first contribution, improve one confusing comment in
[auto_V1.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/auto/auto_V1.java).
Keep documentation, code comments, and commit messages in English, as described
in [CONTRIBUTING.md](CONTRIBUTING.md).

1. Make your change and save it with **Ctrl + S** or **Cmd + S**.
2. Check which files changed and review their differences:

   ```sh
   git status
   git diff
   ```

3. Stage the file you intend to include:

   ```sh
   git add TeamCode/src/main/java/org/firstinspires/ftc/teamcode/auto/auto_V1.java
   ```

   Use the path of the file you actually changed. For example, if you changed
   this guide, use `git add notefornew.md` instead.

4. Review exactly what will be committed:

   ```sh
   git diff --cached
   ```

5. Create the local commit with a short description of the actual change:

   ```sh
   git commit -m "Clarify autonomous startup comment"
   git status
   ```

**The commit command is `git commit -m "Describe your change"`.** The `-m`
provides the commit message. It records the content you staged with `git add`.
If you edit a file again after staging, save and stage it again to include the
new edit in this commit.

`git add .` stages changes under the terminal's current directory, including
new files and deletions, except ignored files. Use it only after reviewing all
those changes; selecting specific files is easier to check as a beginner.
Machine-specific files and generated build output are covered by `.gitignore`.

For code changes, follow the project's
[build and verification instructions](CONTRIBUTING.md#pull-request-verification)
and record what you actually tested. Commit and push alone do not build the app
or install code on the robot. Robot deployment is covered in the
[VS Code deployment guide](docs/VSCODE-ROBOT-DEPLOYMENT.md).

### 7. Push your branch to GitHub

For the first push of this branch, run:

```sh
git push -u origin alex/auto-comment
```

Use the exact branch name you created in step 5. `origin` is the team repository,
and `-u` remembers the remote branch for later pushes and pulls. Complete any
GitHub authentication prompt using your invited account.

After more edits on this same branch, repeat the save, review, stage, and commit
steps, then upload the new commits with:

```sh
git push
```

Visit GitHub, select your branch in the repository's branch selector, and check
that your commit and changed files are visible there.

### 8. Open a pull request and start the next task

1. Open the repository on GitHub after pushing your branch.
2. Click **Compare & pull request** if GitHub shows that banner. Otherwise, open
   **Pull requests > New pull request**.
3. Set **base** to `main` and **compare** to your task branch, such as
   `alex/auto-comment`.
4. Check the diff and fill in the PR template in English. Explain what changed
   and include build and robot-test results where relevant. Clearly identify
   checks you have not performed.
5. Create the PR and ask a teammate to review it. Check any automated build
   results before merging according to the team workflow.

If the reviewer requests changes, edit on the same task branch, commit, and
push again. Those commits automatically update the existing PR.

Once the PR is merged, check that your working tree is clean, then prepare for
the next task:

```sh
git switch main
git pull --ff-only origin main
git switch -c alex/next-task
```

Choose a fresh, descriptive name instead of `alex/next-task`.

### Keep an unfinished task up to date

While you are working, teammates may merge changes into `main`. First commit
your own changes. While still on your task branch, bring in the updated `main`:

```sh
git fetch origin
git merge origin/main
```

If the merge succeeds, check the result, run the relevant checks, and push your
branch. If Git reports conflicts, work with a teammate to edit the marked
sections, keep the required behavior, and remove the conflict markers. Stage
the resolved files and run `git commit` to complete the merge. To cancel that
unfinished merge and return to the state before it, use `git merge --abort`.
See [CONTRIBUTING.md](CONTRIBUTING.md#synchronize-an-active-branch) for the shared
workflow.

### Use VS Code's buttons for the same workflow

You can also do the edit-to-push steps in VS Code's **Source Control** view:

1. Save your files, then open Source Control from the left sidebar.
2. Click each changed file to review its diff.
3. Click **+** beside the intended files to stage them.
4. Enter an English commit message and click **Commit**.
5. Use **Publish Branch** for the first upload, or **Push** from the Source
   Control menu for a branch that already has a remote branch.

Check the current branch in the bottom-left status bar before committing.
The buttons perform the same Git actions as the terminal commands; you do not
need to commit the same change twice.

### Common errors

| Message or symptom | What to check |
| --- | --- |
| `git` is not recognized / command not found | Install Git, allow command-line access, and restart VS Code. |
| `not a git repository` | Open the cloned repository root and create a terminal there. A downloaded ZIP is not a clone. |
| `Repository not found` / permission denied | Confirm the URL, accept the invitation, and check which GitHub account your Git credentials use. |
| `Author identity unknown` | Set `user.name` and `user.email` as shown in step 3. |
| Password authentication fails | Use browser sign-in through the credential manager or GitHub's supported token flow; a GitHub account password will not work for HTTPS Git. |
| `nothing to commit, working tree clean` | Save your edit and check that it differs from the last commit in this clone. |
| Changes appear but are not staged | Run `git add` for the intended files, then inspect `git diff --cached`. |
| The current branch has no upstream branch | For its first upload, use `git push -u origin` followed by your current branch name. |
| Push rejected / `non-fast-forward` | The remote branch may have commits you do not have. Fetch with `git fetch origin` and ask a teammate to help integrate them before pushing; keep the remote commits. |
| `git pull --ff-only` cannot fast-forward | Your local and remote histories have diverged. Keep your work and ask a teammate to help reconcile the histories. |

The everyday sequence is: **save → review → stage → commit → push → pull
request**. A commit stays on your computer until you push it.

Official references: [VS Code repositories and remotes](https://code.visualstudio.com/docs/sourcecontrol/repos-remotes),
[GitHub collaborator invitations](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/repository-access-and-collaboration/inviting-collaborators-to-a-personal-repository),
and [GitHub Flow](https://docs.github.com/en/get-started/using-github/github-flow).

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
