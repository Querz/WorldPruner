# WorldPruner ![](https://user-images.githubusercontent.com/13237524/168894674-c2b8c07e-a4d6-4eb9-ab9d-b906b78dcb70.png)

A simple GUI and command line app to prune chunks from a Minecraft Java Edition world.

> Make sure you have a backup of your world!
>
> We are not liable for data loss!

## Prerequisites

- Java 17

## Usage

There are two ways to use this application: *GUI* mode and *CLI* mode.

### GUI Mode

The GUI mode offers an easy way to tweak the different parameters of the application.

To use the GUI, open the `WorldPruner.jar` file with Java.

You simply need to open your world folder with the file chooser, provide a minimum time, a radius and an
optional [whitelist](#whitelisting-chunks) containing all chunks that should be kept in any case.

![](https://user-images.githubusercontent.com/13237524/168843781-56dcff9b-a29f-4ec6-a191-ea006053f493.png)

### CLI Mode

The CLI mode is meant for server owners, to prune their world without needing to download the world first. This mode
allows to operate the application from the command line.

**Command line arguments:**

| Argument(s)                | Default Value |Description|
|----------------------------|---------------|------------|
| `--world` `-w`             | *Required*    | The path to the world folder |
| `--time` `-t`              | `0 seconds`   | The minimum time a chunk should have to be kept |
| `--radius` `-r`            | `0`           | The radius of additional chunks preserved around matching chunks |
| `--white-list` `-W`        |               | The path to whitelist CSV file |
| `--continue-on-error` `-c` |               | If execution should continue if an error occurs |
| `--help` `-h`              |               | Prints all available commandline options |

**Examples:**

*Prune all chunks that are not older than 1 minute*
```sh
java -jar WorldPruner.jar --world "/path/to/my/world/" --time "1 minute"
```

*Prune all chunks that are not older than 5 minutes, keep a radius of two chunks and continue if an error occurs*
```sh
java -jar WorldPruner.jar --world "/path/to/my/world/" --time "5 minutes" --radius 2 --continue-on-error
```

*Prune all chunks that are not older than 1 day, keep a radius of ten chunks and keep all chunks in the `whitelist.csv`*
```sh
java -jar WorldPruner.jar --world "/path/to/my/world/" --time "1 day" --radius 10 --white-list "/path/to/whitelist.csv"
```

*Print all available commandline options*
```sh
java -jar WorldPruner.jar --help
```

## Whitelisting Chunks

You can create and export a whiltelist from [MCA Selector](https://github.com/Querz/mcaselector).
More information on how to create a whitelist can be found [here](https://github.com/Querz/mcaselector/wiki/Selections#save-and-load-selections).

If you want to create your own whitelist read [this](https://github.com/Querz/mcaselector/wiki/Selections#selection-file-format).
