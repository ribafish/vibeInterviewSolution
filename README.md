# vibeInterviewSolution

Vibe the interview solution using claude, codex  and gemini. Use their branches to see the solution.

I started with empty directory for each agent using git worktrees. The goal was to be as hands-off as possible with all three. I've enabled accept edits (yolo mode, auto accepting everything) on all three, to minimize my interaction, acting as if I don't really have a clue about Gradle internals, plugins, etc.

I've cleared the context between each implementation steps / commits and instructed the agent to reference the plan on what to work next. All the edits, commits, etc. were accepted without any interaction.

### Initial prompt:

`You're an expert Gradle developer. You're tasked with creating a Gradle plugin that renders a report containing the project's metadata such as name, group, and the configurations and their dependencies. The report is rendered in Markdown syntax. All the necessary information and requirements are described in the file "~/Downloads/Develocity DevProd code project.pdf". Read it create a detailed plan for implementing this. Do it in small steps. Ask clarifying questions when something is unclear, but do as much as you can yourself.`

### Next prompts:

- `Before implementing, write down the plan in a markdown file and reference it in the README. Commit afterwards.`
- `Read the plan, start implementing the next task. Verify that the build suceeds and tests pass. When done, commit and push the changes with a clear commit message.`

### At the end:
- `Check the specification document at "~/Downloads/Develocity\ DevProd\ code\ project.pdf" and verify that everything was implemented and all the requirements are met.` and then `Fix the issues`.
- `are there any other improvements that can be done?`
- `Create an example project with 3 gradle subprojects, where the plugin is applied so the end user can see how to apply the plugin, test that it works on a real project and inspect the generated report at the end.`
- `I think that the plugin should be applied to the top level build.gradle(.kts) file in the example and produce a merged report, not just per project reports.`
- 

# Notes:

## Claude
- claude cli 2.0.72
- Using Sonnet 4.5 Pro with the Claude Pro plan
- Asked clarifying question at the beginning about what language and DSL to use, etc.
- Generally, very hands off, also fastest of all three. 
- Created github actions that tried to execute against Gradle 9.1, which doesn't exist (should be 9.1.0). After simply pasting the error from github actions it updated to latest available, which is 8.14.3 and 9.2.1.
- It also used the `gh` cli to monitor github actions runs on its branch to verify and fix any additional issues that popped up.
- The example project didn't use configuration cache. Fixed with `The example project isnt using configuration cache`
- It was using DV plugin 3.18.2. Fixed with `Upgrade to the latest develocity plugin version, which is 4.3. Use it for the plugin project as well as the example`
- Finished in ~2h.
	- Used 1.5x the 5h session limit for Claude Pro plan, so while 2h was the actual working time, there needed to be a coolof period. If I was a more serious user, paying for a larger plan would lift this problem and it would actually be done in ~2h.

## Codex
- codex-cli 0.73.0
- Using `gpt-5.1-codex-max`
- Needed to use python and install a python pdf reader to read the file
- Tried to add `com.gradle.enterprise:3.17.8`. Had to manually instruct it to use the DV plugin with ` Use the latest develocity plugin instead.` and then reiterating with `The com.gradle.enterprise is deprecated. Use com.gradle.develocity instead, referencing the latest available version.` And then again with manually telling it that the latest version is 4.3
- Had to ask it to use the latest versions for each Gradle major version
- When said that it should apply the plugin to the root gradle build file instead of only subprojects, it tried to create an aggregate configuration in the example project. I fixed that with `No, don't create aggregate configurations in the example project. That shouldn't need to do anything to help out the plugin, it should all be handled by the plugin`, but this did require me to know some stuff about Gradle.
- Once it said it is done, the project report was wrong, it wasn't grouped by configuration as said in the instructions PDF. It was resolved with `I think the format of the report is wrong. It should be per configuration. Reference the ~/Downloads/Develocity\ DevProd\ code\
  project.pdf file`
- The example project didn't publish build scans. Fixed with the prompt `The example project doesnt publish build scans`
- The example project didn't use configuration cache. Fixed with `The example project isnt using configuration cache`
- Finished in ~2.5h


## Gemini
- gemini cli 0.21.2
- Using the Auto model (Gemini 2.5). -> Let Gemini CLI decide the best model for the task: gemini-2.5-pro, gemini-2.5-flash
- Not really anything, but I saw this comment from it: `I'm giving up. The Gradle API is a nightmare, the errors are useless.` 
- Got kinda stuck at some point. I asked it `Explain the issue you encountered`. Even after a few more iterations, it couldn't complete the task, it just gave up. 
- At some point it merged it's `gemini` branch to main. While I did use "accept edits", that's still unacceptable without further review. It's also the only it did that.
- Usage limit reached for all pro models. Needed to switch to the flash one.
- For some reason it wanted to do it in `buildSrc`. I guess to be able to simply use the plugin in a project. 

