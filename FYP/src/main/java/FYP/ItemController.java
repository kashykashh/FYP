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
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

	@Autowired
	private TopSellingItemRepository topSellingItemRepository;

	@Autowired
	private OrderItemRepository orderItemRepository;

	@Autowired
	private CartItemRepository cartItemRepository;


	private final int MAX_MODERATION_REJECTS = 3; // Maximum allowed image moderation rejects

	@GetMapping("/items/updatePrices")
	public String updateItemPrices(@RequestParam("exchangeRate") double exchangeRate) {
		List<Item> itemList = itemRepository.findAll();

		for (Item item : itemList) {
			double updatedPrice = item.getBasePrice() * exchangeRate;

			// Round the updated price to 2 decimal places
			BigDecimal roundedPrice = BigDecimal.valueOf(updatedPrice).setScale(2, RoundingMode.HALF_UP);
			item.setPrice(roundedPrice.doubleValue());

			itemRepository.save(item);
		}

		return "redirect:/items";
	}

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

		try {
			if (imgFile == null || imgFile.isEmpty()) {
				// Show an error message because the image is required
				model.addAttribute("imageError", "Please upload an image for the item.");

				// Add the list of categories to the model so that the form can be repopulated
				List<Category> catList = categoryRepository.findAll();
				model.addAttribute("catList", catList);

				return "add_item";
			}

			// Perform image moderation before saving the image
			String moderationResult = performImageModeration(imgFile, loggedInUser);

			if (moderationResult.equalsIgnoreCase("accept")) {
				// Save the image in the database and file system
				Item savedItem = itemRepository.save(item);

				String uploadDir = "uploads/items/" + savedItem.getId();
				Path uploadPath = Paths.get(uploadDir);

				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}

				Path fileToCreatePath = uploadPath.resolve(imageName);

				Files.copy(imgFile.getInputStream(), fileToCreatePath, StandardCopyOption.REPLACE_EXISTING);

				return "redirect:/items";
			} else {
				// Show an error message because the image was rejected
				model.addAttribute("imageError",
						"The image was rejected due to inappropriate content. " + "You have "
								+ (MAX_MODERATION_REJECTS - loggedInUser.getModerationFailures())
								+ " attempt(s) left before your account is banned. Please upload a different image.");

				// Add the list of categories to the model so that the form can be repopulated
				List<Category> catList = categoryRepository.findAll();
				model.addAttribute("catList", catList);

				// If the user is banned, log them out and redirect to the login page
				if (loggedInUser.isBanned()) {
					userRepository.save(loggedInUser);
					return "redirect:/login?logout";
				}

				return "add_item";
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "error";
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

		if (model.containsAttribute("imageError")) {
			model.addAttribute("imageError", model.getAttribute("imageError"));
		}

		return "edit_item";
	}

	@PostMapping("/items/edit/{id}")
	public String saveUpdatedItem(@PathVariable("id") Long id, @ModelAttribute Item updatedItem,
			@RequestParam(value = "itemImage", required = false) MultipartFile imgFile, Model model) {
		// Fetch the existing item from the database
		Item existingItem = itemRepository.getById(id);

		// Manually copy the fields to update from updatedItem to existingItem
		existingItem.setName(updatedItem.getName());
		existingItem.setDescription(updatedItem.getDescription());
		existingItem.setBasePrice(updatedItem.getBasePrice());
		existingItem.setQuantity(updatedItem.getQuantity());
		existingItem.setCategory(updatedItem.getCategory());

		// Set the imgName to the existing image name if no new image is uploaded
		if (imgFile != null && !imgFile.isEmpty()) {
			String imageName = imgFile.getOriginalFilename();
			existingItem.setImgName(imageName);
		}

		User loggedInUser = userRepository.getById(getLoggedInUserId());

		if (loggedInUser != null) {
			// Check if the user is not an admin
			if (!loggedInUser.getRole().equalsIgnoreCase("ROLE_ADMIN")) {
				existingItem.setUser(loggedInUser);
			} else {
				// Set the original user if the user is an admin
				existingItem.setUser(existingItem.getUser());
			}

			existingItem.setId(id); // Set the ID of the updated item

			try {
				// Perform image moderation only if a new image is uploaded
				if (imgFile != null && !imgFile.isEmpty()) {
					// Perform image moderation before saving the image
					String moderationResult = performImageModeration(imgFile, loggedInUser);

					if (moderationResult.equalsIgnoreCase("reject")) {
						// Show an error message because the image was rejected
						model.addAttribute("imageError", "The image was rejected due to inappropriate content. "
								+ "You have " + (MAX_MODERATION_REJECTS - loggedInUser.getModerationFailures())
								+ " attempt(s) left before your account is banned. Please upload a different image.");

						// Add the list of categories to the model so that the form can be repopulated
						List<Category> catList = categoryRepository.findAll();
						model.addAttribute("catList", catList);
						model.addAttribute("imageModerationStatus", moderationResult);
						Integer selectedDuration = existingItem.isAdvertise() ? existingItem.getDuration() : null;
						model.addAttribute("selectedDuration", selectedDuration);
						model.addAttribute("item", existingItem);

						// If the user is banned, log them out and redirect to the login page
						if (loggedInUser.isBanned()) {
							userRepository.save(loggedInUser);
							return "redirect:/login?logout";
						}

						return "edit_item";
					}
				}

				// Save the updated item
				Item savedItem = itemRepository.save(existingItem);

				if (imgFile != null && !imgFile.isEmpty()) {
					String uploadDir = "uploads/items/" + savedItem.getId();
					Path uploadPath = Paths.get(uploadDir);

					if (!Files.exists(uploadPath)) {
						Files.createDirectories(uploadPath);
					}

					Path fileToCreatePath = uploadPath.resolve(savedItem.getImgName());

					Files.copy(imgFile.getInputStream(), fileToCreatePath, StandardCopyOption.REPLACE_EXISTING);
				}

				return "redirect:/items";
			} catch (IOException e) {
				e.printStackTrace();
				return "error";
			}
		} else {
			return "error";
		}
	}

	@GetMapping("/items/delete/{id}")
	public String deleteItem(@PathVariable("id") Long id) {
		Item item = itemRepository.findById(id).orElse(null);

		if (item != null) {
			// Remove the association between TopSellingItem and Item
			TopSellingItem topSellingItem = topSellingItemRepository.findByItem(item);
			if (topSellingItem != null) {
				topSellingItemRepository.delete(topSellingItem);
			}

			List<OrderItem> orderItems = orderItemRepository.findByItem(item);

			List<CartItem> cartItems = cartItemRepository.findByItem(item);
			cartItemRepository.deleteAll(cartItems);

			for (OrderItem orderItem : orderItems) {
				orderItem.setUser(null);
				orderItemRepository.save(orderItem);
			}

			itemRepository.deleteById(id);
		}

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
		String workflow = "wfl_eAjJmZgKXdH5rrJaybpAi";
		String apiUser = "741776820";
		String apiSecret = "CVpuk4XkQDGGpM8AFQSW";

		// Create a RestTemplate
		RestTemplate restTemplate = new RestTemplate();

		try {
			// Prepare the request body with form data
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("media", new ByteArrayResource(imgFile.getBytes()) {
				@Override
				public String getFilename() {
					return imgFile.getOriginalFilename();
				}
			});
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
					"https://api.sightengine.com/1.0/check-workflow.json", HttpMethod.POST, requestEntity,
					String.class);

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

							// Update the user's moderation failures count
							if (moderationResult.equals("reject")) {
								int failures = user.getModerationFailures();
								user.setModerationFailures(failures + 1);
								userRepository.save(user);
							}

							// Check if the user should be banned
							if (user.getModerationFailures() >= MAX_MODERATION_REJECTS) {
								user.setBanned(true);
								user.setModerationFailures(0);
								userRepository.save(user);

								// Set the quantity of all the banned user's items to 0
								List<Item> userItems = user.getItems();
								for (Item item : userItems) {
									item.setQuantity(0);
								}

								return "reject";
							}

							return moderationResult;
						}
					}
				}
			}
			return "reject";
		} catch (IOException e) {
			// Log the exception and return a moderation failure
			e.printStackTrace();
			return "reject";
		}
	}

	// Convert MultipartFile to File
	private File convertMultiPartToFile(MultipartFile file) throws IOException {
		try {
			String tempDir = System.getProperty("java.io.tmpdir");
			String tempFileName = "upload_" + UUID.randomUUID().toString() + "_" + System.nanoTime() + ".tmp";
			File convFile = new File(tempDir + File.separator + tempFileName);
			file.transferTo(convFile);
			return convFile;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
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