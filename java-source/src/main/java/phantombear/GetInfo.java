package phantombear;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.simple.JSONObject;

import twitter4j.FilterQuery;
import twitter4j.GeoLocation;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.io.ByteArrayInputStream; 
import java.io.File; 
import java.io.FileWriter;
import java.io.FileInputStream; 
import java.io.FileNotFoundException; 
import java.io.InputStream; 
import java.io.IOException;
import java.io.UnsupportedEncodingException; 
import java.util.logging.Level; 
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;

public class GetInfo {

	public static void main(String[] args) throws IOException, InterruptedException {
		String[] keywordsArray = {"pretty", "girl","basketball","Trump","gun","marvel"};
		if(args.length > 1){
			keywordsArray = args;
		}
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
    	.setOAuthConsumerKey("uuchxIS2TFhIwAniWsnlq0Pwv")
        .setOAuthConsumerSecret("I9uQ7S2iKRFiFsbW7v1pLc2QETn74QasyVwDZ02jnnXTcsVDYG")
        .setOAuthAccessToken("785668193465401348-FakTCROba7Icmsf87d0MJOA9YyiTnZk")
        .setOAuthAccessTokenSecret("UZpgs53Qd1CNJgQ9OqeCyLbp7b2mgIbpZWrRn1GR68lW1");

		//		Implementing StatusListner
		StatusListener listener = new StatusListener() {
			@Override
	    	public void onStatus(Status status) {
				try{
					//	Get info within english speaking country
					if(status.getGeoLocation() != null && status.getLang().equalsIgnoreCase("en")){
						String createAt = status.getCreatedAt().toString();
						long idStr = status.getId();
						String text = status.getText();
						GeoLocation loc = status.getGeoLocation();
						double latitude = loc.getLatitude();
						double longitude = loc.getLongitude();
						//	Create JSON object
						JSONObject json = new JSONObject();
						//json.put("id_str", String.valueOf(idStr));
						json.put("text", text);
						//json.put("created_at", createAt);
						json.put("lat", String.valueOf(latitude));
						json.put("lon", String.valueOf(longitude));
						String index = json.toString();
						System.out.println(index);
						//	Save as .json file
						saveAsJSON(json);

						//	Upload 
						
						if(text != null){
							String url = "http://search-twitter-1-kf5qeriqw5iu6uasbyv6dmwfbq.us-west-2.es.amazonaws.com/user/profile";
							HttpClient client = HttpClientBuilder.create().build();
							HttpPost put = new HttpPost(url);
							put.setHeader("Content-type", "application/json");
							StringEntity params =new StringEntity(json.toString());
							put.setEntity(params);
							HttpResponse response = client.execute(put);
							System.out.println("Response Code:"+response.getStatusLine().getStatusCode());
	
							BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
							StringBuffer result = new StringBuffer();
							String line = "";
							while ((line = rd.readLine()) != null) {
								result.append(line);
							}
							System.out.println("result:"+result);
						}
						
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
	        }
			
			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
	        }
			
			@Override
	        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
	        }
			
			@Override
	        public void onException(Exception ex){
				ex.printStackTrace();
	        }
			
			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
			}
			@Override
			public void onStallWarning(StallWarning warning) {
				System.out.println("Got stall warning:" + warning);
			}
		};
		//	Start collecting info from twitter
		TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
		twitterStream.addListener(listener);
		
		ArrayList<Long> follow = new ArrayList<Long>();
		ArrayList<String> track = new ArrayList<String>();
		for(String arg : keywordsArray){
			if(isNumericalArgument(arg)){
				for(String id : arg.split(",")){
					follow.add(Long.parseLong(id));
				}
			}else{
				track.addAll(Arrays.asList(arg.split(",")));
			}
		}
		long[] followArray = new long[follow.size()];
		for(int i = 0; i < follow.size(); i++){
			followArray[i] = follow.get(i);
		}
		String[] trackArray = track.toArray(new String[track.size()]);
		double[][] boundingBox= {{-180, -90}, {180, 90}}; /// whole world
		twitterStream.filter(new FilterQuery(0, followArray, trackArray).locations(boundingBox));	
		/*
		 // Filter
	    FilterQuery filtre = new FilterQuery();
	    filtre.track(keywordsArray);
	    twitterStream.filter(filtre);
	    */
	}
	
	private static boolean isNumericalArgument(String argument){
		String args[] = argument.split(",");
		boolean isNumericalArgument = true;
		for(String arg : args){
			try{
				Integer.parseInt(arg);
			} catch(NumberFormatException nfe){
				isNumericalArgument = false;
				break;
			}
		}
		return isNumericalArgument;
	}
		
	private static void saveAsJSON(JSONObject obj)
	{
		// try-with-resources statement based on post comment below :)
		try (FileWriter file = new FileWriter("C:\\Codes\\GWT\\JSON\\twitterStreamData.json", true)) {
			file.write("{ \"create\": { \"_index\" : \"user\", \"_type\" : \"profile\"} }\n");
			file.write(obj.toJSONString() + "\n");
			System.out.println("Successfully Copied JSON Object to File...");
			System.out.println("\nJSON Object: " + obj);
			file.close();
		} catch (IOException e){
			System.out.println("Failed to save files");
		}
	}


}
