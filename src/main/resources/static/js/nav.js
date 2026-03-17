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
        var isPayrollPage = window.location.pathname.includes('/payroll/');
        var isDeductionsPage = window.location.pathname.includes('/deductions');

        dropdowns.forEach(function (btn) {
            var menu = btn.nextElementSibling;
            if (!menu || !menu.classList.contains('nav-dropdown-menu')) return;

            // If any link in this dropdown matches the current path, pin it open
            try {
                var links = menu.querySelectorAll('a[href]');
                links.forEach(function (link) {
                    try {
                        var url = new URL(link.getAttribute('href'), window.location.origin);
                        if (url.pathname === currentPath) {
                            menu.classList.add('show');
                            btn.classList.add('active', 'nav-dropdown-pinned');
                            var li = link.closest('li');
                            if (li) {
                                li.classList.add('active');
                            }
                        }
                    } catch (e) {}
                });
            } catch (e) {}

            // On deductions page, expand menu and set active on button (legacy behaviour)
            if (isDeductionsPage) {
                menu.classList.add('show');
                btn.classList.add('active');
                // Set deductions menu item as active
                var deductionsItem = menu.querySelector('li:nth-child(2)');
                if (deductionsItem) {
                    deductionsItem.classList.add('active');
                }
            }

            var hideMenuTimeout;

            function positionMenu() {
                var nav = document.querySelector('nav');
                var isCollapsed = nav.classList.contains('nav-collapsed');
                if (isCollapsed) {
                    var rect = btn.getBoundingClientRect();
                    // Position menu to the right of button, aligned with button top
                    menu.style.left = (rect.right + 2) + 'px';
                    menu.style.top = rect.top + 'px';
                }
            }

            // Show menu on button hover
            btn.addEventListener('mouseenter', function () {
                clearTimeout(hideMenuTimeout);
                menu.classList.add('show');
                positionMenu();
                if (!isPayrollPage && !isDeductionsPage) {
                    btn.classList.add('active');
                }
            });

            // Hide menu on button leave with delay
            btn.addEventListener('mouseleave', function () {
                hideMenuTimeout = setTimeout(function () {
                    if (btn.classList.contains('nav-dropdown-pinned')) return;
                    if (!menu.matches(':hover')) {
                        menu.classList.remove('show');
                        btn.classList.remove('active');
                    }
                }, 200);
            });

            // Keep menu visible when hovering over it
            menu.addEventListener('mouseenter', function () {
                clearTimeout(hideMenuTimeout);
                menu.classList.add('show');
            });

            // Hide menu when leaving it
            menu.addEventListener('mouseleave', function () {
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
                    if (isCollapsed) {
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
