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

		List<Item> listItems = itemRepository.findAll();

		model.addAttribute("listItems", listItems);
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

		return "edit_item";
	}

	@PostMapping("/items/edit/{id}")
	public String saveUpdatedItem(@PathVariable("id") Long id, Item item,
			@RequestParam("itemImage") MultipartFile imgFile) {
		String imageName = imgFile.getOriginalFilename();

		item.setImgName(imageName);

		User loggedInUser = userRepository.getById(getLoggedInUserId());
		if (loggedInUser != null) {
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

}
