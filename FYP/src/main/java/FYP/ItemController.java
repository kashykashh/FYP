/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2022-Dec-02 8:45:46 AM

 */
package FYP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author 21033239
 *
 */

@Controller
public class ItemController {

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	private final int MAX_MODERATION_REJECTS = 3; // Maximum allowed image moderation rejects

	@GetMapping("/items")
	public String viewItems(Model model) {

		User loggedInUser = userRepository.getById(getLoggedInUserId());

		if (loggedInUser.getRole().equalsIgnoreCase("ROLE_ADMIN")) {
			List<Item> listItems = itemRepository.findAll();
			model.addAttribute("listItems", listItems);
		} else if (loggedInUser.getRole().equalsIgnoreCase("ROLE_SELLER")) {
			// Find the items associated with the logged-in user
			List<Item> listItems = itemRepository.findByUser(loggedInUser);
			model.addAttribute("listItems", listItems);
		}
		return "view_items";
	}

	@GetMapping("/items/add")
	public String addItem(Model model) {
		model.addAttribute("item", new Item());

		List<Category> catList = categoryRepository.findAll();
		model.addAttribute("catList", catList);

		return "add_item";
	}

	@PostMapping("/items/save")
	public String saveItem(Item item, @RequestParam("itemImage") MultipartFile imgFile, Model model) {
		String imageName = imgFile.getOriginalFilename();
		item.setImgName(imageName);

		User loggedInUser = userRepository.getById(getLoggedInUserId());
		item.setUser(loggedInUser);

		// Perform image moderation before saving the image
		String moderationResult = performImageModeration(imgFile, loggedInUser);
		if (moderationResult.equalsIgnoreCase("accept")) {
			// Save the image in the database and file system
			Item savedItem = itemRepository.save(item);

			try {
				String uploadDir = "uploads/items/" + savedItem.getId();
				Path uploadPath = Paths.get(uploadDir);

				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}

				Path fileToCreatePath = uploadPath.resolve(imageName);

				Files.copy(imgFile.getInputStream(), fileToCreatePath, StandardCopyOption.REPLACE_EXISTING);

			} catch (IOException io) {
				io.printStackTrace();
			}

			return "redirect:/items";
		} else {
			// Show an error message because the image was rejected
			model.addAttribute("imageError",
					"The image was rejected due to inappropriate content. " + "You have "
							+ (MAX_MODERATION_REJECTS - loggedInUser.getModerationFailures())
							+ " attempts left before your account is banned.");

			// Add the list of categories to the model so that the form can be repopulated
			List<Category> catList = categoryRepository.findAll();
			model.addAttribute("catList", catList);

			return "add_item";
		}
	}

	@GetMapping("/items/{id}")
	public String viewSingleItem(@PathVariable("id") Long id, Model model) {
		Item item = itemRepository.getById(id);
		model.addAttribute("item", item);

		return "view_single_item";
	}

	@GetMapping("/items/edit/{id}")
	public String editItem(@PathVariable("id") Long id, Model model) {
		Item item = itemRepository.getById(id);
		model.addAttribute("item", item);

		List<Category> catList = categoryRepository.findAll();
		model.addAttribute("catList", catList);

		Integer selectedDuration = item.isAdvertise() ? item.getDuration() : null;
		model.addAttribute("selectedDuration", selectedDuration);

		return "edit_item";
	}

	@PostMapping("/items/edit/{id}")
	public String saveUpdatedItem(@PathVariable("id") Long id, Item updatedItem,
			@RequestParam(value = "itemImage", required = false) MultipartFile imgFile, Model model) {
		Item existingItem = itemRepository.getById(id); // Retrieve the existing item

		String imageName = imgFile != null && !imgFile.isEmpty() ? imgFile.getOriginalFilename()
				: existingItem.getImgName();
		updatedItem.setImgName(imageName); // Set the imgName to the existing image name if no new image is uploaded

		User loggedInUser = userRepository.getById(getLoggedInUserId());
		if (loggedInUser != null) {
			if (!loggedInUser.getRole().equalsIgnoreCase("ROLE_ADMIN")) { // Check if the user is not an admin
				updatedItem.setUser(loggedInUser);
			} else {
				updatedItem.setUser(existingItem.getUser()); // Set the original user if the user is an admin
			}
			updatedItem.setId(id); // Set the ID of the updated item

			// Perform image moderation before saving the image
			String moderationResult = performImageModeration(imgFile, loggedInUser);
			if (moderationResult.equalsIgnoreCase("accept")) {
				// Update the existing item with the updated fields
				Item savedItem = itemRepository.save(updatedItem);

				try {
					if (imgFile != null && !imgFile.isEmpty()) {
						String uploadDir = "uploads/items/" + savedItem.getId();
						Path uploadPath = Paths.get(uploadDir);

						if (!Files.exists(uploadPath)) {
							Files.createDirectories(uploadPath);
						}

						Path fileToCreatePath = uploadPath.resolve(imageName);

						Files.copy(imgFile.getInputStream(), fileToCreatePath, StandardCopyOption.REPLACE_EXISTING);
					}

				} catch (IOException io) {
					io.printStackTrace();
				}

				return "redirect:/items";
			} else {
				// Show an error message because the image was rejected
				model.addAttribute("error",
						"The image was rejected due to inappropriate content. Please upload a different image.");
				// Add the list of categories to the model so that the form can be repopulated
				List<Category> catList = categoryRepository.findAll();
				model.addAttribute("catList", catList);
				Integer selectedDuration = existingItem.isAdvertise() ? existingItem.getDuration() : null;
				model.addAttribute("selectedDuration", selectedDuration);
				model.addAttribute("item", existingItem);
				return "edit_item";
			}
		} else {
			return "error";
		}
	}

	@GetMapping("/items/delete/{id}")
	public String deleteItem(@PathVariable("id") Long id) {

		itemRepository.deleteById(id);

		return "redirect:/items";
	}

	public Long getLoggedInUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			Object principal = authentication.getPrincipal();
			if (principal instanceof UserDetails) {
				return ((UserDetail) principal).getId();
			}
		}
		return null;
	}

	// Helper method to perform image moderation
	private String performImageModeration(MultipartFile imgFile, User user) {
		String workflow = "wfl_eu3wn2MlnUB13ebFiOimJ";
		String apiUser = "602676279";
		String apiSecret = "Ev34FhvdZXjWWaJx6UMG";

		// Create a RestTemplate
		RestTemplate restTemplate = new RestTemplate();

		// Prepare the request body with form data
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("media", new FileSystemResource(convertMultiPartToFile(imgFile)));
		body.add("workflow", workflow);
		body.add("api_user", apiUser);
		body.add("api_secret", apiSecret);

		// Prepare the request headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		// Create the HTTP entity with the headers and body
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

		// Make the API call
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				"https://api.sightengine.com/1.0/check-workflow.json", HttpMethod.POST, requestEntity, String.class);

		// Handle the API response
		if (responseEntity.getStatusCode() == HttpStatus.OK) {
			String response = responseEntity.getBody();

			// Parse the response to get the moderation result
			JSONObject jsonResponse = new JSONObject(response);

			// Check if the response contains the "status" field
			if (jsonResponse.has("status") && jsonResponse.getString("status").equals("success")) {
				// Check if the response contains the "summary" field
				if (jsonResponse.has("summary")) {
					// Get the "action" value from the "summary" object
					JSONObject summary = jsonResponse.getJSONObject("summary");
					if (summary.has("action")) {
						String moderationResult = summary.getString("action");

						if (moderationResult.equals("reject")) {
							// Increment the user's moderation failures count
							int failures = user.getModerationFailures();
							user.setModerationFailures(failures + 1);
							userRepository.save(user);

							// Check if the user should be banned
							if (user.getModerationFailures() >= MAX_MODERATION_REJECTS) {
								user.setBanned(true);
								userRepository.save(user);
							}
						}

						return moderationResult;
					}
				}
			}
		}

		// In case of an error or if the moderation result is not available, return
		// "reject"
		return "reject";
	}

	// Helper method to convert MultipartFile to File
	private File convertMultiPartToFile(MultipartFile file) {
		File convFile = new File(file.getOriginalFilename());
		try {
			FileOutputStream fos = new FileOutputStream(convFile);
			fos.write(file.getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return convFile;
	}

	// Admin Unban users

	@GetMapping("/admin")
	public String adminPage(Model model) {
		List<User> bannedUsers = userRepository.findByBannedTrue();
		model.addAttribute("bannedUsers", bannedUsers);
		return "admin_page";
	}

	@PostMapping("/unban/{userId}")
	public String unbanUser(@PathVariable("userId") Long userId) {
		User user = userRepository.findById(userId).orElse(null);
		if (user != null) {
			user.setBanned(false);
			user.setModerationFailures(0);
			userRepository.save(user);
		}
		return "redirect:/admin";
	}
}
