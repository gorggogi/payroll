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
})();
