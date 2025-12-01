// Admin Utilities - Common JavaScript functions for admin UI
// This file provides utilities for AJAX requests, form handling, and UI interactions

/**
 * Utility object for admin operations
 */
const AdminUtils = {
    
    /**
     * Make an AJAX request with CSRF token
     * @param {string} url - The URL to send the request to
     * @param {string} method - HTTP method (GET, POST, PUT, DELETE)
     * @param {object} data - Data to send with the request
     * @returns {Promise} - Fetch promise
     */
    ajax: function(url, method = 'GET', data = null) {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        
        const options = {
            method: method,
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'same-origin'
        };
        
        // Add CSRF token to headers if present
        if (csrfToken && csrfHeader) {
            options.headers[csrfHeader] = csrfToken;
        }
        
        // Add body for non-GET requests
        if (data && method !== 'GET') {
            options.body = JSON.stringify(data);
        }
        
        return fetch(url, options);
    },

    // JSON helpers
    getJson: async function(url) { const r = await AdminUtils.ajax(url, 'GET'); return r.json(); },
    postJson: async function(url, data) { const r = await AdminUtils.ajax(url, 'POST', data); return r.json(); },
    putJson: async function(url, data) { const r = await AdminUtils.ajax(url, 'PUT', data); return r.json(); },
    deleteJson: async function(url) { const r = await AdminUtils.ajax(url, 'DELETE'); return r.json(); },
    
    /**
     * Show a flash message
     * @param {string} message - Message text
     * @param {string} type - Message type (success, error, warning, info)
     */
    showFlash: function(message, type = 'info') {
        // Find or create flash messages container
        let container = document.querySelector('.flash-messages .container');
        if (!container) {
            const flashDiv = document.createElement('div');
            flashDiv.className = 'flash-messages';
            flashDiv.innerHTML = '<div class="container"></div>';
            document.querySelector('.main-content').insertAdjacentElement('beforebegin', flashDiv);
            container = flashDiv.querySelector('.container');
        }
        
        // Create alert element
        const alert = document.createElement('div');
        alert.className = `alert alert-${type}`;
        alert.innerHTML = `
            <span class="icon">${type === 'success' ? '✓' : type === 'error' ? '✗' : 'ℹ'}</span>
            <span>${message}</span>
        `;
        
        container.appendChild(alert);
        
        // Auto-remove after 5 seconds
        setTimeout(() => {
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 300);
        }, 5000);
    },
    
    /**
     * Confirm action before proceeding
     * @param {string} message - Confirmation message
     * @returns {boolean} - User confirmation
     */
    confirm: function(message) {
        return window.confirm(message);
    },

    confirmDelete: function(itemName, callback) {
        const ok = AdminUtils.confirm(`Are you sure you want to delete "${itemName}"? This action cannot be undone.`);
        if (ok && typeof callback === 'function') callback();
        return ok;
    },
    
    /**
     * Format date for display
     * @param {string} dateString - ISO date string
     * @returns {string} - Formatted date
     */
    formatDate: function(dateString) {
        if (!dateString) return 'N/A';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    },

    // Validators
    validators: {
        required: (value) => (!value || String(value).trim() === '' ? 'This field is required' : null),
        minLength: (min) => (value) => (value && String(value).length < min ? `Minimum length is ${min} characters` : null),
        maxLength: (max) => (value) => (value && String(value).length > max ? `Maximum length is ${max} characters` : null),
        email: (value) => {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            return value && !emailRegex.test(String(value)) ? 'Invalid email address' : null;
        },
        url: (value) => { try { new URL(String(value)); return null; } catch { return 'Invalid URL'; } }
    },

    // Form validation
    validateForm: function(formId, rules) {
        const form = document.getElementById(formId);
        if (!form) return false;
        let valid = true; let firstErrorMsg = null;
        for (const [field, fieldValidators] of Object.entries(rules)) {
            const input = form.querySelector(`[name="${field}"]`);
            if (!input) continue;
            for (const validator of fieldValidators) {
                const error = validator(input.value, input);
                if (error) {
                    valid = false;
                    firstErrorMsg = firstErrorMsg || error;
                    AdminUtils.markFieldInvalid(input, error);
                } else {
                    AdminUtils.markFieldValid(input);
                }
            }
        }
        if (!valid && firstErrorMsg) AdminUtils.showFlash(firstErrorMsg, 'error');
        return valid;
    },
    markFieldInvalid: function(input, message) {
        input.classList.add('is-invalid');
        input.classList.remove('is-valid');
        let msg = input.parentNode.querySelector('.error-message');
        if (!msg) { msg = document.createElement('div'); msg.className = 'error-message'; input.parentNode.appendChild(msg); }
        msg.textContent = message;
    },
    markFieldValid: function(input) {
        input.classList.remove('is-invalid');
        input.classList.add('is-valid');
        const msg = input.parentNode.querySelector('.error-message');
        if (msg) msg.remove();
    },

    // DOM helpers
    $: function(id) { return document.getElementById(id); },
    $$: function(selector) { return document.querySelectorAll(selector); },
    createElement: function(tag, attributes = {}, children = []) {
        const el = document.createElement(tag);
        for (const [k, v] of Object.entries(attributes)) {
            if (k === 'className') el.className = v;
            else if (k === 'textContent') el.textContent = v;
            else el.setAttribute(k, v);
        }
        children.forEach(ch => { if (typeof ch === 'string') el.appendChild(document.createTextNode(ch)); else el.appendChild(ch); });
        return el;
    },
    toggle: function(el) { el.style.display = el.style.display === 'none' ? '' : 'none'; },
    show: function(el) { el.style.display = ''; },
    hide: function(el) { el.style.display = 'none'; },

    // Debounce
    debounce: function(fn, wait) { let t; return function(...args) { clearTimeout(t); t = setTimeout(() => fn.apply(this, args), wait); }; },

    // Loading indicator
    showLoading: function(container) { const s = AdminUtils.createElement('div', { className: 'loading-spinner', textContent: 'Loading...' }); container.appendChild(s); },
    hideLoading: function(container) { const s = container.querySelector('.loading-spinner'); if (s) s.remove(); }
};

// Auto-dismiss flash messages after 5 seconds
document.addEventListener('DOMContentLoaded', function() {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 300);
        }, 5000);
    });
});

// Export for use in other scripts
if (typeof module !== 'undefined' && module.exports) {
    module.exports = AdminUtils;
}

// Also expose to window for convenience
window.adminUtils = AdminUtils;
window.showFlash = AdminUtils.showFlash;
window.confirmDelete = AdminUtils.confirmDelete;
