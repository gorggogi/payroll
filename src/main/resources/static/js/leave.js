

function validateLeaveForm() {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    const leaveType = document.getElementById('leaveTypeId').value;
    const reason = document.getElementById('reason').value.trim();
    
   
    if (!leaveType) {
        alert('Please select a leave type');
        return false;
    }
    
  
    if (!startDate || !endDate) {
        alert('Please select both start and end dates');
        return false;
    }
    
    
    const start = new Date(startDate);
    const end = new Date(endDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    
    if (start < today) {
        alert('Start date cannot be in the past');
        return false;
    }
    
    
    if (end < start) {
        alert('End date cannot be before start date');
        return false;
    }
    
    
    const oneYearFromNow = new Date();
    oneYearFromNow.setFullYear(oneYearFromNow.getFullYear() + 1);
    
    if (start > oneYearFromNow) {
        alert('Leave requests cannot be more than 1 year in advance');
        return false;
    }
    
    
    if (reason.length < 10) {
        alert('Please provide a more detailed reason (at least 10 characters)');
        return false;
    }
    
    if (reason.length > 500) {
        alert('Reason is too long (maximum 500 characters)');
        return false;
    }
    
    
    const days = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1;
    
    
    return confirm(`You are requesting ${days} day(s) of leave from ${formatDate(start)} to ${formatDate(end)}. Continue?`);
}


function formatDate(date) {
    const options = { year: 'numeric', month: 'short', day: 'numeric' };
    return date.toLocaleDateString('en-US', options);
}


document.addEventListener('DOMContentLoaded', function() {
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    
    if (startDateInput && endDateInput) {
        
        const today = new Date().toISOString().split('T')[0];
        startDateInput.setAttribute('min', today);
        endDateInput.setAttribute('min', today);
        
        
        startDateInput.addEventListener('change', function() {
            endDateInput.setAttribute('min', this.value);
            
            
            if (endDateInput.value && endDateInput.value < this.value) {
                endDateInput.value = '';
            }
        });
    }
    
    
    const alerts = document.querySelectorAll('.alert-success, .alert-error');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s ease';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });
});
