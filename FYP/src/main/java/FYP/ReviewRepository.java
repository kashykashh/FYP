/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-Jul-27 12:43:07 AM

 */
package FYP;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author 21033239
 *
 */

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

	List<Review> findBySellerId(Long sellerId);
}
