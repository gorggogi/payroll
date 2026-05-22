

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


// ==================== REQUEST LEAVE MODAL FUNCTIONS ====================
function openRequestLeaveModal() {
    document.getElementById('requestLeaveModal').classList.add('show');
}

function closeRequestLeaveModal() {
    document.getElementById('requestLeaveModal').classList.remove('show');
    // Optionally reset the form
    const form = document.querySelector('#requestLeaveModal form');
    if (form) form.reset();
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
    
    // Close modals when clicking outside of them
    const editModal = document.getElementById('editLeaveModal');
    if (editModal) {
        editModal.addEventListener('click', function(event) {
            if (event.target === editModal) {
                closeEditLeaveModal();
            }
        });
    }
    const requestModal = document.getElementById('requestLeaveModal');
    if (requestModal) {
        requestModal.addEventListener('click', function(event) {
            if (event.target === requestModal) {
                closeRequestLeaveModal();
            }
        });
    }
    
    // Attach event listeners to edit buttons
    document.querySelectorAll('.edit-leave-btn').forEach(button => {
        button.addEventListener('click', function() {
            const leaveId = this.getAttribute('data-leave-id');
            const leaveType = this.getAttribute('data-leave-type');
            const startDate = this.getAttribute('data-start-date');
            const endDate = this.getAttribute('data-end-date');
            const reason = this.getAttribute('data-reason');
            const status = this.getAttribute('data-status');
            
            openEditLeaveModal(leaveId, leaveType, startDate, endDate, reason, status);
        });
    });

    document.querySelectorAll('.edit-undertime-btn').forEach(button => {
        button.addEventListener('click', function() {
            const undertimeId = this.getAttribute('data-undertime-id');
            const requestDate = this.getAttribute('data-request-date');
            const totalHours = this.getAttribute('data-total-hours');
            const reason = this.getAttribute('data-reason');
            const status = this.getAttribute('data-status');
            
            openEditUndertimeModal(undertimeId, requestDate, totalHours, reason, status);
        });
    });

    const editUndertimeModal = document.getElementById('editUndertimeModal');
    if (editUndertimeModal) {
        editUndertimeModal.addEventListener('click', function(event) {
            if (event.target === editUndertimeModal) {
                closeEditUndertimeModal();
            }
        });
    }
});

// ==================== EDIT LEAVE MODAL FUNCTIONS ====================

function openEditLeaveModal(leaveRequestId, leaveTypeId, startDate, endDate, reason, status) {
    // Populate form fields
    document.getElementById('leaveRequestId').value = leaveRequestId;
    document.getElementById('editLeaveType').value = leaveTypeId;
    document.getElementById('editStartDate').value = startDate;
    document.getElementById('editEndDate').value = endDate;
    document.getElementById('editReason').value = reason;
    
    // Check if status is Pending - enable/disable fields accordingly
    const isEditable = status === 'Pending';
    
    const leaveTypeSelect = document.getElementById('editLeaveType');
    const startDateInput = document.getElementById('editStartDate');
    const endDateInput = document.getElementById('editEndDate');
    const reasonTextarea = document.getElementById('editReason');
    const submitBtn = document.querySelector('#editLeaveForm button[type="submit"]');
    
    leaveTypeSelect.disabled = !isEditable;
    startDateInput.disabled = !isEditable;
    endDateInput.disabled = !isEditable;
    reasonTextarea.disabled = !isEditable;
    submitBtn.disabled = !isEditable;
    
    // Show modal
    document.getElementById('editLeaveModal').classList.add('show');
}

function closeEditLeaveModal() {
    document.getElementById('editLeaveModal').classList.remove('show');
    document.getElementById('editLeaveForm').reset();
}

