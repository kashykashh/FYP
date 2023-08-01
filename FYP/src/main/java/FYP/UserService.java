/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-May-15 5:48:25 PM

 */
package FYP;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author 21033239
 *
 */

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	public User getUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public boolean usernameExists(String username) {
		return userRepository.existsByUsername(username);
	}

	public boolean emailExists(String email) {
		return userRepository.existsByEmail(email);
	}

	public void saveUserWithSellerRole(User user) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String encodedpassword = encoder.encode(user.getPassword());
		user.setPassword(encodedpassword);

		String roleSeller = "ROLE_SELLER";
		user.setRole(roleSeller);

		User savedSeller = userRepository.save(user);
		System.out.println("Saved user: " + savedSeller.toString());
	}

	public void saveUserWithBuyerRole(User user) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String encodedpassword = encoder.encode(user.getPassword());
		user.setPassword(encodedpassword);

		String roleBuyer = "ROLE_BUYER";
		user.setRole(roleBuyer);

		User savedBuyer = userRepository.save(user);
		System.out.println("Saved user: " + savedBuyer.toString());
	}

	public void saveUserWithAdminRole(User user) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String encodedpassword = encoder.encode(user.getPassword());
		user.setPassword(encodedpassword);

		String roleAdmin = "ROLE_ADMIN";
		user.setRole(roleAdmin);

		User savedAdmin = userRepository.save(user);
		System.out.println("Saved user: " + savedAdmin.toString());
	}

	public List<User> listAll() {
		return userRepository.findAll();
	}

	public User saveUser(User user) {
		// Update the user's reset token if it exists
		User existingUser = userRepository.findById(user.getId()).orElse(null);
		if (existingUser != null && existingUser.getResetToken() != null) {
			user.setResetToken(existingUser.getResetToken());
		}
		return userRepository.save(user);
	}

	public User getUserByResetToken(String resetToken) {
		return userRepository.findByResetToken(resetToken);
	}
}