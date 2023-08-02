/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-Feb-09 12:32:30 PM

 */
package FYP;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author 21033239
 *
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	List<CartItem> findByUserId(Long userId);

	CartItem findByUserAndItemId(User user, Long itemId);

	List<CartItem> findByItem(Item item);

	List<CartItem> findByUser(User user);
}