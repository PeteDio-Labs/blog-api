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
    }
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
