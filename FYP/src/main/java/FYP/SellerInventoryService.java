/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-May-03 7:25:13 PM

 */
package FYP;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * @author 21033239
 *
 */

@Service
public class SellerInventoryService {
	private final ItemRepository itemRepository;

	private final CategoryRepository categoryRepository;

	private final UserRepository userRepository;

	public SellerInventoryService(ItemRepository itemRepository, CategoryRepository categoryRepository,
			UserRepository userRepository) {
		this.itemRepository = itemRepository;
		this.categoryRepository = categoryRepository;
		this.userRepository = userRepository;
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

	public List<Item> getItemsByUser(User user) {
		return itemRepository.findByUser(user);
	}

	public User getCurrentUser() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return userRepository.findByUsername(username);
	}
}
