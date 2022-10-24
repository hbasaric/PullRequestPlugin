package pullrequest;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;

import hsselect.Server;

public class GeneratePullRequest {
	private static final String authorization = "Bearer {your_PAT}";
	// private static final String baseUrl =
	// "https://api.github.com/repos/{your_username}/{your_repo_name}";
	private static final String baseUrl = "https://github.com/hbasaric/PullRequestPlugin.git";
	private static final ObjectMapper objectMapper = new ObjectMapper();

	File gpmlFile = new File("WP1.gpml");
	// login to github repo

	// generate pull request

	static public void main(String args[]) throws Exception {

	}

}
