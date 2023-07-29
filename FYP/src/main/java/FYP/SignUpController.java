/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-Feb-17 3:42:40 PM
 *
 */

package FYP;

import java.util.List;

import javax.validation.Valid;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
public class SignUpController {

	@Autowired
	private UserService userService;

	@Value("${sightengine.api.user}")
	private String sightengineApiUser;

	@Value("${sightengine.api.secret}")
	private String sightengineApiSecret;

	@GetMapping("/register")
	public String showSignUpForm(Model model) {
		model.addAttribute("user", new User());
		return "signup";
	}

	@PostMapping("/register")
	public String registerUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult, Model model,
			@RequestParam("selectedRole") String selectedRole) {
		try {
			boolean isUsernameAcceptable = isUsernameModerate(user.getUsername());
			if (!isUsernameAcceptable) {
				model.addAttribute("error", "Inappropriate Username. Please choose a different username.");
				return "signup";
			}

			if (bindingResult.hasErrors()) {
				return "signup";
			}

			if ("buyer".equals(selectedRole)) {
				userService.saveUserWithBuyerRole(user);
			} else if ("seller".equals(selectedRole)) {
				userService.saveUserWithSellerRole(user);
			}
			return "redirect:/login";
		} catch (Exception e) {
			model.addAttribute("error", "Failed to register user. Please try again.");
			return "signup";
		}
	}

	private boolean isUsernameModerate(String username) {
		try {
			// Build the request body
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);

			MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
			bodyMap.add("text", username);
			bodyMap.add("lang", "en");
			bodyMap.add("mode", "username");
			bodyMap.add("api_user", sightengineApiUser);
			bodyMap.add("api_secret", sightengineApiSecret);

			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);

			// Make the API call
			ResponseEntity<String> responseEntity = new RestTemplate()
					.postForEntity("https://api.sightengine.com/1.0/text/check.json", requestEntity, String.class);

			// Log the API response on the console
			System.out.println("API Response:");
			System.out.println(responseEntity.getBody());

			// Handle the API response
			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				String response = responseEntity.getBody();

				// Parse the response to get the moderation result
				JSONObject jsonResponse = new JSONObject(response);

				// Check if the response contains the "profanity" field
				if (jsonResponse.has("profanity")) {
					// Get the "matches" array from the "profanity" object
					JSONArray matches = jsonResponse.getJSONObject("profanity").getJSONArray("matches");
					return matches.length() == 0;
				}
			}

		} catch (Exception e) {
			// Handle any exceptions here, e.g., API call failure
			e.printStackTrace();
		}

		// If something went wrong or the response doesn't have the expected structure,
		// assume the username is acceptable
		return true;
	}

	@GetMapping("/list_users")
	public String viewUsersList(Model model) {
		List<User> listUsers = userService.listAll();
		model.addAttribute("listUsers", listUsers);
		return "users";
	}
}
