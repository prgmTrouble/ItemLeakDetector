# Item Leak Detector

## The `itemLeak` Command
The `itemLeak` command allows you to test a particular setup for reliability by spawning tons of items with controlled velocities.
### `power` and `cooldown`
The `power` keyword is for specifying which blocks to power:
- `clear`: Removes all blocks from the list.
- `<block>`: Adds a single block to be powered.
- `<from> <to>`: Adds all red concrete blocks in the specified volume between both block positions.

The `cooldown` keyword is for specifying the frequency at which the specified blocks are powered.

All blocks added to the list are simultaneously powered with a 0 game-tick pulse once every cooldown interval.
### `batches` and `batchSize`
The `batches` keyword is for specifying the total number of batches to spawn until the test is considered complete.

The `batchSize` keyword is for specifying the number of items that will be spawned in a batch, divided by four.

Every time an item is spawned, it is immediately replaced with one batch of items. The size of each batch is multiplied by four to ensure that movement anomalies due to the items' id is accounted for. The test will conclude if the number of batches is exhausted.
### `pos` and `vel`
The `pos` keyword is for specifying the limits for randomly positioned items, relative to the position they were spawned if the random number generator returned zero.

The `vel` keyword is for specifying the limits for randomly moving items, relative to the velocity they were spawned if the random number generator returned zero.

Each of their arguments represents alternating minimum and maximum values for the X, Y, and Z axes. Each argument can also be individually set with their respective keywords.
### `scenario`
The `scenario` keyword is for automatically setting position and velocity limits based on what phenomenon spawned the item.

<table>
    <tbody>
        <tr>
            <td>Scenarios</td>
            <td>Minimum Pos</td>
            <td>Maximum Pos</td>
            <td>Minimum Vel</td>
            <td>Maximum Vel</td>
        </tr>
        <tr>
            <td>
                <p>beehive,boat_drop_boat,boat_drop_materials,cat_morning_gifts,chicken_lay_egg,creeper_drop_head,dolphin,donkey_drop_chest,</p>
                <p>enderman_drop_held_block,entity_detach_leash,eye_of_ender_drop,falling_block_pop,fox_drop,fox_spit,furnace_destroyed,</p>
                <p>horse_drop_inventory,item_frame,item_with_inventory_destroyed,leash_knot,leashed_mob_loaded_without_holder,lectern_drop_book,</p>
                <p>loot_command,minecart_drop_chest,minecart_drop_furnace,minecart_drop_hopper,minecart_drop_minecart,minecart_drop_tnt,</p>
                <p>mob_drop_equipment,mob_drop_inventory,mob_drop_loot,mooshroom_sheared,painting,panda_sneeze,panda_bamboo,pickup_arrow,</p>
                <p>pickup_trident,pig_drop_saddle,piglin_barter,piglin_zombify,player_breaks_shulker_box,raid_hero,raider_drop_banner,</p>
                <p>shear_snow_golem,skeleton_drop_head,strider_drop_saddle,turtle_scute,villager_gather_items,wither_skeleton_drop_head,wither_rose,</p>
                <p>wither_star,zombie_conversion,zombie_drop_head</p>
            </td>
            <td>0,0,0</td>
            <td>0,0,0</td>
            <td>-0.2,0,-0.2</td>
            <td>0.2,0,0.2</td>
        </tr>
        <tr>
            <td>
                <p>barrel_destroyed,brewing_extra_dragons_breath,brewing_stand_destroyed,campfire_cooking,chest_destroyed,dispenser_destroyed</p>
                <p>dropper_destroyed,hopper_destroyed,minecart_destroyed_inventory</p>
            </td>
            <td>-0.75,-0.75,-0.75</td>
            <td>0.75,0.75,0.75</td>
            <td>-INF,-INF,-INF</td>
            <td>INF,INF,INF</td>
        </tr>
        <tr>
            <td>composter_full,jukebox</td>
            <td>0,0,0</td>
            <td>0.7F,0.7F,0.7F</td>
            <td>-0.2,0,-0.2</td>
            <td>0.2,0,0.2</td>
        </tr>
        <tr>
            <td>dispenser_fired,dropper_fired</td>
            <td>0,0,0</td>
            <td>0,0,0</td>
            <td>-INF,-INF,-INF</td>
            <td>INF,INF,INF</td>
        </tr>
        <tr>
            <td>fishing_bobber_catch,test_context</td>
            <td>0,0,0</td>
            <td>0,0,0</td>
            <td>0,0,0</td>
            <td>0,0,0</td>
        </tr>
        <tr>
            <td>hoe_on_rooted_dirt_axis-x</td>
            <td>0,-0.25,-0.25</td>
            <td>0,0.25,0.25</td>
            <td>0,0,-0.1</td>
            <td>0,0.1,0.1</td>
        </tr>
        <tr>
            <td>hoe_on_rooted_dirt_axis-y</td>
            <td>-0.25,0,-0.25</td>
            <td>0.25,0,0.25</td>
            <td>-0.1,0,-0.1</td>
            <td>0.1,0,0.1</td>
        </tr>
        <tr>
            <td>hoe_on_rooted_dirt_axis-z</td>
            <td>-0.25,-0.25,0</td>
            <td>0.25,0.25,0</td>
            <td>-0.1,0,0</td>
            <td>0.1,0.1,0</td>
        </tr>
        <tr>
            <td>piston</td>
            <td>-0.25,-0.25,-0.25</td>
            <td>0.25,0.25,0.25</td>
            <td>-0.2,0,-0.2</td>
            <td>0.2,0,0.2</td>
        </tr>
        <tr>
            <td>player_drop_inventory</td>
            <td>0,0,0</td>
            <td>0,0,0</td>
            <td>-0.5F,0,-0.5F</td>
            <td>0.5F,0,0.5F</td>
        </tr>
        <tr>
            <td>player_drop_item</td>
            <td>0,0,0</td>
            <td>0,0,0</td>
            <td>-0.02F,-0.1F,-0.02F</td>
            <td>0.02F,0.1F,0.02F</td>
        </tr>
        <tr>
            <td>pumpkin_sheared</td>
            <td>0,0,0</td>
            <td>0,0,0</td>
            <td>0,0,0</td>
            <td>0.02,0,0.02</td>
        </tr>
        <tr>
            <td>shear_sheep</td>
            <td>0,0,0</td>
            <td>0.1F,0.05F,0.1F</td>
            <td>-0.2,0,-0.2</td>
            <td>0.2,0,0.2</td>
        </tr>
    </tbody>
