package com.grex.philipssmartremote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.widget.TextView;

public class PhilipsConnect extends ITVConnect {

	private String host = null;
	static String port = "1925";
	String _IP = null;
	private JSONObject sources = null;
	private JSONObject channelLists = null;
	SortedMap<String, Channel> channels = new TreeMap<String, Channel>();
	private boolean _IsValid = false;

	private static HttpClient client = new DefaultHttpClient();
	
	private PhilipsConnect(String IP) {
		_IP = IP;
		host = IP + ":" + port;
		_IsValid = true;
	}

	@Override
	public String GetCurrentSource() {
		String URL = "http://"+host+"/1/sources/current";
		String response = getHTTP(URL);
//		String response = "{ \"id\": \"HDMI 1\" }";
		  try {
		      JSONObject jsonObject = new JSONObject(response);
		      if ( jsonObject.has("id") )
		      {
			      String sourceId = jsonObject.getString("id");
			      if (sources.has(sourceId))
			      {
			    	  jsonObject = sources.getJSONObject(sourceId);
			    	  String sourceName = jsonObject.getString("name");
			    	  return sourceName;
			      }
			      else
			    	  return "unknown Source \""+sourceId+"\"";
		      }
		      else
		    	  return null;
		    } catch (Exception e) {
		      e.printStackTrace();
		    };
		    return "error parsing JSON ";
	}

	@Override
	public String[] GetSources() {
		String URL = "http://"+host+"/1/sources";
		String response = getHTTP(URL);
//		String response = "{ \"tv\":{\"name\": \"Watch TV\" },\"sat\":{\"name\": \"Watch SAT\" },\"HDMI1\":{\"name\": \"HDMI 1\" }}";
        String[] result = new String[0];
        List<String> resultList = new LinkedList<String>();
		try {
		      JSONObject jsonObject = new JSONObject(response);
		      sources = jsonObject;
		      Iterator<?> keys = jsonObject.keys();
		      while ( keys.hasNext() ) {
		    	resultList.add(jsonObject.getJSONObject((String) keys.next()).getString("name"));
		      }
		      return resultList.toArray(result);
		    } catch (Exception e) {
		      e.printStackTrace();
		      result = new String[1];
		      result[0] = "ERROR"+e.getMessage();
//		      result[1] = e.getMessage();
		    };
		      return result;
	}

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

	@Override
	public Integer GetCurrentVolume() {
		String URL = "http://"+host+"/1/audio/volume";
		String response = getHTTP(URL);
		try {
		      JSONObject jsonObject = new JSONObject(response);
		      return jsonObject.getInt("current");
	    } catch (Exception e) {
		      e.printStackTrace();
	    };
		return null;
	}

	@Override
	public Integer MaxVolume() {
		// TODO Auto-generated method stub
		return 60;
	}

	@Override
	public void SetVolume(Integer volume) {
		String URL = "http://"+host+"/1/audio/volume";
        JSONObject send = new JSONObject();
		try {
	          send.put("current", Integer.valueOf(volume));
    		  String response = postHTTP(URL, send.toString());
	    } catch (Exception e) {
		      e.printStackTrace();
	    };
	}

	@Override
	public String[] GetChannelLists() {
		String URL = "http://"+host+"/1/channellists";
		String response = getHTTP(URL);
		/*
{		 
	"tv_tv": {
		"name": "Non-radio TV channels",
		"source": "tv"
	},
	"sat_all": {
		"name": "All satellite channels",
		"source": "satellite"
	}
}		 
		 */
//		String response = "";
        String[] result = new String[0];
        List<String> resultList = new LinkedList<String>();
		try {
		      JSONObject jsonObject = new JSONObject(response);
		      channelLists = jsonObject;
		      Iterator<?> keys = jsonObject.keys();
		      while ( keys.hasNext() ) {
		    	resultList.add(jsonObject.getJSONObject((String) keys.next()).getString("name"));
		      }
		      return resultList.toArray(result);
		    } catch (Exception e) {
		      e.printStackTrace();
		      result = new String[1];
		      result[0] = "ERROR"+e.getMessage();
//		      result[1] = e.getMessage();
		    };
		      return result;
	}

