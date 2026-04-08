// Premium Tables JavaScript

function switchThemeTab(tabId) {
    // Hide all tab contents
    document.querySelectorAll('.tab-content').forEach(content => {
        content.style.display = 'none';
    });
    
    // Remove active class from all buttons
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Show selected tab content
    document.getElementById(tabId + '-tab').style.display = 'block';
    
    // Add active class to clicked button
    const activeBtn = Array.from(document.querySelectorAll('.tab-btn')).find(btn => btn.getAttribute('onclick').includes(tabId));
    if (activeBtn) activeBtn.classList.add('active');
    
    // Update the hidden input in the form so it persists on reload
    document.getElementById('activeTabField').value = tabId;
}

// SSS Modal Functions
function openSssModal(id = '', min = '', max = '', empShare = '', erShare = '') {
    document.getElementById('sssId').value = id;
    document.getElementById('sssModalTitle').innerText = id ? 'Edit SSS Bracket' : 'Add SSS Bracket';
    
    document.getElementById('sssRangeFrom').value = min;
    document.getElementById('sssRangeTo').value = max;
    document.getElementById('sssEmployeeShare').value = empShare;
    document.getElementById('sssEmployerShare').value = erShare;
    
    document.getElementById('sssModal').style.display = 'block';
}

function closeSssModal() {
    document.getElementById('sssModal').style.display = 'none';
}

// Tax Modal Functions
function openTaxModal(id = '', freq = 'SEMI_MONTHLY', min = '', max = '', rate = '', additional = '') {
    document.getElementById('taxId').value = id;
    document.getElementById('taxModalTitle').innerText = id ? 'Edit Tax Bracket' : 'Add Tax Bracket';
    
    document.getElementById('taxPayFrequency').value = freq;
    document.getElementById('taxCompensationFrom').value = min;
    document.getElementById('taxCompensationTo').value = max === 'null' || !max ? '' : max;
    document.getElementById('taxTaxRate').value = rate;
    document.getElementById('taxAdditionalTax').value = additional;
    
    document.getElementById('taxModal').style.display = 'block';
}

function closeTaxModal() {
    document.getElementById('taxModal').style.display = 'none';
}

// Click anywhere outside of modal to close
window.onclick = function(event) {
    const sssModal = document.getElementById('sssModal');
    const taxModal = document.getElementById('taxModal');
    
    if (event.target == sssModal) {
        sssModal.style.display = 'none';
    }
    if (event.target == taxModal) {
        taxModal.style.display = 'none';
    }
}

/* ==================== BULK EDIT AND DELETE LOGIC ==================== */

function toggleAllCheckboxes(source, tableId) {
    const checkboxes = document.querySelectorAll(`#${tableId} .row-checkbox`);
    checkboxes.forEach(cb => cb.checked = source.checked);
}
function toggleAllSssCheckboxes(source) {
    toggleAllCheckboxes(source, 'sssTable');
}

// ------ SSS BULK LOGIC ------
let isSssEditMode = false;
let isSssDeleteMode = false;

function toggleSssEditMode() {
    if (isSssDeleteMode) return; 
    
    const editBtn = document.getElementById('sssEditBtn');
    const deleteBtn = document.getElementById('sssDeleteBtn');
    const addBtn = document.getElementById('sssAddBtn');
    const rows = document.querySelectorAll('#sssTable tbody tr.data-row');
    
    if (!isSssEditMode) {
        // Enter Edit Mode
        isSssEditMode = true;
        editBtn.innerText = 'Save Changes';
        editBtn.style.backgroundColor = '#16a085'; // Custom success color
        editBtn.style.color = '#fff';
        deleteBtn.innerText = 'Cancel';
        deleteBtn.setAttribute('onclick', 'cancelSssEditMode()');
        addBtn.style.display = 'none';
        
        rows.forEach(row => {
            const cols = ['rangeFrom', 'rangeTo', 'employeeShare', 'employerShare'];
            cols.forEach(colPrefix => {
                const td = row.querySelector('.col-' + colPrefix);
                if (td) {
                    const val = td.getAttribute('data-val');
                    td.innerHTML = `<input type="number" step="0.01" class="form-control table-edit-input" data-col="${colPrefix}" value="${val !== 'null' ? val : ''}" style="width:100%; min-width:80px; padding: 5px;"/>`;
                }
            });
        });
    } else {
        saveSssBulkChanges();
    }
}

function cancelSssEditMode() {
    window.location.reload();
}

function saveSssBulkChanges() {
    const rows = document.querySelectorAll('#sssTable tbody tr.data-row');
    let payload = [];
    let year = document.getElementById('yearSelect').value;
    
    rows.forEach(row => {
        let obj = {
            sssId: row.getAttribute('data-id'),
            effectiveYear: year
        };
        const inputs = row.querySelectorAll('.table-edit-input');
        inputs.forEach(input => {
            let col = input.getAttribute('data-col');
            obj[col] = input.value === '' ? null : parseFloat(input.value);
        });
        payload.push(obj);
    });
    
    fetch('/admin/tables/premium/sss/save-bulk', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    }).then(res => {
        if (res.ok) window.location.reload();
        else alert('Error updating SSS brackets.');
    });
}

