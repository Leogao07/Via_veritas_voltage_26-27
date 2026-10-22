# Contributing

## Language and scope

Write documentation, code comments, commit messages, issues, and pull requests in English. Keep Java identifiers, SDK API names, and hardware configuration names consistent with the existing project.

This repository contains the team's source files for the 2026-2027 FTC BIOBUZZ season. Do not claim a build or robot test passed unless it was actually completed.

## Invite teammates

The repository owner can invite GitHub users through **Settings > Collaborators > Add people**. Each teammate must accept the invitation before accessing this private repository. No collaborators are invited automatically by this setup.

## Branches and reviews

1. Commit or otherwise preserve unfinished work before switching branches.
2. Update `main` with `git pull --ff-only origin main`.
3. Create a task branch, such as `leo/drive-tuning` or `alex/intake-controls`.
4. Make a focused change and review `git diff` before committing.
5. Push the branch and open a pull request into `main`.
6. Ask a teammate to review the change and its test results before merging.

This is the team's recommended workflow; repository branch protection has not been configured by this setup.

## Synchronize an active branch

Commit local changes first, then run this from your task branch:

```sh
git fetch origin
git merge origin/main
```

If Git reports conflicts, edit the marked files, preserve the required behavior from both changes, and then run `git add` for the resolved files followed by `git commit`. To cancel an unfinished merge, use `git merge --abort`. Avoid force-pushing shared branches.

## Pull request verification

Record the FTC SDK version, build result, hardware configuration, test procedure, and observed result. For tuning changes, include old and new values. Clearly identify anything that has not been tested.

The repository has no standalone build task yet. Compile the files inside the team's complete FTC Android Studio project.

## Git authentication

GitHub browser sign-in, the GitHub connector, and command-line Git authentication are separate. Command-line pushes require a supported GitHub authentication method. Do not store passwords, access tokens, or signing keys in the repository or remote URL.

On the setup Mac, the system `git` launcher displayed an Xcode license prompt, while the following installed Git executable ran successfully. If you encounter the same launcher issue, this function selects that executable for the current terminal session:

```sh
git() { /Applications/Xcode.app/Contents/Developer/usr/bin/git "$@"; }
```

Other computers should use their normal Git installation.
