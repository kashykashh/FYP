/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-May-02 6:49:08 PM

 */
package FYP;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;

/**
 * @author 21033239
 *
 */
@Controller
public class AdminInventoryController {

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private UserRepository userRepository;

	private final AdminInventoryService adminInventoryService;

	public AdminInventoryController(AdminInventoryService adminInventoryService) {
		this.adminInventoryService = adminInventoryService;
	}

	@GetMapping("/adminInventory")
	public String getSellerInventory(Model model, @RequestParam(required = false) String keyword,
			@RequestParam(required = false) Integer maxQuantity, @RequestParam(required = false) Long categoryId,
			@RequestParam(required = false) String sortField, @RequestParam(required = false) String sortOrder,
			@RequestParam(required = false) String userName) {

		List<Item> itemList = null;
		List<Category> categories = adminInventoryService.getAllCategories();

		Specification<Item> spec = Specification.where(null);

		if (userName != null && !userName.isEmpty()) {
			// Search by user name
			spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder
					.like(criteriaBuilder.lower(root.get("user").get("name")), "%" + userName.toLowerCase() + "%"));
		}

		if (keyword != null && !keyword.isEmpty()) {
			spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder
					.like(criteriaBuilder.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"));
		}

		if (maxQuantity != null) {
			spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("quantity"),
					maxQuantity));
		}

		if (categoryId != null) {
			spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("category").get("id"),
					categoryId));
		}

		PageRequest pageRequest;
		if (sortField != null && !sortField.isEmpty() && sortOrder != null && !sortOrder.isEmpty()) {
			Sort sort = Sort.by(sortField);
			if (sortOrder.equals("desc")) {
				sort = sort.descending();
			}
			pageRequest = PageRequest.of(0, Integer.MAX_VALUE, sort);
		} else {
			pageRequest = PageRequest.of(0, Integer.MAX_VALUE);
		}

		itemList = itemRepository.findAll(spec, pageRequest).getContent();

		model.addAttribute("itemList", itemList);
		model.addAttribute("categories", categories);
		model.addAttribute("keyword", keyword);
		model.addAttribute("maxQuantity", maxQuantity);
		model.addAttribute("categoryId", categoryId);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortOrder", sortOrder);

		return "adminInventory";
	}

	@PostMapping("/adminInventory/update/{itemId}")
	public String updateQuantity(@RequestParam("itemId") Long itemId, @RequestParam("quantity") Integer quantity) {
		Item item = itemRepository.findById(itemId).orElse(null);
		if (item != null) {
			item.setQuantity(quantity);
			itemRepository.save(item);
		}
		return "redirect:/adminInventory";
	}

	@PostMapping("/adminInventory/undo/{itemId}")
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
		return "redirect:/adminInventory";
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