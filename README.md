# Item Leak Detector

## The `itemLeak` Command
The `itemLeak` command allows you to test a particular setup for reliability by spawning tons of items with controlled velocities.
#### Power
The command expects two block positions (both inclusive) which represent a volume and a 32-bit integer representing the cooldown interval. All red concrete in the specified area will be simultaneously powered with a 0 game-tick pulse once every cooldown interval.
#### Batches
The command expects two 32-bit integers representing the number of batches and the number of items per batch divided by four. Every time an item is spawned, it is immediately replaced with one batch of items. The size of each batch is multiplied by four to ensure that movement anomalies due to the items id is accounted for. The test will conclude if the number of batches is exhausted
#### Angle and Speed Limits
The command expects two non-negative finite 64-bit floating-point (hereinafter 'double') values representing the minimum and maximum angles, in degrees. Additionally it will also ask for two more non-negative finite double values for the minimum and maximum speed. 0° represents the positive x-axis and 90° represents the positive z-axis. All items spawned by the command will conform to these restrictions.
#### Item Lifetime
Optionally, you can specify an integer between `1` and `6000` (inclusive) representing the maximum lifetime for each spawned item in game-ticks. The default is 6000 (5 minutes), but decreasing this value appropriately is recommended for performance.
#### Modes
You may also specify either `possibility` or `probability` to evaluate different circumstances. In `possibility` mode, the command will immediately terminate when an item despawns. In `probability` mode, the command will continue to run until the number of batches is exhausted. After the command is finished running, it will print estimates of how likely an item will despawn given the command's restrictions. While `probability` mode provides better statistics, keep in mind that it is still only an estimate.
#### Algorithms
Finally, you may also specify the algorithm used to generate the next velocity to test. `random` selects random values according to the `Math.nextDouble()` of the `java.util.Math` class. `uniform` splits the search area into equal increments (specifically, both the angle and speed are incremented by `(max-min)/sqrt(batches*batchSize)`). However, `uniform` only performs well on extremely limited search areas and an extremely large batch count and batch size.
#### Results
Every time an item despawns during command execution, the command will broadcast a message which shows the item's initial position, initial velocity, and despawn location. Clicking the message will copy a command to summon the item with the initial conditions.
#### Usage Notes
- This command only accounts for one item being spawned at one location.
- The command is very heavy on both the server and client. Consider testing on a quiet server.
- At the moment, the command only accounts for the randomness caused by spawning the entity. Consider running tests several times to account for other sources of randomness (e.g. piston block breaking).
#### Command Syntax
- `/itemLeak stop`
- `/itemLeak <x1> <y1> <z1> <x2> <y2> <z2> <cooldown> <batches> <batchSize> <minAngle> <maxAngle> <minSpeed> <maxSpeed> [itemLifetime] [mode] [algorithm]`

## The `itemVelocity` Command
The `itemVelocity` command allows you to control the velocity of spawned items.
#### Arguments
Each of the three arguments expects a double, optionally preceded by a tilde (`~`), or a caret (`^`). A plain double simply sets the value. A double preceded by a tilde adds the specified value to whatever value the item is spawned with. A caret does not change the last setting for the value, effectively ignoring the argument. If no arguments are specified, the previous setting will be printed to the chat.
#### Command Syntax
- `/itemVelocity [<x> <y> <z>]`

## The `power` Command
The `power` command simply powers a red concrete block in the same way that the `itemLeak` command does. This command is provided for convenience of re-creating the circumstance which caused an item to despawn.
#### Arguments
The command expects three integers representing the block position to power.
#### Command Syntax
- `/power <x> <y> <z>`