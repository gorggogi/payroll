function openHolidayModal(id) {
    document.getElementById(id).classList.add('show');
    document.body.style.overflow = 'hidden';
}

function openEditHolidayModal(button) {
    const holidayId = button.getAttribute('data-holiday-id');
    const holidayName = button.getAttribute('data-holiday-name') || '';
    const holidayDate = button.getAttribute('data-holiday-date') || '';
    const holidayType = button.getAttribute('data-holiday-type') || '';

    const form = document.getElementById('editHolidayForm');
    form.setAttribute('action', '/admin/holidays/' + holidayId + '/update');

    document.getElementById('editHolidayName').value = holidayName;
    document.getElementById('editHolidayDate').value = holidayDate;
    document.getElementById('editHolidayType').value = holidayType;

    openHolidayModal('editHolidayModal');
}

function closeHolidayModal(id) {
    document.getElementById(id).classList.remove('show');
    document.body.style.overflow = '';
}
window.addEventListener('click', function (e) {
    const modals = document.querySelectorAll('.holiday-modal.show');
    modals.forEach(function (m) {
        if (e.target === m) closeHolidayModal(m.id);
    });
});
