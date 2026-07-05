# CC: Spatial Projector

ComputerCraft + Create addon for drawing simple world-space overlays from Lua.

Place a **Spatial Projector**, connect it to a ComputerCraft computer, draw lines/paths/boxes/markers, then wear bound **Spatial Goggles** to see them in the world.

## Requirements

- Minecraft `1.21.1`
- NeoForge `21.1.x`
- Java `21`
- CC:Tweaked `1.120.0+`
- Create `6.0.10+` for Minecraft `1.21.1`
- Curios API `9.x` optional

## What it adds

- **Spatial Projector** block: ComputerCraft peripheral named `spatial_projector`.
- **Spatial Goggles** item: lets players see visuals from one bound projector.
- Lua drawing API for:
  - lines
  - polylines
  - boxes
  - markers

Visuals are overlays only. They do not place blocks, spawn entities, or affect collisions.

## How to use

1. Place a **Spatial Projector**.
2. Put a ComputerCraft computer/turtle next to it, or connect it with a wired modem.
3. Right-click the projector with **Spatial Goggles** to bind them.
4. Wear the bound goggles.
5. Run the built-in demo:

```lua
spatial_projector
```

Useful CraftOS commands:

```lua
spatial_projector          -- demo
spatial_projector demo 30  -- demo with 30 second TTL
spatial_projector clear    -- clear all visuals
spatial_projector stats    -- show projector stats
spatial_projector list     -- list object IDs in the buffer
```

## Goggles

| Action | Result |
| --- | --- |
| Right-click a projector with goggles | Bind goggles to that projector |
| Shift + right-click with goggles | Unbind goggles |
| Wear bound goggles | See that projector's visuals |

If Curios is installed, goggles can also work from a supported head/curio slot. Without Curios, use them as a normal helmet.

## Crafting

### Spatial Projector

```text
create:iron_sheet     computercraft:wired_modem     create:iron_sheet
create:electron_tube  minecraft:echo_shard          minecraft:spyglass
create:iron_sheet     create:precision_mechanism    create:iron_sheet
```

### Spatial Goggles

```text
empty                  minecraft:string              empty
minecraft:ender_pearl  minecraft:glow_ink_sac        minecraft:ender_pearl
empty                  empty                         empty
```

## Lua API

Find the peripheral:

```lua
local projector = peripheral.find("spatial_projector")
if not projector then error("spatial_projector not found") end
```

Draw something:

```lua
projector.clear()

projector.line("axis.x", 0, 64, 0, 16, 64, 0, {
  color = 0xff3333,
  width = 0.07,
})

projector.marker("target", 16, 64, 0, {
  color = 0xff33cc,
  size = 1.0,
})

projector.sync()
```

Available methods:

```lua
projector.clear()
projector.remove(id)
projector.list()

projector.line(id, x1, y1, z1, x2, y2, z2, options)
projector.polyline(id, points, options)
projector.box(id, x1, y1, z1, x2, y2, z2, options)
projector.marker(id, x, y, z, options)

projector.stats()
projector.getLimits()
projector.sync()
```

### Object IDs

Every visual has a string ID inside the projector buffer.

```lua
projector.marker("target", 10, 64, -20)
projector.marker("target", 12, 64, -18) -- overwrites the same visual
projector.remove("target")
```

Object IDs are local to one projector. They are not global world IDs.

The internal projector ID is not available through Lua and cannot be changed by Lua programs.

### Polyline points

`polyline` uses a dense array of `{x, y, z}` points:

```lua
projector.polyline("path", {
  {0, 64, 0},
  {8, 70, 8},
  {16, 64, 16},
})
```

### Options

All drawing methods accept an optional options table:

```lua
{
  color = 0x33ff88,
  width = 0.0625,
  ttl = 0,
  size = 0.35,
}
```

| Option | Used by | Meaning |
| --- | --- | --- |
| `color` | all visuals | RGB integer, for example `0xff0000` |
| `width` | line, polyline, box | line width |
| `ttl` | all visuals | seconds before auto-removal; `0` means persistent |
| `size` | marker | marker size |

By default, `ttl = 0`, so visuals stay until `clear()`, `remove(id)`, overwrite, or chunk/world unload cleanup.

## Config

The config file is created at:

```text
config/cc-spatial-projector-common.toml
```

Important defaults:

```toml
[limits]
maxObjectsPerProjector = 1024
maxPointsPerPolyline = 2048
maxTtlSeconds = 600
maxObjectIdLength = 96
maxWidth = 2.0
maxMarkerSize = 64.0

[defaults]
defaultColor = 3407752 # 0x33ff88
defaultWidth = 0.0625
defaultTtlSeconds = 0.0
defaultMarkerSize = 0.35
```

Restart the game/server after changing the config.

## Notes

- Only players wearing goggles bound to a projector receive its visuals.
- Coordinates are normal Minecraft world coordinates.
- Call `sync()` after a batch of drawing changes.
- `list()` returns only object IDs, not full geometry data.
- This build targets NeoForge for Minecraft `1.21.1`.

## License

MIT.
