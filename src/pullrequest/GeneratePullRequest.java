package pullrequest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GeneratePullRequest {
	private static final String authorization = "Bearer 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";
	// private static final String baseUrl =
	// "https://api.github.com/repos/{your_username}/{your_repo_name}";
	private static final String baseUrl = "https://api.github.com/repos/hbasaric/PullRequestPlugin";
	private static final ObjectMapper objectMapper = new ObjectMapper();

	File gpmlFile = new File("WP1.gpml");

	static public void main(String args[]) throws Exception {
		new GeneratePullRequest().executeExample();
	}
	
    private String get(String path) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder().uri(URI.create(baseUrl + path))
                .setHeader("Authorization", authorization)
                .setHeader("Accept", "application/json")
                .setHeader("Content-Type","application/json;charset=UTF-8")
                .GET()
                .build();
        
        var response = HttpClient.newHttpClient().send(request, BodyHandlers.ofString());
        return response.body();
    }
    private String delete(String path) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder().uri(URI.create(baseUrl + path))
                .setHeader("Authorization", authorization)
                .DELETE()
                .build();

        var response = HttpClient.newHttpClient().send(request, BodyHandlers.ofString());
        return response.body();
    }
    private String post(String path, String body) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder().uri(URI.create(baseUrl + path))
                .setHeader("Authorization", authorization)
                .POST(BodyPublishers.ofString(body))
                .build();

        var response = HttpClient.newHttpClient().send(request, BodyHandlers.ofString());
        return response.body();
    }
    private String put(String path, String body) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder().uri(URI.create(baseUrl + path))
                .setHeader("Authorization", authorization)
                .PUT(BodyPublishers.ofString(body))
                .build();

        var response = HttpClient.newHttpClient().send(request, BodyHandlers.ofString());
        return response.body();
    }
    
    private String getResourceFile(String filename) throws IOException, ClassNotFoundException {
      //  var fileStream = Class.forName("GeneratePullRequest").getResourceAsStream("WP1.gpml");
        var fileStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
        return new String(Objects.requireNonNull(fileStream).readAllBytes(), StandardCharsets.UTF_8);
    }
    private String getMasterBranchSHA() throws IOException, InterruptedException {
        var body = this.get("/git/refs/heads");

        var sha = objectMapper.readTree(body)
                .get(0)
                .get("object")
                .get("sha")
                .asText();

        return sha;
    }

    private String createBranch(String sha) throws IOException, InterruptedException {
        var createBranchMap = Map.of(
                "ref", "refs/heads/new-branch",
                "sha", sha);

        var requestBody = objectMapper.writeValueAsString(createBranchMap);
        return this.post("/git/refs", requestBody);
    }
    private String createFile() throws IOException, InterruptedException, ClassNotFoundException {
        var fileToAdd = getResourceFile("WP1.gpml");
        var encodedContent = java.util.Base64.getEncoder().encodeToString(fileToAdd.getBytes());

        var createMap = Map.of(
                "message", "New file added",
                "content", encodedContent,
                "branch", "new-branch");

        var requestBody = objectMapper.writeValueAsString(createMap);
        return this.put("/contents/WP1.gpml", requestBody);
    }
    private String createPullRequest() throws IOException, InterruptedException {
        var createPullRequestMap = Map.of(
                "title", "test-pull-request",
                "head", "new-branch",
                "base", "master");

        var requestBody = objectMapper.writeValueAsString(createPullRequestMap);
        return this.post("/pulls", requestBody);
    }
    private String getPullNumber(String pullRequestResponse) throws JsonProcessingException {
        return objectMapper.readTree(pullRequestResponse)
                .get("number")
                .asText();
    }

    private String mergePullRequest(String pullNumber) throws IOException, InterruptedException {

        var mergeMap = Map.of(
                "commit_message", "Merging pull request");

        var requestBody = objectMapper.writeValueAsString(mergeMap);
        var url = String.format("/pulls/%s/merge", pullNumber);

        return this.put(url, requestBody);
    }
    
    private String deleteBranch() throws IOException, InterruptedException {
        return this.delete("/git/refs/heads/new-branch");
    }
    
    private void executeExample() throws IOException, InterruptedException, ClassNotFoundException {
        var masterSHA = this.getMasterBranchSHA();
        this.createBranch(masterSHA);
        this.createFile();

        var pullRequestResponse = this.createPullRequest();
        var pullNumber = this.getPullNumber(pullRequestResponse);

        //this.mergePullRequest(pullNumber);
        //this.deleteBranch();
    }


}
