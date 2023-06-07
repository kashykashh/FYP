/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-May-31 1:49:11 AM

 */
package FYP;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 21033239
 *
 */

@Service
public class OrderItemService {

	private final OrderItemRepository orderItemRepository;

	@Autowired
	public OrderItemService(OrderItemRepository orderItemRepository) {
		this.orderItemRepository = orderItemRepository;
	}

	public List<OrderItem> retrieveOrderItems() {
		return orderItemRepository.findAll();
	}
	
	public OrderItem retrieveOrderItemWithTimestamp() {
        return orderItemRepository.findByTimestampNotNull();
    }
}
