

local connect = dofile("PhilipsConnect.lua")

if not PhilipsStatus then
   PhilipsStatus = {}
end


connect.GetCurrentVolume(function(v) print("callback") end)
connect.PostKey('Digit1')
connect.PostKey('Digit2')
connect.PostKey('Digit3')
connect.PostKey('Digit4')
connect.PostKey('VolumeUp')
connect.PostKey('VolumeDown')
connect.GetCurrentVolume(function(v) print("callback") end, function(err) print("error: ",err) end)
connect.GetCurrentSource(function(v) print("callback") end, function(err) print("error: ",err) end)
connect.GetCurrentChannel(function(v) print("callback") end, function(err) print("error: ",err) end)

function LoadStatics()
   if not PhilipsStatus.sources then
      connect.GetSources(function(v) PhilipsStatus.sources = v end, function(err) print("error: ",err) end)
   end
end

function SaveData()
   print("Volume: "..PhilipsStatus.currentVolume)
   print("Source: "..PhilipsStatus.sources[PhilipsStatus.currentSourceId]["name"])
end

function UpdateValues(t)
   print("Updating")
   LoadStatics()
   connect.GetCurrentSource(function(v) PhilipsStatus.currentSourceId = v["id"] end, function(err) print("error reading current source: ",err) end)
   connect.GetCurrentVolume(function(v) PhilipsStatus.currentVolume = v["current"] SaveData() end, function(err) print("error reading volume: ",err) end)
   connect.GetSystemInfo(function(v) PhilipsStatus.Status = "on" print("TV is ON") t:start() end,function(err)  PhilipsStatus.Status = "off" print("TV is OFF") t:start() end)
end



PhilipsStatus.updateTimer = tmr.create()
PhilipsStatus.updateTimer:register(5000,tmr.ALARM_SEMI,UpdateValues)
PhilipsStatus.updateTimer:start()


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
