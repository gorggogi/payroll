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

    // Leave nav indicator: refresh when tab is visible only (no polling in background)
    (function () {
        const indicator = document.getElementById('leave-nav-indicator');
        if (!indicator) return;

        function updateIndicator() {
            fetch('/api/leave-indicator', { credentials: 'same-origin' })
                .then(function (res) { return res.ok ? res.json() : null; })
                .then(function (data) {
                    if (data && typeof data.showIndicator === 'boolean') {
                        indicator.style.display = data.showIndicator ? '' : 'none';
                    }
                })
                .catch(function () {});
        }

        var pollTimer;
        function startPolling() {
            updateIndicator();
            pollTimer = setInterval(updateIndicator, 45000);
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
