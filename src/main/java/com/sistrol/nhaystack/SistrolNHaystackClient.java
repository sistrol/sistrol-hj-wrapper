package com.sistrol.nhaystack;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.projecthaystack.auth.AuthClientContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.StringJoiner;

class SistrolNHaystackClient {
	private final AuthClientContext authClientContext;
	private final HttpClient httpClient;
	private final String baseUri;

	public SistrolNHaystackClient(String uri, String user, String password) {
		this.baseUri = uri + (uri.endsWith("/") ? "" : "/" ) + "haystack";
		String authUri = baseUri+"/about";
		this.authClientContext = new AuthClientContext(authUri, user, password);
		this.authClientContext.open();
		this.httpClient = HttpClient.newHttpClient();
	}

	public String getAuthorizationToken() {
		return (String) this.authClientContext.headers.get("Authorization");
	}

	public JSONObject read(String filter) throws URISyntaxException, IOException, InterruptedException {
		return read(filter, -1);
	}
	public JSONObject read(String filter, int limit) throws URISyntaxException, IOException, InterruptedException {
		// Let's build request body which must be in ZINC format, that is a CSV actually where the first line is 'ver: "3.0"'
		StringJoiner joiner = new StringJoiner(System.lineSeparator());
		joiner.add("ver: \"3.0\"");
		joiner.add("filter,limit");
		joiner.add(String.format("\"%s\",%s",
				filter.replace("\"", "\\\""),  // We should escape double quotes as request will fail otherwise
				limit > 0 ? Integer.toString(limit) : "N")); // By default, there's a limit to the number of returned rows. Passing 0 (or lower) to this method will mean unlimited, that must be specified by putting 'N' in the request body

		URI requestUri = new URI(this.baseUri+"/read");
		HttpRequest request = HttpRequest.newBuilder(requestUri)
				.POST(HttpRequest.BodyPublishers.ofString(joiner.toString()))
				.header("Authorization", this.getAuthorizationToken())
				.header("Content-Type", "text/zinc")
				.header("Accept", "application/json")
				.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() == 200) {
			JSONObject jsonObject = (JSONObject) JSONValue.parse(response.body());
			return jsonObject;
		} else {
			// Handle error properly by building a JSONObject that holds status code and returned failure cause
			return null;
		}
	}

	public JSONObject hisRead(String id, String date) throws URISyntaxException, IOException, InterruptedException {
		return hisRead(id, date, null);
	}

	// In this example startDate and endDate are expected to be date string matching the pattern yyyy-MM-dd
	// endDate could be null, then it would be ignored
	public JSONObject hisRead(String id, String startDate, String endDate) throws URISyntaxException, IOException, InterruptedException {
		StringJoiner joiner = new StringJoiner(System.lineSeparator());
		joiner.add("ver: \"3.0\"");
		joiner.add("id,range");
		joiner.add("@"+id+",\""+startDate+(endDate!=null ? ","+endDate+"\"" : "\""));

		URI requestUri = new URI(this.baseUri+"/hisRead");
		HttpRequest request = HttpRequest.newBuilder(requestUri)
				.POST(HttpRequest.BodyPublishers.ofString(joiner.toString()))
				.header("Authorization", this.getAuthorizationToken())
				.header("Content-Type", "text/zinc")
				.header("Accept", "application/json")
				.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() == 200) {
			JSONObject jsonObject = (JSONObject) JSONValue.parse(response.body());
			return jsonObject;
		} else {
			// Handle error properly by building a JSONObject that holds status code and returned failure cause
		}

		return null;
	}
}