/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-May-03 7:24:49 PM

 */
package FYP;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
public class SellerInventoryController {

	@Autowired
	private ItemRepository itemRepository;

	private final SellerInventoryService sellerInventoryService;

	public SellerInventoryController(SellerInventoryService sellerInventoryService) {
		this.sellerInventoryService = sellerInventoryService;
	}

	@GetMapping("/sellerInventory")
	public String getSellerInventory(Model model, @RequestParam(required = false) String keyword,
			@RequestParam(required = false) Integer maxQuantity, @RequestParam(required = false) Long categoryId,
			@RequestParam(required = false) String sortField, @RequestParam(required = false) String sortOrder) {

		List<Item> itemList = null;
		List<Category> categories = sellerInventoryService.getAllCategories();
		User currentUser = sellerInventoryService.getCurrentUser();

		if (keyword != null && !keyword.isEmpty()) {
			itemList = itemRepository.findByNameContainingIgnoreCase(keyword);
			if (maxQuantity != null) {
				itemList = itemList.stream().filter(item -> item.getQuantity() <= maxQuantity)
						.collect(Collectors.toList());
			}
			if (categoryId != null) {
				itemList = itemList.stream().filter(item -> item.getCategory().getId().equals(categoryId))
						.collect(Collectors.toList());
			}
		} else if (maxQuantity != null) {
			itemList = itemRepository.findByQuantityLessThanEqual(maxQuantity);
		} else if (categoryId != null) {
			itemList = itemRepository.findByCategoryId(categoryId);
		} else {
			itemList = itemRepository.findByUser(currentUser);
		}

		// Sorting based on user selection using lambda expressions
		if (sortField != null && !sortField.isEmpty() && sortOrder != null && !sortOrder.isEmpty()) {
			if (sortField.equals("name")) {
				if (sortOrder.equals("asc")) {
					itemList.sort(Comparator.comparing(Item::getName));
				} else {
					itemList.sort(Comparator.comparing(Item::getName).reversed());
				}
			} else if (sortField.equals("price")) {
				if (sortOrder.equals("asc")) {
					itemList.sort(Comparator.comparingDouble(Item::getPrice));
				} else {
					itemList.sort(Comparator.comparingDouble(Item::getPrice).reversed());
				}
			} else if (sortField.equals("category")) {
				if (sortOrder.equals("asc")) {
					itemList.sort(Comparator.comparing(item -> item.getCategory().getName()));
				} else {
					itemList.sort(Comparator.<Item, String>comparing(item -> item.getCategory().getName()).reversed());
				}
			}
		}

		model.addAttribute("itemList", itemList);
		model.addAttribute("categories", categories);
		model.addAttribute("keyword", keyword);
		model.addAttribute("maxQuantity", maxQuantity);
		model.addAttribute("categoryId", categoryId);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortOrder", sortOrder);

		return "sellerInventory";
	}

	@PostMapping("/sellerInventory/update/{itemId}")
	public String updateQuantity(@RequestParam("itemId") Long itemId, @RequestParam("quantity") Integer quantity) {
		Item item = itemRepository.findById(itemId).orElse(null);
		if (item != null) {

			item.setQuantity(quantity);
			itemRepository.save(item);
		}
		return "redirect:/sellerInventory";
	}

	@PostMapping("/sellerInventory/undo/{itemId}")
	public String undoQuantityUpdate(@PathVariable("itemId") Long itemId) {
		Item item = itemRepository.findById(itemId).orElse(null);
		if (item != null) {
			Integer previousQuantity = retrievePreviousQuantityFromHistory(item);
			if (previousQuantity != null) {
				item.setQuantity(previousQuantity);
				item.getQuantityHistory().remove(item.getQuantityHistory().size() - 1);
				itemRepository.save(item);
			}
		}
		return "redirect:/sellerInventory";
	}

	private Integer retrievePreviousQuantityFromHistory(Item item) {
		List<Integer> quantityHistory = item.getQuantityHistory();
		if (quantityHistory.size() > 1) {
			int lastIndex = quantityHistory.size() - 2;
			Integer previousQuantity = quantityHistory.get(lastIndex);
			item.setQuantity(previousQuantity);
			return previousQuantity;
		} else if (quantityHistory.size() == 1) {
			Integer previousQuantity = quantityHistory.get(0);
			item.setQuantity(previousQuantity);
			return previousQuantity;
		}
		return null;
	}
}