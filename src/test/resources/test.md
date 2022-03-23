# Title {.menu}

Menu option
-----------

### Option1 A {#1a .optional}

hola **feo *le* xxx** esto es un texto largo para ver como se adapta al ancho de pantalla y lo divide en varias líneas.

```shell
$ echo Hello Linux
$ echo Hello Mac
```
---

### [Option1](https://xoa.dev) B {.optional}

1. One
2. Two
   1. Two 1: Estos es para ver como se parte la linea dentro de una lista anidada que excede el tamaño.
   2. Two 2
3. Tree

## Menu *option* 2 {.choice}

* One
* `Two`
  * Two 1
  * Two 2
* Three

### Option_2A_ {#2a}

Hello, _my_
[name](https://xoa.dev)
*is* Byte **Bit**.

Other [paragraph][#1a]

![alt text](image.jpg)

### `Variables`

Before 
[APP_ID]

```shell {#generateAppId .play}
$ echo "hello $USER bye"
hello [APP_ID] bye
```

After declaration [APP_ID]

```yaml {"settings.yaml"}
id: [APP_ID]
settings:
    appName: [APP_NAME]
    port: [APP_PORT]
```

[APP_NAME]: {input}  "Application name"
[APP_PORT]: {input}  "Port"
[APP_ID]: {parsed} "Id generado de la aplicación"
