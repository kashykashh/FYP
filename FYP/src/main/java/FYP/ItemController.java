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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

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
	public String saveItem(Item item, @RequestParam("itemImage") MultipartFile imgFile) {
		String imageName = imgFile.getOriginalFilename();

		item.setImgName(imageName);

		User loggedInUser = userRepository.getById(getLoggedInUserId());
		item.setUser(loggedInUser);

		Item savedItem = itemRepository.save(item);

		try {
			String uploadDir = "uploads/items/" + savedItem.getId();
			Path uploadPath = Paths.get(uploadDir);
			System.out.println("Directory path: " + uploadPath);

			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			Path fileToCreatePath = uploadPath.resolve(imageName);
			System.out.println("File path: " + fileToCreatePath);

			Files.copy(imgFile.getInputStream(), fileToCreatePath, StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException io) {
			io.printStackTrace();
		}

		itemRepository.save(savedItem);

		return "redirect:/items";
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
			@RequestParam(value = "itemImage", required = false) MultipartFile imgFile) {
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

			// Update the existing item with the updated fields
			Item savedItem = itemRepository.save(updatedItem);

			try {
				if (imgFile != null && !imgFile.isEmpty()) {
					String uploadDir = "uploads/items/" + savedItem.getId();
					Path uploadPath = Paths.get(uploadDir);
					System.out.println("Directory path: " + uploadPath);

					if (!Files.exists(uploadPath)) {
						Files.createDirectories(uploadPath);
					}

					Path fileToCreatePath = uploadPath.resolve(imageName);
					System.out.println("File path: " + fileToCreatePath);

					Files.copy(imgFile.getInputStream(), fileToCreatePath, StandardCopyOption.REPLACE_EXISTING);
				}

			} catch (IOException io) {
				io.printStackTrace();
			}

			return "redirect:/items";
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

	public class ImageModerationExample {
		public static void main(String[] args) throws Exception {
			// API endpoint and access token
			String apiEndpoint = "https://api.openai.com/v1/engines/davinci/moderate";
			String accessToken = "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";

			// Read and encode the image file
			String imagePath = "uploads/items/4";
			byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
			String base64Image = Base64.getEncoder().encodeToString(imageBytes);

			// Construct the API request
			URL url = new URL(apiEndpoint);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", "Bearer " + accessToken);
			connection.setDoOutput(true);

			// Create the request body
			String requestBody = "{\"inputs\": [{\"data\": {\"image\": \"" + base64Image + "\"}}]}";

			// Send the request
			DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
			outputStream.writeBytes(requestBody);
			outputStream.flush();
			outputStream.close();

			// Get the API response
			int responseCode = connection.getResponseCode();
			BufferedReader reader;
			if (responseCode == HttpURLConnection.HTTP_OK) {
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			} else {
				reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
			}

			// Parse and handle the response
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();

			// Handle the moderation results
			String moderationResults = response.toString();
			// Implement your logic here to take appropriate actions based on the moderation
			// results

			// Print the moderation results
			System.out.println(moderationResults);
		}
	}
}