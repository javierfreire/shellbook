# Custom Setup

Let's generate the configuration of our new super app

## Application Id {#appId}

```shell {.play}
$ cat /proc/sys/kernel/random/uuid
[APP_ID]
```

## Name {#appName}

The app name is [APP_NAME]

## Port {#port}

The port to be used is [APP_PORT]

## Setup file generation

```yaml {"setup.yaml"}
id: [APP_ID]
settings:
    appName: [APP_NAME]
    port: [APP_PORT]
```

[APP_NAME]: {input}  "Application name"
[APP_PORT]: {input}  "Port"
[APP_ID]: {parsed} "Id generado de la aplicación"