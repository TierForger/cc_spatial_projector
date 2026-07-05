-- CC: Spatial Projector demo program
-- Installed into CraftOS ROM as /rom/programs/spatial_projector.lua
-- Usage:
--   spatial_projector
--   spatial_projector demo [ttl]
--   spatial_projector clear
--   spatial_projector stats
--   spatial_projector list

local args = { ... }

local function usage()
  print("Usage:")
  print("  spatial_projector [demo] [ttl]")
  print("    ttl = 0 keeps visuals until clear/remove/overwrite")
  print("  spatial_projector clear")
  print("  spatial_projector stats")
  print("  spatial_projector list")
end

local function findProjector()
  local projector = peripheral.find("spatial_projector")
  if projector then return projector end

  print("spatial_projector peripheral not found")
  print("Place a Spatial Projector next to this computer/turtle")
  print("or connect it through a wired modem.")
  print("")
  print("Visible peripherals:")
  local names = peripheral.getNames()
  if #names == 0 then
    print("  <none>")
  else
    for _, name in ipairs(names) do
      print("  " .. name .. " : " .. tostring(peripheral.getType(name)))
    end
  end
  return nil
end

local function call(projector, name, ...)
  local fn = projector[name]
  if type(fn) ~= "function" then
    error("projector method missing: " .. name, 0)
  end

  local ok, result, err = pcall(fn, ...)
  if not ok then
    error(name .. " crashed: " .. tostring(result), 0)
  end
  return result, err
end

local projector = findProjector()
if not projector then return end

local command = args[1] or "demo"
if command == "help" or command == "--help" or command == "-h" then
  usage()
  return
end

if command == "stats" then
  print(textutils.serialize(projector.stats()))
  return
end

if command == "list" then
  local ids = projector.list()
  if not ids or #ids == 0 then
    print("Spatial Projector buffer is empty.")
    return
  end

  print("Spatial Projector objects:")
  for _, id in ipairs(ids) do
    print("  " .. tostring(id))
  end
  return
end

if command == "clear" then
  call(projector, "clear")
  call(projector, "sync")
  print("Spatial Projector visuals cleared.")
  return
end

if command ~= "demo" then
  usage()
  return
end

local limits = projector.getLimits()
local maxTtl = tonumber(limits.maxTtlSeconds) or 600
local maxWidth = tonumber(limits.maxWidth) or 2
local maxMarkerSize = tonumber(limits.maxMarkerSize) or 64

local function capped(value, maxValue)
  if maxValue > 0 and value > maxValue then return maxValue end
  return value
end

local ttl = 0
if args[2] ~= nil then
  ttl = tonumber(args[2])
  if ttl == nil then
    error("ttl must be a number", 0)
  end
end
if ttl < 0 or ttl > maxTtl then
  if maxTtl == 0 then
    error("ttl must be 0 because positive TTL is disabled by config", 0)
  end
  error("ttl must be 0 for persistent, or > 0 and <= " .. tostring(maxTtl) .. " seconds", 0)
end

local stats = projector.stats()
local sx, sy, sz = tonumber(stats.sourceX), tonumber(stats.sourceY), tonumber(stats.sourceZ)
if not sx or not sy or not sz then
  error("projector.stats() did not return sourceX/sourceY/sourceZ", 0)
end

local ox, oy, oz = sx + 0.5, sy + 1.25, sz + 0.5

call(projector, "clear")

-- Projector block reference box.
call(projector, "box", "demo.projector", sx, sy, sz, sx + 1, sy + 1, sz + 1, {
  color = 0xffff00,
  width = capped(0.04, maxWidth),
  ttl = ttl,
})

-- RGB world axes around the projector.
call(projector, "line", "demo.axis.x", ox - 8, oy, oz, ox + 8, oy, oz, {
  color = 0xff3333,
  width = capped(0.07, maxWidth),
  ttl = ttl,
})

call(projector, "line", "demo.axis.y", ox, oy - 3, oz, ox, oy + 10, oz, {
  color = 0x33ff33,
  width = capped(0.07, maxWidth),
  ttl = ttl,
})

call(projector, "line", "demo.axis.z", ox, oy, oz - 8, ox, oy, oz + 8, {
  color = 0x3366ff,
  width = capped(0.07, maxWidth),
  ttl = ttl,
})

-- A simple flight-path-like arc.
local points = {}
for i = 0, 96 do
  local t = i / 96
  points[#points + 1] = {
    ox + t * 32,
    oy + 1.0 + math.sin(t * math.pi) * 12,
    oz + math.sin(t * math.pi * 2) * 3,
  }
end

call(projector, "polyline", "demo.arc", points, {
  color = 0x33aaff,
  width = capped(0.08, maxWidth),
  ttl = ttl,
})

local last = points[#points]
call(projector, "marker", "demo.target", last[1], last[2], last[3], {
  color = 0xff33cc,
  size = capped(1.5, maxMarkerSize),
  ttl = ttl,
})

call(projector, "sync")

local after = projector.stats()
print("Spatial Projector demo sent.")
print("Source: " .. tostring(after.dimension) .. " " .. sx .. " " .. sy .. " " .. sz)
print("Objects: " .. tostring(after.objects) .. ", subscribers: " .. tostring(after.subscribers))
if ttl == 0 then
  print("TTL: persistent until clear/remove/overwrite.")
else
  print("TTL: " .. tostring(ttl) .. " seconds.")
end
print("Wear Spatial Goggles bound to this projector to see the visuals.")
