/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2022-Dec-02 8:47:30 AM

 */
package FYP;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author 21033239
 *
 */
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

	List<Item> findByNameContainingIgnoreCase(String name);

	Optional<Item> findById(Long itemId);

	List<Item> findByQuantityLessThanEqual(Integer maxQuantity);

	List<Item> findByCategoryId(Long categoryId);

	List<Item> findByUserNameContainingIgnoreCase(String userName);

	List<Item> findByUser(User user);
}