	@Override
	public String[] GetChannelList(String listName) {
		
		if (channelLists == null)
			GetChannelLists();
		if (channelLists == null)
			return null;

		String[] res = null;

		try {
			String listID = null;
			// find ID to name
			Iterator<?> keys = channelLists.keys();
			while ( keys.hasNext() ) {
				String currentKey = (String) keys.next();
				if ( channelLists.getJSONObject(currentKey).getString("name").equals(listName))
					listID = currentKey;
			}
	
			String URL = "http://"+host+"/1/channellists/"+listID;
			String response = getHTTP(URL);
			JSONArray jsonArray = new JSONArray(response);
			res = new String[jsonArray.length()];
			for ( int i = 0;i < jsonArray.length(); i++){
				res[i] = jsonArray.getString(i);
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
		};
		return res;
	}
	
	@Override
	public SortedMap<String, Channel> GetChannels() {

		if ( !channels.isEmpty())
			return channels;

		SortedMap<String, Channel> res = new TreeMap<String, Channel>();
		String URL = "http://"+host+"/1/channels";
		String response = getHTTP(URL);
		try {
			JSONObject jsonObject = new JSONObject(response);
			Iterator<?> keys = jsonObject.keys();
			String ID;
			JSONObject Chan;
			while ( keys.hasNext() ) {
				ID = (String)keys.next()+"";
				Chan = jsonObject.getJSONObject(ID);
				res.put(ID,new Channel(Chan.getString("preset") ,Chan.getString("name"),ID));
			}
			channels = res;
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			res.put("xx",new Channel("1" ,"ERROR "+e.getMessage(),"xx"));
		};
		return res;
	}

	@Override
	public Channel GetCurrentChannel() {
		String URL = "http://"+host+"/1/channels/current";
		String response = getHTTP(URL);
		try {
			JSONObject jsonObject = new JSONObject(response);
            String ID = jsonObject.getString("id");
//            ID = SortId(ID);
            SortedMap<String, Channel> channels = GetChannels(); 
            if ( channels.containsKey(ID))
            	return channels.get(ID);
		} catch (Exception e) {
			e.printStackTrace();
		};
		return new Channel();
	}

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

	public String GetLogoID(Channel channel){
		String URL = "http://"+host+"/1/channels/" + channel.ID;
		String response = getHTTP(URL);
		try {
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.has("logoid")) {
				String logoID = jsonObject.getString("logoid");
				return "i"+logoID;
			}
			else return null;
		} catch (Exception e) {
			e.printStackTrace();
		};
		return null;
	}
	
/* keys     key (string): One of the following:
    Standby Back Find RedColour GreenColour YellowColour BlueColour Home VolumeUp VolumeDown Mute Options Dot Digit0 Digit1 Digit2
Digit3 Digit4 Digit5 Digit6 Digit7 Digit8 Digit9 Info CursorUp CursorDown CursorLeft CursorRight Confirm Next Previous 
Adjust WatchTV Viewmode Teletext Subtitle ChannelStepUp ChannelStepDown Source AmbilightOnOff PlayPause Pause FastForward
Stop Rewind Record Online
*/ 
	private void PostKey(String key) {
		String URL = "http://"+host+"/1/input/key";
        JSONObject send = new JSONObject();
		try {
	          send.put("key", key);
    		  String response = postHTTP(URL, send.toString());
	    } catch (Exception e) {
		      e.printStackTrace();
	    };
	}
	
	@Override
	public void PowerOff() {
		PostKey("Standby");
	}

	
////////////////////////////////////////////////////
	
	
	  public static String getHTTP(String URL) {
		    StringBuilder builder = new StringBuilder();
		    HttpGet httpGet = new HttpGet(URL);
		    try {
		      HttpResponse response = client.execute(httpGet);
		      StatusLine statusLine = response.getStatusLine();
		      int statusCode = statusLine.getStatusCode();
		      if (statusCode == 200) {
		        HttpEntity entity = response.getEntity();
		        InputStream content = entity.getContent();
		        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
		        //URL xxx = new URL(URL);
		        //BufferedReader reader = new BufferedReader(new InputStreamReader(xxx.openStream()));
		        
		        String line;
		        while ((line = reader.readLine()) != null) {
		          builder.append(line);
		        }
		      } else {
		        return "error: "+statusLine;
		      }
		    } catch (ClientProtocolException e) {
		      e.printStackTrace();
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		    return builder.toString();
		  }
		 
	  public static String postHTTP(String URL, String body) {
		    StringBuilder builder = new StringBuilder();
		    //HttpClient client = new DefaultHttpClient();
		    HttpPost httpPost = new HttpPost(URL);
		    httpPost.setHeader("content-type", "application/json");
		    try {
				httpPost.setEntity(new StringEntity(body, "UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		    try {
		      HttpResponse response = client.execute(httpPost);
		      StatusLine statusLine = response.getStatusLine();
		      int statusCode = statusLine.getStatusCode();
		      if (statusCode == 200) {
		        HttpEntity entity = response.getEntity();
		        InputStream content = entity.getContent();
		        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
		        String line;
		        while ((line = reader.readLine()) != null) {
		          builder.append(line);
		        }
		      } else {
		        return "error: "+statusLine;
		      }
		    } catch (ClientProtocolException e) {
		      e.printStackTrace();
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		    return builder.toString();
		  }

	@Override
	public String GetDeviceName() {
		// TODO Auto-generated method stub
		String URL = "http://"+host+"/1/system";
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
