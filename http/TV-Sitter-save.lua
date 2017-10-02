return function (connection, req, args)
   dofile("httpserver-header.lc")(connection, 200, 'txt')
   connection:send([===[
   ]===])

   local data = req.getRequestData();
   req.getRequestData = nil
   local filename = "http/" .. data.key
   local payload = sjson.encode(data.value)
   
   local tmpFilename = string.sub(filename, 0, 27) .. '.dnl'
   local bakFilename = string.sub(filename, 0, 27) .. '.bak'
   file.remove(tmpFilename)
   file.open(tmpFilename,'w+')
   file.write(payload)
   file.close()
   file.remove(bakFilename)
   file.rename(filename, bakFilename)
   file.rename(tmpFilename, filename)
end
