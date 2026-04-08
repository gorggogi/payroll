// Ensure all deduction modals are hidden by default on page load
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.deduction-modal').forEach(function(modal) {
        modal.style.display = 'none';
    });
});
// Edit deduction modal logic
window.openEditDeductionModal = function(btn) {
    // Get data attributes
    const id = btn.getAttribute('data-id');
    const employeeId = btn.getAttribute('data-employee-id');
    const deductionId = btn.getAttribute('data-deduction-id');
    const amount = btn.getAttribute('data-amount');
    const recurring = btn.getAttribute('data-recurring') === 'true';
    const start = btn.getAttribute('data-start');
    const end = btn.getAttribute('data-end');

    // Set form values
    document.getElementById('editDeductionId').value = id;
    document.getElementById('editEmployeeId').value = employeeId;
    document.getElementById('editDeductionTypeId').value = deductionId;
    document.getElementById('editAmount').value = amount;
    document.getElementById('editRecurring').checked = recurring;
    document.getElementById('editStartDate').value = start;
    document.getElementById('editEndDate').value = end;

    // Show modal
    document.getElementById('modalEditDeduction').style.display = 'block';
}

window.closeModal = function(modalId) {
    document.getElementById(modalId).style.display = 'none';
}
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