</table>

`INF` values are the result of uncapped gaussian calls. In these cases, it is possible that items move significant distances despite travelling through fluids, cobwebs, etc.

Values ending with a `F` indicate a 32-bit floating point representation is used instead of a 64-bit representation. In these cases, it is possible that an item can be spawned with a modifier not possible in vanilla. While the difference is insignificant over short distances, consider manually verifying that a failed position and velocity is actually possible if the item travels several thousand blocks during its lifetime.
### `lifetime`
The `lifetime` keyword is for specifying the amount of game-ticks until an item is considered to be despawned. Any test item which is removed from the world for any reason before it ages past its lifetime is considered a success.
### `mode`
The `mode` keyword is for specifying when the test concludes:
- `possibility`: If a test item despawns, the test will conclude immediately.
- `probability`: The test will not conclude when an item despawns.

After the command is finished running, it will print estimates of how likely an item will despawn given the command's restrictions. While `probability` mode provides significantly better statistics, keep in mind that it is still only an estimate.
### `algorithm`
The `algorithm` keyword is for specifying how test items are generated:
- `random`: Items are generated with random positions and velocities.
- `uniform`: Items are generated incrementally.

Note that `uniform` only performs well on extremely limited search constraints and an extremely large batch count and batch size.
### `deleteDespawns`
The `deleteDespawns` keyword is for specifying whether test items will be automatically deleted if they are considered despawned. This can be turned off so that the user can see the item in its despawn location.
### `stop`
The `stop` keyword immediately stops the test and prints the results.
### Results
Every time an item despawns during the test, the command will broadcast a message which shows the item's initial position, initial velocity, and despawn location. Clicking the message will copy a command to summon the item with the initial conditions.
### Usage Notes
- This command only accounts for one item being spawned at one location.
- The command is very heavy on both the server and client. Consider testing on a quiet server.
## The `itemVelocity` Command
The `itemVelocity` command allows you to control the velocity of spawned items.
### Arguments
Each of the three arguments expects a decimal number, optionally preceded by a tilde (`~`), or a caret (`^`). A plain number simply sets the value. A number preceded by a tilde adds the specified value to whatever value the item is spawned with. A caret does not change the last setting for the value, effectively ignoring the argument. If no arguments are specified, the previous setting will be printed to the chat.
### Command Syntax
- `/itemVelocity [<x> <y> <z>]`
## The `power` Command
The `power` command simply powers a block in the same way that the `itemLeak` command does. This command is provided for convenience of re-creating the circumstance which caused an item to despawn.
### Arguments
The command expects three integers representing the block position to power.
### Command Syntax
- `/power <x> <y> <z>`