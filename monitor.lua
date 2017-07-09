

local connect = dofile("PhilipsConnect.lua")

connect.GetCurrentVolume(function(v) print("callback") end)
connect.PostKey('Digit1')
connect.PostKey('Digit2')
connect.PostKey('Digit3')
connect.PostKey('Digit4')
connect.PostKey('VolumeUp')
connect.PostKey('VolumeDown')
connect.GetCurrentVolume(function(v) print("callback") end)
connect.GetCurrentSource(function(v) print("callback") end)
connect.GetCurrentChannel(function(v) print("callback") end)


connect.GetSources(function(v) sources = v end)

function SaveData(t)
   print("Volume: "..currentVolume)
   print("Source: "..sources[currentSourceId]["name"])
end

function UpdateValues(t)
   print("Updating")
   connect.GetCurrentSource(function(v) currentSourceId = v["id"] end)
   connect.GetCurrentVolume(function(v) currentVolume = v["current"] SaveData() t:start() end)

end



updateTimer = tmr.create()
updateTimer:register(5000,tmr.ALARM_SEMI,UpdateValues)
updateTimer:start()


sntp.sync(nil,
  function(sec, usec, server, info)
    print('sync', sec, usec, server)
  end,
  function(a,b)
   print('failed!'..a)
  end
)

time = rtctime.epoch2cal(rtctime.get()+60*60*2)
print(time["hour"]..":"..time["min"])