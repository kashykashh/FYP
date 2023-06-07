/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-May-02 7:00:36 PM

 */
package FYP;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * @author 21033239
 *
 */
@Service
public class AdminInventoryService {
	private final ItemRepository itemRepository;

	private final CategoryRepository categoryRepository;

	public AdminInventoryService(ItemRepository itemRepository, CategoryRepository categoryRepository) {
		this.itemRepository = itemRepository;
		this.categoryRepository = categoryRepository;
	}

	public List<Item> getAllItems() {
		return itemRepository.findAll();
	}

	public List<Item> searchItems(String keyword) {
		return itemRepository.findByNameContainingIgnoreCase(keyword);
	}

	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}
}

