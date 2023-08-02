/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-Feb-09 12:35:43 PM

 */
package FYP;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author 21033239
 *
 */

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

	OrderItem findByTimestampNotNull();
	
	List<OrderItem> findByUserId(Long loggedInUserId);
	
	List<OrderItem> findByItem(Item item);
}
