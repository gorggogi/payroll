function myFunction(dropdownId) {
    document.getElementById(dropdownId).classList.toggle("show");
}


window.onclick = function(event) {
    if (!event.target.matches('.dropbtn')) {
        var dropdowns = document.getElementsByClassName("dropdown-content");
        var i;
        for (i = 0; i < dropdowns.length; i++) {
            var openDropdown = dropdowns[i];
            if (openDropdown.classList.contains('show')) {
                openDropdown.classList.remove('show');
            }
        }
    }
}

let allEmployees = [];
let searchQuery = '';
let sortBy = 'lastName';
let sortOrder = 'asc';

window.addEventListener('load', function(){

    loadEmployees();

    document.getElementById('searchInput').addEventListener('keydown', function(event){
        if (event.key === 'Enter'){
            event.preventDefault();
            searchQuery = this.value.trim().toLowerCase();
            filterAndDisplayEmployees();
        }

    })

    document.getElementById('searchButton').addEventListener('click', function(){

        searchQuery = document.getElementById('serachInput').value.trim().toLowerCase();
        filterAndDisplayEmployees();

    })

})

document.getElementById('searchButton').addEventListener('click', function(){
    searchQuery = document.getElementById('searchInput').value.trim().toLowerCase();
    filterAndDisplayEmployees();
})

