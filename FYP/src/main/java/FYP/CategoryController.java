/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2022-Dec-02 1:29:42 PM

 */
package FYP;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author 21033239
 *
 */
@Controller
public class CategoryController {

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ItemRepository itemRepository;

	@GetMapping("/categories")
	public String viewCategories(Model model) {

		List<Category> listCategories = categoryRepository.findAll();

		model.addAttribute("listCategories", listCategories);
		return "view_categories";
	}

	@GetMapping("/categories/add")
	public String addCategory(Model model) {
		model.addAttribute("category", new Category());
		return "add_category";
	}

	@PostMapping("/categories/save")
	public String saveCategory(Category category) {
		categoryRepository.save(category);
		return "redirect:/categories";
	}

	@GetMapping("/categories/edit/{id}")
	public String editCategory(@PathVariable("id") Long id, Model model) {
		Category category = categoryRepository.getById(id);
		model.addAttribute("category", category);

		return "edit_category";
	}

	@PostMapping("/categories/edit/{id}")
	public String saveUpdatedCategory(@PathVariable("id") Long id, Category category) {
		categoryRepository.save(category);
		return "redirect:/categories";

	}

	@GetMapping("/categories/delete/{id}")
	public String deleteCategory(@PathVariable("id") Long id) {

		categoryRepository.deleteById(id);

		return "redirect:/categories";
	}

	@GetMapping("/categories/{categoryId}")
	public String viewItemsByCategory(@PathVariable("categoryId") Long categoryId, Model model) {
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + categoryId));
		List<Item> items = itemRepository.findByCategoryId(categoryId);
		model.addAttribute("category", category);
		model.addAttribute("items", filterItemsWithZeroQuantity(items));
		return "view_items_by_category";

	}

	private List<Item> filterItemsWithZeroQuantity(List<Item> itemList) {
		return itemList.stream().filter(item -> item.getQuantity() > 0).collect(Collectors.toList());
	}

}