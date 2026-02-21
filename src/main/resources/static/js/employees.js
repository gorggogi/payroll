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

    document.getElementById('filterToggleBtn').addEventListener('click', toggleAdvancedFilters);
    document.getElementById('applyFiltersBtn').addEventListener('click', applyAdvancedFilters);
    document.getElementById('resetFiltersBtn').addEventListener('click', resetFilters);

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

function displayEmployeeListInfo(employees) {
    const infoContainer = document.getElementById('employeeListInfo');
    
    if (!employees || employees.length === 0) {
        infoContainer.innerHTML = '<div class="search-info"><p>No employees found.</p></div>';
        return;
    }

    const infoHTML = `<div class="search-info"><p><strong>${employees.length}</strong> employee${employees.length !== 1 ? 's' : ''} found</p></div>`;
    infoContainer.innerHTML = infoHTML;
}

function displayEmployeeCards(employees) {
    const container = document.getElementById('employeeContainer');

    if (!employees || employees.length === 0) {
        container.innerHTML = '<p>No employee cards to display.</p>';
        return;
    }

    const cardsHTML = employees.map(emp => `
            <div class="employee-card">
                <div class="employee-header">
                    <h3 class="employee-name">${emp.firstName} ${emp.middleName || ''} ${emp.lastName}</h3>
                    <span class="employee-status">${emp.employmentStatus}</span>
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
                </div>
            </div>
    `).join('');
    container.innerHTML = cardsHTML;
}

function displayEmployees(employees) {
    displayEmployeeListInfo(employees);
    displayEmployeeCards(employees);
}

function toggleAdvancedFilters() {
    const panel = document.getElementById('advancedFilterSection');
    const btn = document.getElementById('filterToggleBtn');
    
    if (panel.style.display === 'none') {
  
        panel.style.display = 'block';
        btn.innerHTML = '<i class="fa-solid fa-chevron-up"></i> Hide Filters';
  
    } else {
     
        panel.style.display = 'none';
        btn.innerHTML = '<i class="fa-solid fa-chevron-down"></i> Show Filters';
      
    }
}

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






