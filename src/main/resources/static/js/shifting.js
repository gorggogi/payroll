var SHIFT_DETAILS_HASH = '#shift-details';

/** Tailwind utility class names (match shifting.html) for selected employee row */
var SHIFT_EMP_ACTIVE_CLASSES = [
    'border-[var(--primary-color)]',
    'bg-[#f3f2ff]',
    'shadow-[0_0_0_2px_rgba(112,111,214,0.12)]'
];

function clearShiftEmployeeActiveClasses(el) {
    SHIFT_EMP_ACTIVE_CLASSES.forEach(function (c) {
        el.classList.remove(c);
    });
}

function addShiftEmployeeActiveClasses(el) {
    SHIFT_EMP_ACTIVE_CLASSES.forEach(function (c) {
        el.classList.add(c);
    });
}

function selectTemplate(templateId, openDetailsAfterLoad) {
    if (!templateId) return;
    var monthInput = document.getElementById('scheduleMonthPicker');
    var yearInput = document.getElementById('scheduleYearPicker');
    var monthVal = monthInput ? monthInput.value : '';
    var yearVal = yearInput ? yearInput.value : '';
    var qs = '?scheduleMonth=' + encodeURIComponent(monthVal) +
             '&scheduleYear=' + encodeURIComponent(yearVal) +
             '&templateId=' + encodeURIComponent(templateId);
    var url = '/admin/attendance/shifts' + qs;
    if (openDetailsAfterLoad) {
        url += SHIFT_DETAILS_HASH;
    }
    window.location.href = url;
}

function onShiftCardClick(templateId, isAlreadySelected) {
    if (!templateId) return;
    if (isAlreadySelected) {
        openShiftDetailsModal();
        return;
    }
    selectTemplate(templateId, true);
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

function openShiftDetailsFromHashIfPresent() {
    if (window.location.hash !== SHIFT_DETAILS_HASH) return;
    var m = document.getElementById('shiftDetailsModal');
    if (!m) return;
    openShiftDetailsModal();
    if (history.replaceState) {
        history.replaceState(null, '', window.location.pathname + window.location.search);
    }
}

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('input[data-weekly-rest][data-dow]').forEach(applyWeeklyRestDisabled);
    initRemainingScheduleData();
    openShiftDetailsFromHashIfPresent();
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
        clearShiftEmployeeActiveClasses(item);
    });
    addShiftEmployeeActiveClasses(el);
    var empName = el.getAttribute('data-employee-name') || 'Employee';
    var panel = document.getElementById('shiftRemainingPanel');
    if (!panel) return;

    var year = remainingScheduleMeta.scheduleYear;
    var month = remainingScheduleMeta.scheduleMonth;
    if (!year || !month) {
        panel.innerHTML = '<p class="no-payroll m-0">No schedule data.</p>';
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
        '<p class="no-payroll m-0"><b>' + escapeHtml(empName) + '</b></p>' +
        '<p class="no-payroll mt-1.5 mb-0">Remaining calendar days in this shift: <b>' + totalRemaining + '</b></p>' +
        '<p class="no-payroll mt-0.5 mb-0">Remaining scheduled work days: <b>' + workRemaining + '</b></p>';
}

function escapeHtml(str) {
    return String(str || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}