function loadEmployees(){

    const url = '/api/employees';

    console.log('Fetching employees from:', url);

    fetch(url)

    .then(response =>{
        if (!response.ok){
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return response.json();
    })

    .then(employees =>{

        console.log('Received employees:', employees);
        allEmployees = employees;
        filterAndDisplayEmployees();
    
    })

    .catch(error => {
        console.error('Error loading employees:', error);
        document.getElementById('employeeContainer').innerHTML = 
            `<p style="color: red;">Error loading employees: ${error.message}</p>`;

    });


}

function filterAndDisplayEmployees(){

    let filtered = allEmployees;

    if (searchQuery) {
        filtered = allEmployees.filter(emp => {
            return (
                (emp.firstName && emp.firstName.toLowerCase().includes(searchQuery)) ||
                (emp.lastName && emp.lastName.toLowerCase().includes(searchQuery)) ||
                (emp.middleName && emp.middleName.toLowerCase().includes(searchQuery)) ||
                (emp.employeeNumber && emp.employeeNumber.toString().toLowerCase().includes(searchQuery)) ||
                (emp.email && emp.email.toLowerCase().includes(searchQuery)) ||
                (emp.department && emp.department.departmentName && emp.department.departmentName.toLowerCase().includes(searchQuery)) ||
                (emp.position && emp.position.positionName && emp.position.positionName.toLowerCase().includes(searchQuery)) ||
                (emp.employmentStatus && emp.employmentStatus.toLowerCase().includes(searchQuery)) ||
                (emp.employmentType && emp.employmentType.toLowerCase().includes(searchQuery)) 
            );
        });
       
    }

    displayEmployees(filtered);
}

function sortEmployees(employees) {
    return employees.sort((a, b) => {
        let valueA, valueB;
        
        if (sortBy === 'firstName') {
            valueA = a.firstName?.toLowerCase() || '';
            valueB = b.firstName?.toLowerCase() || '';
        } else if (sortBy === 'lastName') {
            valueA = a.lastName?.toLowerCase() || '';
            valueB = b.lastName?.toLowerCase() || '';
        }
        
        if (valueA < valueB) {
            return sortOrder === 'asc' ? -1 : 1;
        }
        if (valueA > valueB) {
            return sortOrder === 'asc' ? 1 : -1;
        }
        return 0;
    });
}

function setSortBy(field) {
    sortBy = field;
   
}

function setSortOrder(order) {
    sortOrder = order;

}

function displayEmployees(employees) {
    const container = document.getElementById('employeeContainer');
    const infoContainer = document.getElementById('searchInfoContainer');

    if (!employees || employees.length === 0) {
        infoContainer.innerHTML = '';
        container.innerHTML = '<p>No employees found.</p>';
        return;
    }

    const infoHTML = `<div class="search-info"><p><i class="fa-solid fa-users"></i><strong>${employees.length}</strong> employee${employees.length !== 1 ? 's' : ''} found</p></div>`;
    infoContainer.innerHTML = infoHTML;

    const cardsHTML = employees.map(emp => `
            <div class="employee-card" data-id="${emp.employeeId}" onclick="showEmployee(${emp.employeeId})">
                <div class="employee-header">
                    <h3 class="employee-name">${emp.firstName} ${emp.middleName || ''} ${emp.lastName}</h3>
                    <span class="employee-status ${(emp.employmentStatus || '') === 'Terminated' ? 'status-terminated' : ''}">${emp.employmentStatus}</span>
                </div>
                <div class="employee-details">
                    <p><strong>Employee #:</strong> ${emp.employeeNumber}</p>
                    <p><strong>Email:</strong> ${emp.email || 'N/A'}</p>
                    <p><strong>Contact:</strong> ${emp.contactNumber || 'N/A'}</p>
                    <p><strong>Department:</strong> ${emp.department ? emp.department.departmentName : 'N/A'}</p>
                    <p><strong>Position:</strong> ${emp.position ? emp.position.positionName : 'N/A'}</p>
                    <p><strong>Type:</strong> ${emp.employmentType}</p>
                    <p><strong>Salary:</strong> ₱${emp.basicSalary ? emp.basicSalary.toLocaleString() : 'N/A'}</p>
                    <p><strong>Date Hired:</strong> ${emp.dateHired || 'N/A'}</p>
                    <a href="/admin/attendance?empId=${emp.employeeId}" class="card-link-attendance" onclick="event.stopPropagation()" title="View attendance"><i class="fa-solid fa-calendar-check"></i> View attendance</a>
                </div>
            </div>
    `).join('');
    container.innerHTML = cardsHTML;
}

function showEmployee(employeeId) {
    fetch(`/api/employees/${employeeId}`)
        .then(res => {
            if (!res.ok) throw new Error('Employee not found');
            return res.json();
        })
        .then(emp => renderEmployeeDetail(emp))
        .catch(err => alert('Error loading employee: ' + err.message));
}

function renderEmployeeDetail(emp) {
    // Remove existing modal if any
    const existing = document.getElementById('employeeModal');
    if (existing) existing.remove();

    // Fetch departments and positions in parallel to populate selects
    Promise.all([
        fetch('/api/departments').then(r => { if (!r.ok) throw new Error('Failed to load departments'); return r.json(); }),
        fetch('/api/positions').then(r => { if (!r.ok) throw new Error('Failed to load positions'); return r.json(); })
    ])
    .then(([departments, positions]) => {
        const modal = document.createElement('div');
        modal.id = 'employeeModal';
        modal.className = 'employee-modal';

        // build department options
        const deptOptions = departments.map(d => `
            <option value="${d.departmentId}" ${emp.department && emp.department.departmentId === d.departmentId ? 'selected' : ''}>
                ${d.departmentName}
            </option>
        `).join('');

        // build position options
        const posOptions = positions.map(p => `
            <option value="${p.positionId}" ${emp.position && emp.position.positionId === p.positionId ? 'selected' : ''}>
                ${p.positionName}
            </option>
        `).join('');

        modal.innerHTML = `
            <div class="employee-modal-content">
                <button class="close-btn" id="closeEmployeeModal">×</button>
                <h2>Edit Employee: ${emp.firstName} ${emp.lastName}</h2>
                <form id="employeeDetailForm">
                    <input type="hidden" name="employeeId" value="${emp.employeeId}" />
                    <label>First name: <input name="firstName" value="${emp.firstName || ''}" /></label>
                    <label>Middle name: <input name="middleName" value="${emp.middleName || ''}" /></label>
                    <label>Last name: <input name="lastName" value="${emp.lastName || ''}" /></label>
                    <label>Email: <input type="email" name="email" value="${emp.email || ''}" /></label>
                    <label>Contact: <input name="contactNumber" value="${emp.contactNumber || ''}" /></label>
                    <label>Address: <input name="address" value="${emp.address || ''}" /></label>
                    <label>Employment Status: <input name="employmentStatus" value="${emp.employmentStatus || ''}" /></label>
                    <label>Employment Type: <input name="employmentType" value="${emp.employmentType || ''}" /></label>
                    <label>Pay Type: <select name="payType">
                        <option value="monthly" ${(emp.payType || '').toLowerCase() === 'monthly' ? 'selected' : ''}>Monthly</option>
                        <option value="biweekly" ${(emp.payType || '').toLowerCase() === 'biweekly' ? 'selected' : ''}>Biweekly</option>
                        <option value="daily" ${(emp.payType || '').toLowerCase() === 'daily' ? 'selected' : ''}>Daily</option>
                        <option value="hourly" ${(emp.payType || '').toLowerCase() === 'hourly' ? 'selected' : ''}>Hourly</option>
                    </select></label>
                    <label>Basic Salary: <input type="number" name="basicSalary" value="${emp.basicSalary || ''}" /></label>
                    <label>Department: <select name="departmentId">
                        <option value="">-- Select Department --</option>
                        ${deptOptions}
                    </select></label>
                    <label>Position: <select name="positionId">
                        <option value="">-- Select Position --</option>
                        ${posOptions}
                    </select></label>
                    <label>TIN: <input name="tin" value="${emp.tin || ''}" /></label>
                    <label>SSS: <input name="sssNumber" value="${emp.sssNumber || ''}" /></label>
                    <label>Philhealth: <input name="philhealthNumber" value="${emp.philhealthNumber || ''}" /></label>
                    <label>Pagibig: <input name="pagibigNumber" value="${emp.pagibigNumber || ''}" /></label>
                    <label>Bank Account: <input name="bank_Account" value="${emp.bank_Account || ''}" /></label>
                    <div class="form-actions">
                        <a href="/admin/attendance?empId=${emp.employeeId}" class="btn-view-attendance" target="_blank"><i class="fa-solid fa-calendar-check"></i> View Attendance</a>
                        <button type="button" id="resetPasswordBtn" class="btn-reset-pw"><i class="fa-solid fa-key"></i> Reset Password</button>
                        <button type="button" id="saveEmployeeBtn" class="btn-save-emp"><i class="fa-solid fa-floppy-disk"></i>Save</button>
                    </div>
                </form>
            </div>
        `;

        document.body.appendChild(modal);

        document.getElementById('closeEmployeeModal').addEventListener('click', closeEmployeeModal);
        document.getElementById('saveEmployeeBtn').addEventListener('click', function() { saveEmployee(emp.employeeId); });
        document.getElementById('resetPasswordBtn').addEventListener('click', function() { resetEmployeePassword(emp.employeeId, emp.firstName + ' ' + emp.lastName); });
    })
    .catch(err => {
        alert('Error loading data for form: ' + err.message);
    });
}

function closeEmployeeModal() {
    const m = document.getElementById('employeeModal');
    if (m) m.remove();
}

function resetEmployeePassword(employeeId, employeeName) {
    var newPassword = prompt('Enter new password for ' + employeeName + ' (min 6 characters):');
    if (newPassword === null) return;
    if (newPassword.length < 6) {
        alert('Password must be at least 6 characters.');
        return;
    }
    var confirmPassword = prompt('Confirm new password:');
    if (confirmPassword !== newPassword) {
        alert('Passwords do not match.');
        return;
    }
    fetch('/admin/api/employees/' + employeeId + '/reset-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ newPassword: newPassword })
    })
    .then(function(res) {
        if (res.ok) return res.json();
        if (res.status === 404) throw new Error('User account not found for this employee.');
        return res.json().then(function(data) { throw new Error(data.error || 'Failed to reset password'); });
    })
    .then(function() {
        alert('Password reset successfully.');
    })
    .catch(function(err) {
        alert(err.message || 'Error resetting password');
    });
}

