// Function to extract currency code from the URL
function getCurrencyFromUrl() {
	const urlParams = new URLSearchParams(window.location.search);
	return urlParams.get('currency');
}

function handleCurrencySelection() {
	const selectedCurrency = getCurrencyFromUrl();
	if (selectedCurrency) {
		fetchExchangeRate(selectedCurrency);
		// Update the selected currency code in the navbar link
		document.getElementById('selectedCurrencyCode').textContent = selectedCurrency;
		// Save the selected currency to localStorage
		localStorage.setItem('selectedCurrency', selectedCurrency);
	} else {
		// Check if there's a previously saved currency and use it
		const savedCurrency = localStorage.getItem('selectedCurrency');
		if (savedCurrency) {
			fetchExchangeRate(savedCurrency);
			document.getElementById('selectedCurrencyCode').textContent = savedCurrency;
		}
	}
}

// Function to fetch exchange rate for the selected currency
function fetchExchangeRate(currencyCode) {
	const apiUrl = 'https://openexchangerates.org/api/latest.json?app_id=84eb9f67b6f3484da9c8a4f1916f8580&base=SGD';

	$.getJSON(apiUrl)
		.done(function(json) {
			const exchangeRate = json.rates[currencyCode];
			console.log(`Exchange rate for ${currencyCode}: ${exchangeRate}`);

			// Call the updateItemPrices function to update item prices on the server
			updateItemPrices(exchangeRate);

		})
		.fail(function(xhr, status, error) {
			console.error('Error fetching exchange rate:', error);
		});
}

// Function to update item prices on the server
function updateItemPrices(exchangeRate) {
	$.ajax({
		url: '/items/updatePrices',
		method: 'GET',
		data: { exchangeRate: exchangeRate },
		success: function(data) {
			// Optional: Handle success response if needed
			console.log('Item prices updated successfully.');
			// Reload the page to see the updated item prices

		},
		error: function(xhr, status, error) {
			// Optional: Handle error response if needed
			console.error('Error updating item prices:', error);
		}
	});
}

function updateCurrencyOptions() {
	const apiUrl = 'https://openexchangerates.org/api/currencies.json';
	$.getJSON(apiUrl)
		.done(function(json) {
			const currencyOptions = document.getElementById('currencyOptions');
			currencyOptions.innerHTML = '';

			for (const currencyCode in json) {
				const currencyName = json[currencyCode];
				const option = document.createElement('li');
				option.innerHTML = `<a class="dropdown-item" href="#">${currencyCode} - ${currencyName}</a>`;
				option.addEventListener('click', function() {
					// Update the window.location to navigate to the new URL with currency parameter
					window.location.href = `/?currency=${currencyCode}`;
				});
				currencyOptions.appendChild(option);
			}

			// Fetch exchange rate for the selected currency on page load
			handleCurrencySelection();

			// Set the selected currency based on the value in localStorage
			const savedCurrency = localStorage.getItem('selectedCurrency');
			if (savedCurrency) {
				document.getElementById('selectedCurrencyCode').textContent = savedCurrency;
			}
		})
		.fail(function(xhr, status, error) {
			console.error('Error fetching currency data:', error);
		});
}

// Attach event listener to the currency dropdown menu to update options on click
document.getElementById('currencyDropdown').addEventListener('click', updateCurrencyOptions);

// Fetch currency options and exchange rate on page load
document.addEventListener('DOMContentLoaded', updateCurrencyOptions);