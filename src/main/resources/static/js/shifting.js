function selectTemplate(templateId) {
    if (!templateId) return;
    var monthInput = document.getElementById('scheduleMonthPicker');
    var yearInput = document.getElementById('scheduleYearPicker');
    var monthVal = monthInput ? monthInput.value : '';
    var yearVal = yearInput ? yearInput.value : '';
    var qs = '?scheduleMonth=' + encodeURIComponent(monthVal) +
             '&scheduleYear=' + encodeURIComponent(yearVal) +
             '&templateId=' + encodeURIComponent(templateId);
    window.location.href = '/admin/attendance/shifts' + qs;
}

function openEditScheduleModal() {
    var m = document.getElementById('editScheduleModal');
    if (!m) return;
    m.style.display = 'block';
    m.setAttribute('aria-hidden', 'false');
    document.querySelectorAll('#editScheduleModal input[data-weekly-rest][data-dow]').forEach(applyWeeklyRestDisabled);
}

function closeEditScheduleModal() {
    var m = document.getElementById('editScheduleModal');
    if (!m) return;
    m.style.display = 'none';
    m.setAttribute('aria-hidden', 'true');
}

function openAssignEmployeesModal() {
    var m = document.getElementById('assignEmployeesModal');
    if (!m) return;
    m.style.display = 'block';
    m.setAttribute('aria-hidden', 'false');
    var f = document.getElementById('employeeAssignFilter');
    if (f) { f.value = ''; filterAssignEmployees(''); }
}

function closeAssignEmployeesModal() {
    var m = document.getElementById('assignEmployeesModal');
    if (!m) return;
    m.style.display = 'none';
    m.setAttribute('aria-hidden', 'true');
}

function filterAssignEmployees(q) {
    var term = (q || '').trim().toLowerCase();
    document.querySelectorAll('.employee-assign-row').forEach(function (row) {
        var hay = row.getAttribute('data-search') || '';
        if (!term || hay.indexOf(term) >= 0) {
            row.classList.remove('hidden');
        } else {
            row.classList.add('hidden');
        }
    });
}

function openNewTemplateModal() {
    var m = document.getElementById('newTemplateModal');
    if (!m) return;
    m.style.display = 'block';
    m.setAttribute('aria-hidden', 'false');
}

function closeNewTemplateModal() {
    var m = document.getElementById('newTemplateModal');
    if (!m) return;
    m.style.display = 'none';
    m.setAttribute('aria-hidden', 'true');
}

function openShiftDetailsModal() {
    var m = document.getElementById('shiftDetailsModal');
    if (!m) return;
    m.style.display = 'block';
    m.setAttribute('aria-hidden', 'false');
}

function closeShiftDetailsModal() {
    var m = document.getElementById('shiftDetailsModal');
    if (!m) return;
    m.style.display = 'none';
    m.setAttribute('aria-hidden', 'true');
}

// close if user clicks overlay (not modal content)
document.addEventListener('click', function (e) {
    var newTpl = document.getElementById('newTemplateModal');
    if (newTpl && e.target === newTpl) closeNewTemplateModal();
    var detailsM = document.getElementById('shiftDetailsModal');
    if (detailsM && e.target === detailsM) closeShiftDetailsModal();
    var editSch = document.getElementById('editScheduleModal');
    if (editSch && e.target === editSch) closeEditScheduleModal();
    var assignM = document.getElementById('assignEmployeesModal');
    if (assignM && e.target === assignM) closeAssignEmployeesModal();
});

function applyWeeklyRestDisabled(cb) {
    if (!cb || !cb.getAttribute('data-dow')) return;
    var dow = cb.getAttribute('data-dow');
    var inn = document.getElementById('weekly_in_' + dow);
    var out = document.getElementById('weekly_out_' + dow);
    if (!inn || !out) return;
    inn.disabled = cb.checked;
    out.disabled = cb.checked;
}

function onWeeklyRestChange(cb) {
    applyWeeklyRestDisabled(cb);
    if (cb.checked) {
        var dow = cb.getAttribute('data-dow');
        var inn = document.getElementById('weekly_in_' + dow);
        var out = document.getElementById('weekly_out_' + dow);
        if (inn) inn.value = '';
        if (out) out.value = '';
    }
}

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('input[data-weekly-rest][data-dow]').forEach(applyWeeklyRestDisabled);
    initRemainingScheduleData();
});

var remainingScheduleMeta = {
    scheduleYear: 0,
    scheduleMonth: 0,
    restDays: {},
    selectedEmployeeId: ''
};

function initRemainingScheduleData() {
    remainingScheduleMeta.scheduleYear = parseInt('[[${scheduleYear}]]', 10) || 0;
    remainingScheduleMeta.scheduleMonth = parseInt('[[${scheduleMonth}]]', 10) || 0;
    remainingScheduleMeta.restDays = {};
    document.querySelectorAll('input[data-weekly-rest][data-dow]').forEach(function (cb) {
        var dow = parseInt(cb.getAttribute('data-dow'), 10);
        if (!isNaN(dow)) remainingScheduleMeta.restDays[dow] = !!cb.checked;
    });
}

function showEmployeeRemaining(el) {
    if (!el) return;
    document.querySelectorAll('.shift-employee-item').forEach(function (item) {
        item.classList.remove('active');
    });
    el.classList.add('active');
    var empName = el.getAttribute('data-employee-name') || 'Employee';
    var panel = document.getElementById('shiftRemainingPanel');
    if (!panel) return;

    var year = remainingScheduleMeta.scheduleYear;
    var month = remainingScheduleMeta.scheduleMonth;
    if (!year || !month) {
        panel.innerHTML = '<p class="no-payroll shifting-remaining-hint">No schedule data.</p>';
        return;
    }

    var today = new Date();
    var start = new Date(year, month - 1, 1);
    var end = new Date(year, month, 0);
    var cursor = new Date(start);

    if (today.getFullYear() > year || (today.getFullYear() === year && (today.getMonth() + 1) > month)) {
        panel.innerHTML = '<p class="no-payroll shifting-remaining-hint"><b>' + escapeHtml(empName) + '</b>: 0 remaining day(s). Month already ended.</p>';
        return;
    }

    if (today.getFullYear() === year && (today.getMonth() + 1) === month && today.getDate() > 1) {
        cursor = new Date(year, month - 1, today.getDate());
    }

    var totalRemaining = 0;
    var workRemaining = 0;
    while (cursor <= end) {
        totalRemaining++;
        var jsDay = cursor.getDay(); // 0=Sun
        var mappedDow = jsDay === 0 ? 7 : jsDay; // 1=Mon..7=Sun
        if (!remainingScheduleMeta.restDays[mappedDow]) {
            workRemaining++;
        }
        cursor.setDate(cursor.getDate() + 1);
    }

    panel.innerHTML =
        '<p class="no-payroll shifting-remaining-hint"><b>' + escapeHtml(empName) + '</b></p>' +
        '<p class="no-payroll shifting-remaining-line">Remaining calendar days in this shift: <b>' + totalRemaining + '</b></p>' +
        '<p class="no-payroll shifting-remaining-line-tight">Remaining scheduled work days: <b>' + workRemaining + '</b></p>';
}

function escapeHtml(str) {
    return String(str || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}