function submitEditLeaveForm(event) {
    event.preventDefault();
    
    const leaveRequestId = document.getElementById('leaveRequestId').value;
    const leaveTypeId = document.getElementById('editLeaveType').value;
    const startDate = document.getElementById('editStartDate').value;
    const endDate = document.getElementById('editEndDate').value;
    const reason = document.getElementById('editReason').value.trim();
    
    // Validate form
    if (!leaveTypeId) {
        alert('Please select a leave type');
        return;
    }
    
    if (!startDate || !endDate) {
        alert('Please select both start and end dates');
        return;
    }
    
    const start = new Date(startDate);
    const end = new Date(endDate);
    
    if (end < start) {
        alert('End date cannot be before start date');
        return;
    }
    
    if (reason.length < 10) {
        alert('Please provide a more detailed reason (at least 10 characters)');
        return;
    }
    
    if (reason.length > 500) {
        alert('Reason is too long (maximum 500 characters)');
        return;
    }
    
    const attachmentInput = document.getElementById('editLeaveAttachment');
    const attachment = attachmentInput.files[0];
    
    // Submit the form
    const formData = new FormData();
    formData.append('leaveTypeId', leaveTypeId);
    formData.append('startDate', startDate);
    formData.append('endDate', endDate);
    formData.append('reason', reason);
    if (attachment) {
        formData.append('attachment', attachment);
    }
    
    fetch(`/api/employee/leave/${leaveRequestId}`, {
        method: 'PUT',
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to update leave request');
        }
        return response.json();
    })
    .then(data => {
        alert('Leave request updated successfully!');
        closeEditLeaveModal();
        // Reload the page to see the changes
        window.location.reload();
    })
    .catch(error => {
        alert('Error: ' + error.message);
    });
}

// ==================== EDIT UNDERTIME MODAL FUNCTIONS ====================

function openEditUndertimeModal(undertimeRequestId, requestDate, totalHours, reason, status) {
    document.getElementById('editUndertimeId').value = undertimeRequestId;
    document.getElementById('editUndertimeDate').value = requestDate;
    document.getElementById('editUndertimeHours').value = totalHours;
    document.getElementById('editUndertimeReason').value = reason;
    
    const isEditable = status === 'Pending';
    
    const dateInput = document.getElementById('editUndertimeDate');
    const hoursInput = document.getElementById('editUndertimeHours');
    const reasonTextarea = document.getElementById('editUndertimeReason');
    const submitBtn = document.querySelector('#editUndertimeForm button[type="submit"]');
    
    dateInput.disabled = !isEditable;
    hoursInput.disabled = !isEditable;
    reasonTextarea.disabled = !isEditable;
    submitBtn.disabled = !isEditable;
    
    document.getElementById('editUndertimeModal').classList.add('show');
}

function closeEditUndertimeModal() {
    document.getElementById('editUndertimeModal').classList.remove('show');
    document.getElementById('editUndertimeForm').reset();
}

function submitEditUndertimeForm(event) {
    event.preventDefault();
    
    const undertimeRequestId = document.getElementById('editUndertimeId').value;
    const requestDate = document.getElementById('editUndertimeDate').value;
    const totalHours = document.getElementById('editUndertimeHours').value;
    const reason = document.getElementById('editUndertimeReason').value.trim();
    
    if (!requestDate) {
        alert('Please select a date');
        return;
    }
    
    if (!totalHours || totalHours < 0.5) {
        alert('Please enter valid hours');
        return;
    }
    
    if (reason.length < 10) {
        alert('Please provide a more detailed reason (at least 10 characters)');
        return;
    }
    
    const attachmentInput = document.getElementById('editUndertimeAttachment');
    const attachment = attachmentInput.files[0];
    
    const formData = new FormData();
    formData.append('requestDate', requestDate);
    formData.append('totalHours', totalHours);
    formData.append('reason', reason);
    if (attachment) {
        formData.append('attachment', attachment);
    }
    
    fetch(`/api/employee/undertime/${undertimeRequestId}`, {
        method: 'PUT',
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to update undertime request');
        }
        return response.json();
    })
    .then(data => {
        alert('Undertime request updated successfully!');
        closeEditUndertimeModal();
        window.location.reload();
    })
    .catch(error => {
        alert('Error: ' + error.message);
    });
}
