# Git Replay

Automated replay of git repositories.

Add an upstream for the project you want to replay.
git add remote upstream <repourl>

Fetch all:
`git fetch --all`

Checkout the branch you wish to replay (e.g. master):
`git checkout -b replaymaster upstream/master`

Checkout the commit you want to start from (optional):
`git checkout <commithash>`

Push the branch you want to replay to.

- Configure the credentials needed to access git.
- Configure the location of your local git repository.
- Configure the branch you want to replay from and replay to.

To start the tool, run `./start.sh` (`start.bat bootRun` on Windows)
Add an application-remote.yml file in src/main/resources containing the remote access means to the repository you want to push to.

If the branches  are  setup correctly, run the init command:
`http://localhost:8080/gitreplay/build/init`

Set the commit you want to start with (usually the first commit)
`http://localhost:8080/gitreplay/build/current-commit/<commithash>`

In your build server add a shell executer at the end of the build:
`curl -X POST http://localhost:8090/gitreplay/build/finished`