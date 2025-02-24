package org.aleks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

import static java.lang.System.exit;

public class GitHubUserActivity {
    public static void main(String[] args) throws IOException, URISyntaxException {
        /*if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java GitHubUserActivity <username>");
        }*/
        checkUserActivity("kamranahmedse");
    }

    private static void checkUserActivity(String username) throws URISyntaxException, IOException {
        URL url = new URI("https://api.github.com/users/" + username + "/events").toURL();

        HttpURLConnection connection = getUrlConnection(url);
        setGETProtocol(connection);
        int responseCode = getResponseCode(connection);

        if (responseCode != HttpURLConnection.HTTP_OK) {return;}

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        printResponse(response);
    }

    private static int getResponseCode(HttpURLConnection connection) {
        int responseCode = 0;

        try {
            responseCode = connection.getResponseCode();
        } catch (IOException e) {
            System.err.println("No response code returned");
        }

        return responseCode;
    }

    private static HttpURLConnection getUrlConnection(URL url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            System.err.println("Couldn't connect to GitHub API");
            exit(1);
        }
        return connection;
    }

    private static void setGETProtocol(HttpURLConnection connection) {
        try {
            connection.setRequestMethod("GET");
        } catch (ProtocolException e) {
            System.err.println("Error with request protocol");
            exit(2);
        }
    }

    private static void printResponse(StringBuilder response) {
        JsonNode jsonNode = parseJson(response.toString());

        for (JsonNode node : jsonNode) {
            switch (node.get("type").asText()) {
                case "PushEvent":
                    int commits = node.get("payload").get("commits").size();
                    System.out.println("- " + commits + " commits done to repo " + node.get("repo").get("name").asText());
                    break;
                case "PullRequestEvent":
                    System.out.println("- Created pull request to " + node.get("payload").get("pull_request").get("url").asText());
                    break;
                case "WatchEvent":
                    String action = node.get("payload").get("action").asText();
                    System.out.println("- " + action.substring(0, 1).toUpperCase() + action.substring(1) + " watching repo " + node.get("repo").get("name").asText());
                    break;
                case "IssueCommentEvent":
                    action = node.get("payload").get("action").asText();
                    System.out.println("- " + action.substring(0, 1).toUpperCase() + action.substring(1) + " issue comment on repo " + node.get("repo").get("name").asText());
                    break;
                case "IssuesEvent":
                    action = node.get("payload").get("action").asText();
                    System.out.println("- " + action.substring(0, 1).toUpperCase() + action.substring(1) + " issues on repo " + node.get("repo").get("name").asText());
                    break;
                case "PullRequestReviewEvent":
                    action = node.get("payload").get("action").asText();
                    System.out.println("- " + action.substring(0, 1).toUpperCase() + action.substring(1) + " pull request review on repo " + node.get("repo").get("name").asText());
                    break;
                case "PullRequestReviewCommentEvent":
                    action = node.get("payload").get("action").asText();
                    System.out.println("- " + action.substring(0,1).toUpperCase() + action.substring(1) + "pull request review comment on repo " + node.get("repo").get("name").asText());
                    break;
                default:
                    action = node.get("payload").get("action").asText();
                    String event = node.get("type").asText().replace("Event", "");
                    System.out.println("- " + action + " " + event + " on repo " + node.get("repo").get("name").asText());
            }
        }
    }

    private static JsonNode parseJson(String response) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = null;

        try {
            jsonNode = mapper.readTree(response);
        } catch (JsonProcessingException e) {
            System.err.println("Couldn't parse response");
            exit(3);
        }

        return jsonNode;
    }
}

