// deductions.js - Handles deduction modal logic for installment payments

document.addEventListener('DOMContentLoaded', function() {
    const monthsSelect = document.getElementById('installmentMonths');
    const amountInput = document.getElementById('amount');
    const totalAmountDisplay = document.getElementById('totalInstallmentAmount');

    if (monthsSelect && amountInput && totalAmountDisplay) {
        function updateTotal() {
            const months = parseInt(monthsSelect.value, 10);
            const amount = parseFloat(amountInput.value);
            if (!isNaN(months) && !isNaN(amount)) {
                totalAmountDisplay.textContent = (amount/months).toFixed(2);
            } else {
                totalAmountDisplay.textContent = '0.00';
            }
        }
        monthsSelect.addEventListener('change', updateTotal);
        amountInput.addEventListener('input', updateTotal);
        updateTotal();
    }
});
