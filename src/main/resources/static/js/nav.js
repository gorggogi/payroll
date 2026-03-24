(function () {
    'use strict';

    const STORAGE_KEY = 'payroll-nav-collapsed';
    const BREAKPOINT = 1180;

    const nav = document.querySelector('nav');
    const toggle = document.getElementById('navToggle');

    if (!nav) return;

    function isSmallViewport() {
        return window.innerWidth < BREAKPOINT;
    }

    function applyState() {
        if (isSmallViewport()) {
            const expanded = nav.classList.contains('nav-expanded');
            if (expanded) {
                nav.classList.remove('nav-collapsed');
            } else {
                nav.classList.add('nav-collapsed');
            }
        } else {
            nav.classList.remove('nav-expanded');
            const collapsed = localStorage.getItem(STORAGE_KEY) === 'true';
            if (collapsed) {
                nav.classList.add('nav-collapsed');
            } else {
                nav.classList.remove('nav-collapsed');
            }
        }
    }

    function handleToggle() {
        if (isSmallViewport()) {
            nav.classList.toggle('nav-expanded');
        } else {
            const collapsed = nav.classList.toggle('nav-collapsed');
            localStorage.setItem(STORAGE_KEY, collapsed);
        }
    }

    function updateToggleIcon() {
        if (!toggle) return;
        const isCollapsed = isSmallViewport()
            ? !nav.classList.contains('nav-expanded')
            : nav.classList.contains('nav-collapsed');
        const icon = toggle.querySelector('i');
        if (icon) {
            icon.className = isCollapsed ? 'fa-solid fa-chevron-right' : 'fa-solid fa-chevron-left';
        }
    }

    nav.addEventListener('transitionend', updateToggleIcon);

    if (toggle) {
        toggle.addEventListener('click', function () {
            handleToggle();
            updateToggleIcon();
        });
    }

    window.addEventListener('resize', function () {
        applyState();
        updateToggleIcon();
    });

    applyState();
    updateToggleIcon();

    // Dropdown toggle functionality for nav dropdowns - hover and click
    (function () {
        var dropdowns = document.querySelectorAll('.nav-dropdown-toggle');
        if (!dropdowns.length) return;

        // Check if we're on specific pages
        var currentPath = window.location.pathname || '';
        var isPayrollPage = currentPath.includes('/payroll/');
        var isDeductionsPage = currentPath.includes('/deductions');
        var isAdjustmentsPage = currentPath.includes('/adjustments');
        var isAttendancePage = currentPath.includes('/attendance');
        var isLeavePage = currentPath.includes('/leave');
        var isShiftsPage = currentPath.includes('/attendance/shifts');
        var isPayrollRelatedPage = isPayrollPage || isDeductionsPage || isAdjustmentsPage;
        var isAttendanceRelatedPage = isAttendancePage || isLeavePage;

        dropdowns.forEach(function (btn) {
            var menu = btn.nextElementSibling;
            if (!menu || !menu.classList.contains('nav-dropdown-menu')) return;

            // Determine which dropdown this is based on button title
            var btnTitle = btn.getAttribute('title') || '';
            var isPayrollDropdown = btnTitle === 'Payroll';
            var isAttendanceDropdown = btnTitle === 'Attendance';

            // Track if menu was opened by click (true) or just hover (false)
            var isClickOpen = menu.classList.contains('show');

            var nav = document.querySelector('nav');
            var isSmallScreenCollapsed = window.innerWidth < 1180 && !nav.classList.contains('nav-expanded');
            var isNavCollapsed = nav.classList.contains('nav-collapsed');

            // Always mark toggle as active on payroll related pages (only if payroll dropdown)
            if (isPayrollDropdown && isPayrollRelatedPage) {
                btn.classList.add('active');
                
                // Remove active class from all menu items first
                var allItems = menu.querySelectorAll('li');
                allItems.forEach(function(item) {
                    item.classList.remove('active');
                });
                
                // Mark the appropriate menu item as active based on current page
                if (isPayrollPage) {
                    var payrollItem = menu.querySelector('li:nth-child(1)');
                    if (payrollItem) {
                        payrollItem.classList.add('active');
                    }
                } else if (isDeductionsPage) {
                    var deductionsItem = menu.querySelector('li:nth-child(2)');
                    if (deductionsItem) {
                        deductionsItem.classList.add('active');
                    }
                } else if (isAdjustmentsPage) {
                    var adjustmentsItem = menu.querySelector('li:nth-child(3)');
                    if (adjustmentsItem) {
                        adjustmentsItem.classList.add('active');
                    }
                }
            }

            // Always mark toggle as active on attendance related pages (only if attendance dropdown)
            if (isAttendanceDropdown && isAttendanceRelatedPage) {
                btn.classList.add('active');
                
                // Remove active class from all menu items first
                var allAttendanceItems = menu.querySelectorAll('li');
                allAttendanceItems.forEach(function(item) {
                    item.classList.remove('active');
                });
                
                // Mark the appropriate attendance menu item as active based on current page
                if (isShiftsPage) {
                    // On shifts page, highlight Shifting link
                    var shiftsLink = menu.querySelector('a[href*="/attendance/shifts"]');
                    if (shiftsLink) {
                        var liShifts = shiftsLink.closest('li');
                        if (liShifts) {
                            liShifts.classList.add('active');
                        }
                    }
                } else if (isLeavePage) {
                    // Find Leave link by checking href attribute
                    var leaveLink = menu.querySelector('a[href*="/leave"]');
                    if (leaveLink) {
                        var li = leaveLink.closest('li');
                        if (li) {
                            li.classList.add('active');
                        }
                    }
                } else {
                    // Find Daily Time Record link - first attendance link
                    var allLinks = Array.from(menu.querySelectorAll('a'));
                    var dtrLink = allLinks.find(function(link) {
                        return link.href.includes('/attendance') && !link.href.includes('/overtime') && !link.href.includes('time-adjustments') && !link.href.includes('/shifts');
                    });
                    if (dtrLink) {
                        var li = dtrLink.closest('li');
                        if (li) {
                            li.classList.add('active');
                        }
                    }
                }
            }

            // On small collapsed screens, remove the default show class
            if (isSmallScreenCollapsed && menu.classList.contains('show')) {
                menu.classList.remove('show');
                btn.classList.remove('click-open');
                isClickOpen = false;
            }
            // On collapsed nav (large screens), remove the default show class
            else if (isNavCollapsed && menu.classList.contains('show')) {
                menu.classList.remove('show');
                btn.classList.remove('click-open');
                isClickOpen = false;
            }
            // On payroll related pages, expand payroll menu (but not on collapsed nav)
            else if (isPayrollDropdown && isPayrollRelatedPage && !menu.classList.contains('show') && !isSmallScreenCollapsed && !isNavCollapsed) {
                menu.classList.add('show');
                btn.classList.add('click-open');
                isClickOpen = true; // Treat default open as click-opened
            }
            // On attendance related pages, expand attendance menu (but not on collapsed nav)
            else if (isAttendanceDropdown && isAttendanceRelatedPage && !menu.classList.contains('show') && !isSmallScreenCollapsed && !isNavCollapsed) {
                menu.classList.add('show');
                btn.classList.add('click-open');
                isClickOpen = true; // Treat default open as click-opened
            }

            var hideMenuTimeout;

            function positionMenu() {
                var nav = document.querySelector('nav');
                var isCollapsed = nav.classList.contains('nav-collapsed');
                var isSmallScreen = window.innerWidth < 1180;
                var isNavExpanded = nav.classList.contains('nav-expanded');
                
                if (isCollapsed || (isSmallScreen && !isNavExpanded)) {
                    var rect = btn.getBoundingClientRect();
                    // Position menu to the right of button, aligned with button top
                    menu.style.left = (rect.right + 2) + 'px';
                    menu.style.top = rect.top + 'px';
                }
            }

            // Show/hide menu on button click (toggle) - click persists state across hovers
            btn.addEventListener('click', function (e) {
                e.preventDefault();
                var isOpen = menu.classList.contains('show');
                
                if (isOpen && isClickOpen) {
                    // Menu is open and click-opened, so toggle it closed
                    menu.classList.remove('show');
                    btn.classList.remove('active', 'click-open');
                    isClickOpen = false;
                } else if (isOpen && !isClickOpen) {
                    // Menu is open but only hover-opened, so convert to click-opened (keep open)
                    btn.classList.add('click-open');
                    isClickOpen = true;
                } else {
                    // Menu is closed, so open it and set as click-opened
                    menu.classList.add('show');
                    btn.classList.add('active', 'click-open');
                    isClickOpen = true;
                    positionMenu();
                }
            });

            // Show menu on button hover (only if not click-opened)
            btn.addEventListener('mouseenter', function () {
                if (isClickOpen) return; // Don't hover-open if click-opened
                clearTimeout(hideMenuTimeout);
                menu.classList.add('show');
                positionMenu();
                btn.classList.add('active');
            });

            // Hide menu on button leave with delay (only if not click-opened)
            btn.addEventListener('mouseleave', function () {
                if (isClickOpen) return; // Don't hover-close if click-opened
                hideMenuTimeout = setTimeout(function () {
                    if (btn.classList.contains('nav-dropdown-pinned')) return;
                    if (!menu.matches(':hover')) {
                        menu.classList.remove('show');
                        btn.classList.remove('active');
                    }
                }, 200);
            });

            // Keep menu visible when hovering over it (only if not click-opened)
            menu.addEventListener('mouseenter', function () {
                if (isClickOpen) return; // Don't hover-interact if click-opened
                clearTimeout(hideMenuTimeout);
                menu.classList.add('show');
                btn.classList.add('active');
            });

            // Hide menu when leaving it (only if not click-opened)
            menu.addEventListener('mouseleave', function () {
                if (isClickOpen) return; // Don't hover-close if click-opened
                hideMenuTimeout = setTimeout(function () {
                    menu.classList.remove('show');
                    btn.classList.remove('active');
                }, 200);
            });
        });

        // Close dropdown when clicking on a link inside it
        var dropdownLinks = document.querySelectorAll('.nav-dropdown-menu a');
        dropdownLinks.forEach(function (link) {
            link.addEventListener('click', function () {
                // Allow the link to navigate normally
            });
        });

        // Reposition menus on window resize
        window.addEventListener('resize', function () {
            dropdowns.forEach(function (btn) {
                var menu = btn.nextElementSibling;
                if (menu && menu.classList.contains('nav-dropdown-menu') && menu.classList.contains('show')) {
                    var nav = document.querySelector('nav');
                    var isCollapsed = nav.classList.contains('nav-collapsed');
                    var isSmallScreen = window.innerWidth < 1180;
                    var isNavExpanded = nav.classList.contains('nav-expanded');
                    
                    if (isCollapsed || (isSmallScreen && !isNavExpanded)) {
                        var rect = btn.getBoundingClientRect();
                        menu.style.top = rect.top + 'px';
                        menu.style.left = (rect.right + 10) + 'px';
                    }
                }
            });
        });

        // Close dropdown when clicking outside
        document.addEventListener('click', function (e) {
            var isClickInside = false;
            dropdowns.forEach(function (btn) {
                if (btn.contains(e.target)) {
                    isClickInside = true;
                }
                var menu = btn.nextElementSibling;
                if (menu && menu.classList.contains('nav-dropdown-menu') && menu.contains(e.target)) {
                    isClickInside = true;
                }
            });
            if (!isClickInside) {
                // Close all dropdowns
                dropdowns.forEach(function (btn) {
                    var menu = btn.nextElementSibling;
                    if (menu && menu.classList.contains('nav-dropdown-menu')) {
                        if (!btn.classList.contains('nav-dropdown-pinned')) {
                            menu.classList.remove('show');
                        }
                    }
                    if (!btn.classList.contains('nav-dropdown-pinned')) {
                        btn.classList.remove('active');
                    }
                });
            }
        });
    })();

    // Nav indicators (leave, deductions, etc.): one API, poll when tab visible
    (function () {
        var indicators = document.querySelectorAll('[data-nav-indicator]');
        if (!indicators.length) return;

        var lastLeaveTeam = null;
        var lastLeaveSelf = null;

        function updateIndicators() {
            fetch('/api/nav-indicators', { credentials: 'same-origin' })
                .then(function (res) { return res.ok ? res.json() : null; })
                .then(function (data) {
                    if (!data || typeof data !== 'object') return;
                    indicators.forEach(function (el) {
                        var key = el.getAttribute('data-nav-indicator');
                        if (key && typeof data[key] === 'boolean') {
                            el.style.display = data[key] ? '' : 'none';
                        }
                    });

                    // If we're on a leave page and the relevant indicator just turned on,
                    // reload once so the pending list / history reflect the new state.
                    var path = window.location.pathname || '';
                    var leaveTeam = typeof data.leave === 'boolean' ? data.leave : null;
                    var leaveSelf = typeof data.leaveSelf === 'boolean' ? data.leaveSelf : null;

                    if (path === '/admin/leave' && leaveTeam !== null) {
                        if (lastLeaveTeam === false && leaveTeam === true) {
                            window.location.reload();
                        }
                        lastLeaveTeam = leaveTeam;
                    } else if ((path === '/admin/my-leave' || path === '/employee/leave') && leaveSelf !== null) {
                        if (lastLeaveSelf === false && leaveSelf === true) {
                            window.location.reload();
                        }
                        lastLeaveSelf = leaveSelf;
                    }
                })
                .catch(function () {});
        }

        var pollTimer;
        function startPolling() {
            updateIndicators();
            pollTimer = setInterval(updateIndicators, 45000);
        }
        function stopPolling() {
            if (pollTimer) {
                clearInterval(pollTimer);
                pollTimer = null;
            }
        }

        document.addEventListener('visibilitychange', function () {
            if (document.hidden) {
                stopPolling();
            } else {
                startPolling();
            }
        });
        if (!document.hidden) startPolling();
    })();
})();
