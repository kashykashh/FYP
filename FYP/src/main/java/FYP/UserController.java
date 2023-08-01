/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-Jan-18 6:48:41 PM

 */
package FYP;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author 21033239
 *
 */

@Controller
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private UserService userService;

	@GetMapping("/user")
	public String viewUsers(Model model) {

		List<User> listUsers = userRepository.findAll();
		model.addAttribute("listUsers", listUsers);
		return "view_users";

	}

	@GetMapping("/user/add")
	public String addUser(Model model) {
		model.addAttribute("user", new User());
		return "add_user";
	}

	@PostMapping("/user/save")
	public String saveUser(User user, RedirectAttributes redirectAttribute,
			@RequestParam("selectedRole") String selectedRole) {

		if (selectedRole.equals("buyer")) {
			userService.saveUserWithBuyerRole(user);
		} else if (selectedRole.equals("seller")) {
			userService.saveUserWithSellerRole(user);
		} else if (selectedRole.equals("admin")) {
			userService.saveUserWithAdminRole(user);
		}

		return "redirect:/user";
	}

	@GetMapping("/user/edit/{id}")
	public String editUser(@PathVariable("id") Long id, Model model) {

		User user = userRepository.getById(id);
		model.addAttribute("user", user);

		return "edit_user";
	}

	@PostMapping("/user/edit/{id}")
	public String saveUpdatedUser(@PathVariable("id") Long id, User editedUser,
			@RequestParam("selectedRole") String selectedRole) {
		User existingUser = userRepository.getById(id);

		existingUser.setUsername(editedUser.getUsername());
		existingUser.setEmail(editedUser.getEmail());

		// Update the role only if it has changed
		if (!selectedRole.equals(existingUser.getRole())) {
			existingUser.setRole("ROLE_" + selectedRole.toUpperCase());
		}

		userRepository.save(existingUser);
		return "redirect:/user";
	}

	@GetMapping("/user/delete/{id}")
	public String deleteUser(@PathVariable("id") Long id, Authentication authentication) {
		User user = userRepository.findById(id).orElse(null);
		if (user != null) {
			String deletedBy = authentication.getName(); // Get the username of the logged-in admin
			user.setDeletedBy(deletedBy);
			user.setDeletedAt(new Date());
			userRepository.save(user);
		}
		return "redirect:/user";
	}
}