function toggleSssDeleteMode() {
    if (isSssEditMode) return;
    
    const editBtn = document.getElementById('sssEditBtn');
    const deleteBtn = document.getElementById('sssDeleteBtn');
    const addBtn = document.getElementById('sssAddBtn');
    
    if (!isSssDeleteMode) {
        // Enter Delete Mode
        isSssDeleteMode = true;
        deleteBtn.innerText = 'Confirm Deletion';
        editBtn.innerText = 'Cancel';
        editBtn.setAttribute('onclick', 'cancelSssDeleteMode()');
        addBtn.style.display = 'none';
        
        document.querySelectorAll('#sssTable .cb-col').forEach(td => td.style.display = 'table-cell');
    } else {
        confirmSssDelete();
    }
}

function cancelSssDeleteMode() {
    window.location.reload();
}

function confirmSssDelete() {
    const checkboxes = document.querySelectorAll('#sssTable .sss-checkbox:checked');
    if (checkboxes.length === 0) {
        window.location.reload();
        return;
    }
    
    if (!confirm(`Delete ${checkboxes.length} selected SSS brackets?`)) return;
    
    let ids = Array.from(checkboxes).map(cb => parseInt(cb.value));
    
    fetch('/admin/tables/premium/sss/delete-bulk', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(ids)
    }).then(res => {
        if (res.ok) window.location.reload();
        else alert('Error deleting SSS brackets.');
    });
}

// ------ TAX BULK LOGIC ------
let isTaxEditMode = false;
let isTaxDeleteMode = false;

function toggleTaxEditMode() {
    if (isTaxDeleteMode) return;
    
    const editBtn = document.getElementById('taxEditBtn');
    const deleteBtn = document.getElementById('taxDeleteBtn');
    const addBtn = document.getElementById('taxAddBtn');
    const rows = document.querySelectorAll('#tax-tab tbody tr.data-row');
    
    if (!isTaxEditMode) {
        isTaxEditMode = true;
        editBtn.innerText = 'Save Changes';
        editBtn.style.backgroundColor = '#16a085'; 
        editBtn.style.color = '#fff';
        deleteBtn.innerText = 'Cancel';
        deleteBtn.setAttribute('onclick', 'cancelTaxEditMode()');
        addBtn.style.display = 'none';
        
        rows.forEach(row => {
            const cols = ['compensationFrom', 'compensationTo', 'additionalTax', 'taxRate'];
            cols.forEach(colPrefix => {
                const td = row.querySelector('.col-' + colPrefix);
                if (td) {
                    const val = td.getAttribute('data-val');
                    td.innerHTML = `<input type="number" step="0.01" class="form-control table-edit-input" data-col="${colPrefix}" value="${val && val !== 'null' ? val : ''}" style="width:100%; min-width:80px; padding: 5px;"/>`;
                }
            });
        });
    } else {
        saveTaxBulkChanges();
    }
}

function cancelTaxEditMode() {
    window.location.reload();
}

function saveTaxBulkChanges() {
    let payload = [];
    let year = document.getElementById('yearSelect').value;
    
    function extractFromTable(tableId, freq) {
        const rows = document.querySelectorAll(`#${tableId} tbody tr.data-row`);
        rows.forEach(row => {
            let obj = {
                taxId: row.getAttribute('data-id'),
                effectiveYear: year,
                payFrequency: freq
            };
            const inputs = row.querySelectorAll('.table-edit-input');
            inputs.forEach(input => {
                let col = input.getAttribute('data-col');
                obj[col] = input.value === '' ? null : parseFloat(input.value);
            });
            payload.push(obj);
        });
    }
    
    extractFromTable('taxSemiTable', 'SEMI_MONTHLY');
    extractFromTable('taxMonthlyTable', 'MONTHLY');
    
    fetch('/admin/tables/premium/tax/save-bulk', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    }).then(res => {
        if (res.ok) window.location.reload();
        else alert('Error updating Tax brackets.');
    });
}

function toggleTaxDeleteMode() {
    if (isTaxEditMode) return;
    
    const editBtn = document.getElementById('taxEditBtn');
    const deleteBtn = document.getElementById('taxDeleteBtn');
    const addBtn = document.getElementById('taxAddBtn');
    
    if (!isTaxDeleteMode) {
        isTaxDeleteMode = true;
        deleteBtn.innerText = 'Confirm Deletion';
        editBtn.innerText = 'Cancel';
        editBtn.setAttribute('onclick', 'cancelTaxDeleteMode()');
        addBtn.style.display = 'none';
        
        document.querySelectorAll('#tax-tab .cb-col').forEach(td => td.style.display = 'table-cell');
    } else {
        confirmTaxDelete();
    }
}

function cancelTaxDeleteMode() {
    window.location.reload();
}

function confirmTaxDelete() {
    const checkboxes = document.querySelectorAll('#tax-tab .tax-checkbox:checked');
    if (checkboxes.length === 0) {
        window.location.reload();
        return;
    }
    
    if (!confirm(`Delete ${checkboxes.length} selected Tax brackets?`)) return;
    
    let ids = Array.from(checkboxes).map(cb => parseInt(cb.value));
    
    fetch('/admin/tables/premium/tax/delete-bulk', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(ids)
    }).then(res => {
        if (res.ok) window.location.reload();
        else alert('Error deleting Tax brackets.');
    });
}
