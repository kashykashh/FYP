/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-Jul-04 6:50:20 PM

 */
package FYP;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 21033239
 *
 */

@RestController
public class PaymentController {

	@PostMapping("/items/complete-payment")
	public ResponseEntity<String> completePayment(@RequestBody PaymentRequest paymentRequest) {

		if (paymentRequest.getOrderId() != null) {
			// Payment completed successfully
			return ResponseEntity.ok("Payment completed successfully");
		} else {
			// Payment failed
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Payment failed");
		}
	}

	static class PaymentRequest {
		private String orderId;

		public String getOrderId() {
			return orderId;
		}

		public void setOrderId(String orderId) {
			this.orderId = orderId;
		}
	}
}