function saveEmployee(employeeId) {
    const form = document.getElementById('employeeDetailForm');
    const formData = new FormData(form);
    const payload = {
        employeeId: employeeId,
        firstName: formData.get('firstName'),
        middleName: formData.get('middleName'),
        lastName: formData.get('lastName'),
        email: formData.get('email'),
        contactNumber: formData.get('contactNumber'),
        address: formData.get('address'),
        employmentStatus: formData.get('employmentStatus'),
        employmentType: formData.get('employmentType'),
        payType: formData.get('payType'),
        basicSalary: formData.get('basicSalary') ? Number(formData.get('basicSalary')) : null,
        tin: formData.get('tin'),
        sssNumber: formData.get('sssNumber'),
        philhealthNumber: formData.get('philhealthNumber'),
        pagibigNumber: formData.get('pagibigNumber'),
        bank_Account: formData.get('bank_Account')
    };

    const deptId = formData.get('departmentId');
    if (deptId) payload.department = { departmentId: Number(deptId) };
    const posId = formData.get('positionId');
    if (posId) payload.position = { positionId: Number(posId) };

    fetch(`/api/employees/${employeeId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
    .then(res => {
        if (!res.ok) throw new Error('Failed to save');
        return res.json();
    })
    .then(saved => {
        alert('Employee saved');
        closeEmployeeModal();
        loadEmployees();
    })
    .catch(err => alert('Error saving employee: ' + err.message));
}

function toggleAdvancedFilters() {
    const panel = document.getElementById('advancedFilterSection');
    
    if (panel.style.display === 'none' || panel.style.display === '') {
        panel.style.display = 'flex';
    } else {
        panel.style.display = 'none';
    }
}

// Close modal when clicking on the backdrop (outside the content)
document.addEventListener('click', function(event) {
    const panel = document.getElementById('advancedFilterSection');
    const content = document.querySelector('.advanced-filter-section-content');
    
    if (panel && panel.style.display === 'flex' && event.target === panel) {
        panel.style.display = 'none';
    }
});

function applyAdvancedFilters(){
    
       
        const searchQuery = document.getElementById('searchInput').value.trim();
        

        const sortBy = document.getElementById('sortBy').value;
        const sortOrder = document.getElementById('sortOrder').value;
        

        const departmentId = document.getElementById('filterDepartment').value;
        const positionId = document.getElementById('filterPosition').value;
        const employmentStatus = document.getElementById('filterStatus').value;
        const employmentType = document.getElementById('filterType').value;
        const payType = document.getElementById('filterPayType').value;
        const minSalary = document.getElementById('filterMinSalary').value;
        const maxSalary = document.getElementById('filterMaxSalary').value;
        
   
        const params = new URLSearchParams();
        if (searchQuery) params.append('searchQuery', searchQuery);
        params.append('sortBy', sortBy);
        params.append('direction', sortOrder);
        
 
        if (departmentId) params.append('departmentId', departmentId);
        if (positionId) params.append('positionId', positionId);
        if (employmentStatus) params.append('employmentStatus', employmentStatus);
        if (employmentType) params.append('employmentType', employmentType);
        if (payType) params.append('payType', payType);
        if (minSalary) params.append('minSalary', minSalary);
        if (maxSalary) params.append('maxSalary', maxSalary);
        
   
        fetch(`/api/employees/filter?${params.toString()}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(employees => {
                console.log('Filter applied:', params.toString());
                allEmployees = employees;
                displayEmployees(employees);
            })
            .catch(error => {
                console.error('Error applying filters:', error);
                alert(`Error applying filters: ${error.message}`);
            });
    }
    
    function resetFilters(){
 
        document.getElementById('sortBy').value = 'lastName';
        document.getElementById('sortOrder').value = 'asc';
        document.getElementById('filterDepartment').value = '';
        document.getElementById('filterPosition').value = '';
        document.getElementById('filterStatus').value = '';
        document.getElementById('filterType').value = '';
        document.getElementById('filterPayType').value = '';
        document.getElementById('filterMinSalary').value = '';
        document.getElementById('filterMaxSalary').value = '';
        
     
        document.getElementById('searchInput').value = '';
        
        loadEmployees();
        
        console.log('All filters reset');
    }






