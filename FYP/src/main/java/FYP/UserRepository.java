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

import org.springframework.data.jpa.repository.JpaRepository;

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
}
