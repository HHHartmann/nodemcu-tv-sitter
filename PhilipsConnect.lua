if not http then
   print("using HTTP mock")
   http = {}
   function http.post(a,b,c,d)
      print("POST",a,c)
      tmr.alarm(0,1000,0,function()d(200, "", "") end)
   end
   function http.get(a,c,d)
      print("GET",a)
      tmr.alarm(0,1000,0,function()d(200, '{"a":"b"}', "") end)
   end
end
if not sjson then
   sjson = cjson
end

local connect = {}
local IP = dofile("configuration.lua").TVIP
print (IP)
local port = "1925"
local host = IP .. ":" .. port



local queue = {first = 0, last = -1}
local running = false

local function enqueue(value)
   local last = queue.last + 1
   queue.last = last
   queue[last] = value
end

local function dequeue()
   local first = queue.first
   if first > queue.last then return nil end
   local value = queue[first]
   queue[first] = nil        -- to allow garbage collection
   queue.first = first + 1
   return value
end
  
local function startNext()
   print("startNext ",running)
   if not running then
      local nextTask = dequeue()
      if nextTask then
         running = true
         nextTask()
      end
   end
end
   
local function isOkResult(statusCode)
   local isOk = statusCode >= 200 and statusCode < 300
   if not isOk then
      print("received statusCode "..statusCode)
   end
   return isOk
end
	
local function readGetResult(statusCode, body, action, errorCallback)
   print("reading get result")
   local result
   if isOkResult(statusCode) then
      print("decoding: "..body)
      body = string.gsub(body, "\r", "")
      body = string.gsub(body, "\n", "")
      result = sjson.decode(body)
      if action then action(result) end
   else
      if errorCallback then errorCallback(statusCode) end
   end
   running = false
   startNext()
end
   
local function readPostResult(statusCode, body, action, errorCallback)
   print("reading post result")
   local result
   if isOkResult(statusCode) then
      if action then action(result) end
   else
      if errorCallback then errorCallback(statusCode) end
   end
   running = false
   startNext()
end
   
-- callback gets the returned body as lua array or nil
local function postHTTP(URL, body, callback, errorCallback)
   enqueue( function() print("sending: "..body); http.post(URL, 'Content-Type: application/json\r\n',
      body, function(code, body, headers) readPostResult(code, body, callback, errorCallback) end) end)
   startNext()
end

-- callback gets the returned body as lua array or nil
local function getHTTP(URL, callback, errorCallback)
   enqueue( function() http.get(URL, 'Content-Type: application/json\r\n',
      function(code, body, headers) readGetResult(code, body, callback, errorCallback) end) end)
   startNext()
end






----------------------------------------------------------------







function connect.GetCurrentSource(callback, errorCallback)
-- response = "{ \"id\": \"HDMI 1\" }";
   local URL = "http://"..host.."/1/sources/current"
   getHTTP(URL, callback, errorCallback)
end

function connect.GetSources(callback, errorCallback)
   local URL = "http://"..host.."/1/sources";
--		String response = "{ \"tv\":{\"name\": \"Watch TV\" },\"sat\":{\"name\": \"Watch SAT\" },\"HDMI1\":{\"name\": \"HDMI 1\" }}";
   getHTTP(URL, callback, errorCallback)
end

--[[

	@Override
	public void SetSource(String sourceName) {
		String URL = "http://"+host+"/1/sources/current";
        JSONObject send = new JSONObject();
		try {
		      Iterator<?> keys = sources.keys();
		      while ( keys.hasNext() ) {
		    	  String key = (String)keys.next();
		    	if ( sources.getJSONObject(key).getString("name").equals(sourceName))
		    		{
		    		 	send.put("id", key);
		    		 	String response = postHTTP(URL, send.toString());
		    		}
		      }
	    } catch (Exception e) {
		      e.printStackTrace();
	    };
	}
--]]

function connect.GetCurrentVolume(callback, errorCallback)
-- response = "{ \"current\": 12 }";
   local URL = "http://"..host.."/1/audio/volume"
   getHTTP(URL, callback, errorCallback)
end

--[[
	@Override
	public Integer MaxVolume() {
		// TODO Auto-generated method stub
		return 60;
	}
--]]

function connect.SetVolume(volume, callback, errorCallback)
-- response = "{ \"current\": 12 }";
   local URL = "http://"..host.."/1/audio/volume"
   postHTTP(URL, '{"current": '..volume..'}', callback, errorCallback)
end

function connect.GetCurrentChannel(callback, errorCallback)
-- response = "{ \"id\": \"sender id\" }";
   local URL = "http://"..host.."/1/channels/current"
   getHTTP(URL, callback, errorCallback)
end


--[[
	@Override
	public void SetChannel(Channel channel) {
		String URL = "http://"+host+"/1/channels/current";
        JSONObject send = new JSONObject();
		try {
	          send.put("id", channel.ID);
    		  String response = postHTTP(URL, send.toString());
	    } catch (Exception e) {
		      e.printStackTrace();
	    };
	}
--]]
	
--[[ keys     key (string): One of the following:
    Standby Back Find RedColour GreenColour YellowColour BlueColour Home VolumeUp VolumeDown Mute Options Dot Digit0 Digit1 Digit2
Digit3 Digit4 Digit5 Digit6 Digit7 Digit8 Digit9 Info CursorUp CursorDown CursorLeft CursorRight Confirm Next Previous 
Adjust WatchTV Viewmode Teletext Subtitle ChannelStepUp ChannelStepDown Source AmbilightOnOff PlayPause Pause FastForward
Stop Rewind Record Online
]]
function connect.PostKey(key, callback, errorCallback)
   local URL = "http://"..host.."/1/input/key";
   postHTTP(URL, '{"key": "'..key..'"}', callback, errorCallback)
end
	
function connect.PowerOff(callback, errorCallback)
   PostKey("Standby", callback, errorCallback)
end

function connect.GetSystemInfo(key, callback, errorCallback)
   local URL = "http://"..host.."/1/system";
   getHTTP(URL, callback, errorCallback)
end

--[[
	function ()
		local URL = "http://"+host+"/1/system";
		String response = getHTTP(URL);
		try {
		      JSONObject jsonObject = new JSONObject(response);
		      return jsonObject.getString("name")+" "+jsonObject.getString("model");
	    } catch (Exception e) {
		      e.printStackTrace();
	    };
		return null;
	}

	public static String GetDeviceName(String IP) {
		// TODO Auto-generated method stub
		String URL = "http://"+IP+":"+port+"/1/system";
		String response = getHTTP(URL);
		try {
		      JSONObject jsonObject = new JSONObject(response);
		      return jsonObject.getString("name")+" "+jsonObject.getString("model");
	    } catch (Exception e) {
		      e.printStackTrace();
	    };
		return null;
	}

	public static PhilipsConnect CreatePhilipsConnect(String IP) {
		if (GetDeviceName(IP) != null)
			return new PhilipsConnect(IP);
		else
			return null;
	}

	@Override
	public boolean IsValid() {
		return _IsValid;
	}

	@Override
	public String IP() {
		return _IP;
	}
	 
}
--]]



return connect

