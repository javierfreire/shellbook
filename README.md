# Shellbook

`Shellbook` is a command-line tool that allows playing a `Markdown` document, executing the shell commands that it contains,
and interacting with it. The same document that you can see statically, with `shellbook` can be reproduced on the command line,
allowing you to generate files and execute commands directly.

[![generate setup file example](https://asciinema.org/a/ogQZ33Skpim867EmlwoVj4HtY.svg)](https://asciinema.org/a/ogQZ33Skpim867EmlwoVj4HtY)

> You can see the [source code](https://raw.githubusercontent.com/javierfreire/shellbook/main/examples/setup.md)

This can be useful to:

* Onboarding 
* Learning
* Setup

## Installation

```shell
  $ brew tap javierfreire/apps

  $ brew install shellbook
```

## Usage

```shell
  $ shellbook https://raw.githubusercontent.com/javierfreire/shellbook/main/examples/setup.md
```

```shell
  $ shellbook folder/onboarding.md#linux
```

## How to start to develop?

You can follow [the onboarding manual](examples/onboarding.md)

## Features

### Execute shell commands

```markdown
    ```shell {.play}
    $ echo hola

    $ cat debug.log
    ```
```

### Build a menu

```markdown
    # Choice {.menu}
    ## Option 1
    ...
    
    ## Option 2
    ...
```

### Choice one option

```markdown
    # Choice {.choice}
    ## Option 1
    ...
    
    ## Option 2
    ...
```

### Optional

```markdown
    ## Any title {.optional}
    ...
```

### Save to file

```markdown
    ```yaml {"file1.yml"}
    property:
        nested: hello
    ```
```

### Variables

You can declare variables as link definitions. 

If the variable destination is `{input}` then the user will be prompted.

```markdown
    The app name is [APP_NAME]
    ...    
    [APP_NAME]: {input}  "Application name"
```

If the variable destination is `{parsed}` then it can be extracted from a command result.

```markdown
    ```shell
    $ cat /proc/sys/kernel/random/uuid
    [APP_ID]
    ```
    ...
    The app id is [APP_ID]
    ...
    [APP_ID]: {parsed}  "Application name"
```

## TODO

- [ ] output file option with the document played and the command results
- [ ] yaml metainfo with url to upload the output report to github, stackoverflow, ... in case of error, success, ...
- [ ] Improve documentation
- [ ] Append code fragments to files
- [ ] Follow a link
