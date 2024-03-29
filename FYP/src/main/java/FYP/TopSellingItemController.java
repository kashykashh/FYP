/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-Jun-19 7:34:16 PM

 */
package FYP;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 21033239
 *
 */

@Controller
public class TopSellingItemController {

	private final TopSellingItemRepository topSellingItemRepository;
	private final UserRepository userRepository;
	private final ItemRepository itemRepository;

	public TopSellingItemController(TopSellingItemRepository topSellingItemRepository, UserRepository userRepository,
			ItemRepository itemRepository) {
		this.topSellingItemRepository = topSellingItemRepository;
		this.userRepository = userRepository;
		this.itemRepository = itemRepository;
	}

	@GetMapping("/seller/top-selling-items")
	public String viewTopSellingItemsForSeller(Model model, Principal principal) {
		String username = principal.getName();
		User seller = userRepository.findByUsername(username);
		List<TopSellingItem> topSellingItems = topSellingItemRepository.findBySeller(seller);

		List<TopSellingItem> filteredTopSellingItems = topSellingItems.stream()
				.filter(item -> item.getItem() != null && item.getItem().getName() != null)
				.collect(Collectors.toList());

		model.addAttribute("topSellingItems", filteredTopSellingItems);
		return "seller_top-selling-items";
	}

	@GetMapping("/admin/top-selling-items-sellerList")
	public String viewTopSellingItemsForAdmin(Model model) {
		List<User> sellers = userRepository.findAll().stream().filter(user -> user.getRole().equals("ROLE_SELLER"))
				.peek(seller -> {
					// Calculate total revenue for each seller
					List<TopSellingItem> topSellingItems = topSellingItemRepository.findBySeller(seller);
					double totalRevenue = topSellingItems.stream().filter(item -> item.getItem() != null).mapToDouble(
							topSellingItem -> topSellingItem.getQuantitySold() * topSellingItem.getItem().getPrice())
							.sum();
					seller.setTotalRevenue(totalRevenue);
				}).sorted(Comparator.comparingDouble(User::getTotalRevenue).reversed()).collect(Collectors.toList());

		model.addAttribute("sellers", sellers);

		return "admin_top-selling-items-sellerList";
	}

	@GetMapping("/admin/top-sellers-graph")
	public String showTopSellersGraph(Model model) {
		List<User> sellers = userRepository.findAll().stream().filter(user -> user.getRole().equals("ROLE_SELLER"))
				.peek(seller -> {
					// Calculate total revenue for each seller
					List<TopSellingItem> topSellingItems = topSellingItemRepository.findBySeller(seller);
					double totalRevenue = topSellingItems.stream().mapToDouble(
							topSellingItem -> topSellingItem.getQuantitySold() * topSellingItem.getItem().getPrice())
							.sum();
					seller.setTotalRevenue(totalRevenue);
				}).sorted(Comparator.comparingDouble(User::getTotalRevenue).reversed()).collect(Collectors.toList());

		model.addAttribute("sellersData", sellers);
		return "admin_top-sellers-graph";
	}

	@GetMapping("/admin/top-selling-items/{sellerId}")
	public String viewTopSellingItemsForSellerId(@PathVariable Long sellerId, Model model) {
		User seller = userRepository.findById(sellerId).orElse(null);
		if (seller == null) {
			// Handle seller not found error
			return "error";
		}
		List<TopSellingItem> topSellingItems = topSellingItemRepository.findBySellerOrderByQuantitySoldDesc(seller);

		// Filter the topSellingItems list to exclude items with null name
		List<TopSellingItem> filteredTopSellingItems = topSellingItems.stream()
				.filter(item -> item.getItem() != null && item.getItem().getName() != null)
				.collect(Collectors.toList());

		model.addAttribute("topSellingItems", filteredTopSellingItems);
		return "admin_top-selling-items";
	}

	@PostMapping("/purchase")
	public String handlePurchase(@RequestParam("itemId") Long itemId, Principal principal) {
		String username = principal.getName();
		User buyer = userRepository.findByUsername(username);
		Item item = itemRepository.findById(itemId).orElse(null);

		if (item == null) {
			// Handle item not found error
			return "error";
		}

		// Update the quantity of the item and save it
		int newQuantity = item.getQuantity() - 1;
		item.setQuantity(newQuantity);
		itemRepository.save(item);

		// Check if the item already exists in the top selling items
		TopSellingItem topSellingItem = topSellingItemRepository.findByItem(item);
		if (topSellingItem == null) {
			// Create a new top selling item
			topSellingItem = new TopSellingItem();
			topSellingItem.setItem(item);
			topSellingItem.setSeller(item.getUser());
			topSellingItem.setQuantitySold(1);
		} else {
			// Increment the total sales of the existing top selling item
			topSellingItem.setQuantitySold(topSellingItem.getQuantitySold() + 1);
		}

		// Save the top selling item
		topSellingItemRepository.save(topSellingItem);

		return "redirect:/seller/top-selling-items";
	}
}
