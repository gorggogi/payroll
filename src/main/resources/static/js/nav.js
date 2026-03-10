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
