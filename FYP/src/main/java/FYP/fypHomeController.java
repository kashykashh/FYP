/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2022-Dec-01 11:50:45 PM

 */
package FYP;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author 21033239
 *
 */

@Controller
public class fypHomeController {

	@Autowired
	private ItemRepository itemRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;

	@GetMapping("/")
	public String home(Model model) {
		List<Item> listItems = itemRepository.findAll();
	    List<Category> listCategories = categoryRepository.findAll();
		model.addAttribute("listItems", listItems);
	    model.addAttribute("listCategories", listCategories);

		return "index";
	}

	@GetMapping("/403")
	public String error403() {
		return "403";
	}
}
