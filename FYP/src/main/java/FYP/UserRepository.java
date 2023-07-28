/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-Jan-18 6:49:43 PM

 */
package FYP;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * @author 21033239
 *
 */
public interface UserRepository extends JpaRepository<User, Long> {

	public User findByUsername(String username);

	public User getById(Long loggedInUserId);

	public User findByName(String userName);

	public User findByEmail(String email);

	public User findByResetToken(String resetToken);

	@Query("SELECT u FROM User u WHERE u.banned = true")
	List<User> findByBannedTrue();

	List<User> findAll();
}
