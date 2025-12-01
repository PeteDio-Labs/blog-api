/**
 * Media Upload Handler
 * Handles drag-and-drop and file input uploads with AJAX
 */

let currentPostId = null;
let mediaUploadInitialized = false;

function initMediaUpload(postId) {
    if (mediaUploadInitialized) return; // Prevent duplicate init
    currentPostId = postId;
    const uploadArea = document.getElementById('uploadArea');
    const mediaUpload = document.getElementById('mediaUpload');
    if (!uploadArea || !mediaUpload) return;

    mediaUpload.addEventListener('change', function(e) { handleFiles(e.target.files); });
    uploadArea.addEventListener('dragover', handleDragOver);
    uploadArea.addEventListener('dragleave', handleDragLeave);
    uploadArea.addEventListener('drop', handleDrop);
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        document.addEventListener(eventName, preventDefaults, false);
    });
    mediaUploadInitialized = true;
}

function preventDefaults(e) { e.preventDefault(); e.stopPropagation(); }
function handleDragOver(e) { e.preventDefault(); e.currentTarget.classList.add('drag-over'); }
function handleDragLeave(e) { e.currentTarget.classList.remove('drag-over'); }
function handleDrop(e) { e.preventDefault(); e.currentTarget.classList.remove('drag-over'); handleFiles(e.dataTransfer.files); }

function handleFiles(files) {
    if (!files || files.length === 0) return;
    const validFiles = Array.from(files).filter(validateFile);
    if (validFiles.length === 0) return;
    uploadFiles(validFiles);
}

function validateFile(file) {
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp', 'image/gif'];
    if (!allowedTypes.includes(file.type)) { showError(`File type not allowed: ${file.type}`); return false; }
    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) { showError(`File too large: ${file.name} (max 10MB)`); return false; }
    return true;
}

async function uploadFiles(files) {
    const uploadProgress = document.getElementById('uploadProgress');
    const progressFill = document.getElementById('progressFill');
    const progressText = document.getElementById('progressText');
    if (uploadProgress) uploadProgress.style.display = 'block';
    let uploaded = 0; const total = files.length;
    for (const file of files) {
        try {
            const media = await uploadFile(file);
            uploaded++; const percentage = Math.round((uploaded / total) * 100);
            if (progressFill) progressFill.style.width = percentage + '%';
            if (progressText) progressText.textContent = `Uploading ${uploaded}/${total}...`;
            addMediaToGallery(media);
        } catch (error) {
            console.error('Upload error:', error);
            showError(`Failed to upload ${file.name}: ${error.message}`);
        }
    }
    setTimeout(() => {
        if (uploadProgress) uploadProgress.style.display = 'none';
        if (progressFill) progressFill.style.width = '0%';
        if (progressText) progressText.textContent = 'Uploading...';
    }, 1000);
    const input = document.getElementById('mediaUpload'); if (input) input.value = '';
}

async function uploadFile(file) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('postId', currentPostId);
    formData.append('altText', file.name.replace(/\.[^/.]+$/, ''));
    const response = await fetch('/manage/api/media/upload', { method: 'POST', body: formData });
    const data = await response.json();
    if (!response.ok || !data.success) { throw new Error(data.error || `Server error: ${response.status}`); }
    return data.media;
}

function addMediaToGallery(media) {
    const mediaGrid = document.getElementById('mediaGrid'); if (!mediaGrid) return;
    const mediaItem = createMediaItem(media); mediaGrid.appendChild(mediaItem);
    const mediaCount = document.querySelector('.media-count');
    if (mediaCount) { const count = parseInt(mediaCount.textContent) + 1; mediaCount.textContent = count; }
}

function createMediaItem(media) {
    const div = document.createElement('div');
    div.className = 'media-item';
    div.dataset.mediaId = media.id;
    div.innerHTML = `
        <div class="media-thumbnail">
            <img src="${media.imageUrl}" alt="${media.altText}" class="media-img">
            <div class="media-overlay">
                <button type="button" class="btn-icon" onclick="setCoverImage('${media.imageUrl}')">Set as Cover</button>
                <button type="button" class="btn-icon btn-delete" data-media-id="${media.id}" onclick="deleteMedia(this)">Delete</button>
            </div>
        </div>
        <input type="text" value="${media.altText}" class="media-alt-input" placeholder="Alt text..." data-media-id="${media.id}" onchange="updateAltText(this)">
    `;
    div.classList.add('fade-in');
    return div;
}

function setCoverImage(imageUrl) {
    const coverPreview = document.getElementById('coverPreview');
    const coverImageUrl = document.getElementById('coverImageUrl');
    if (!coverPreview || !coverImageUrl) return;
    coverImageUrl.value = imageUrl;
    coverPreview.innerHTML = `<img src="${imageUrl}" alt="Cover" class="cover-img">`;
    showSuccess('Cover image updated');
    if (currentPostId) { updateCoverImageOnServer(currentPostId, imageUrl); }
}

async function updateCoverImageOnServer(postId, imageUrl) {
    try {
        const response = await fetch(`/manage/api/posts/${postId}/cover-image`, {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ imageUrl })
        });
        const data = await response.json();
        if (!response.ok || !data.success) { throw new Error(data.error || 'Failed to set cover image'); }
    } catch (error) {
        console.error('Cover image error:', error);
        showError('Failed to set cover image: ' + error.message);
    }
}

async function deleteMedia(button) {
    if (!confirm('Are you sure you want to delete this media?')) return;
    const mediaId = button.dataset.mediaId;
    try {
        const response = await fetch(`/manage/api/media/${mediaId}`, { method: 'DELETE', headers: { 'Content-Type': 'application/json' } });
        const data = await response.json();
        if (!response.ok || !data.success) { throw new Error(data.error || 'Delete failed'); }
        const mediaItem = button.closest('.media-item');
        mediaItem.classList.add('fade-out');
        setTimeout(() => {
            mediaItem.remove();
            const mediaCount = document.querySelector('.media-count');
            if (mediaCount) { const count = parseInt(mediaCount.textContent) - 1; mediaCount.textContent = count; }
        }, 300);
        showSuccess('Media deleted successfully');
    } catch (error) {
        console.error('Delete error:', error);
        showError('Failed to delete media: ' + error.message);
    }
}

async function updateAltText(input) {
    const mediaId = input.dataset.mediaId;
    const altText = input.value;
    try {
        const response = await fetch(`/manage/api/media/${mediaId}/alt-text`, {
            method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ altText })
        });
        const data = await response.json();
        if (!response.ok || !data.success) { throw new Error(data.error || 'Update failed'); }
        const img = input.closest('.media-item').querySelector('.media-img'); if (img) img.alt = altText;
    } catch (error) {
        console.error('Alt text update error:', error);
        showError('Failed to update alt text: ' + error.message);
    }
}

function showError(message) {
    if (window.adminUtils && window.adminUtils.showFlash) {
        window.adminUtils.showFlash(message, 'error');
        return;
    }
    let alert = document.querySelector('.alert-error');
    if (!alert) { alert = document.createElement('div'); alert.className = 'alert alert-error'; (document.querySelector('.main-content')||document.body).prepend(alert); }
    alert.innerHTML = `<span class="icon">✗</span> ${message}`; alert.style.display = 'flex';
    setTimeout(() => { alert.style.display = 'none'; }, 5000);
}

function showSuccess(message) {
    if (window.adminUtils && window.adminUtils.showFlash) {
        window.adminUtils.showFlash(message, 'success');
        return;
    }
    let alert = document.querySelector('.alert-success');
    if (!alert) { alert = document.createElement('div'); alert.className = 'alert alert-success'; (document.querySelector('.main-content')||document.body).prepend(alert); }
    alert.innerHTML = `<span class="icon">✓</span> ${message}`; alert.style.display = 'flex';
    setTimeout(() => { alert.style.display = 'none'; }, 3000);
}

window.initMediaUpload = initMediaUpload;
window.setCoverImage = setCoverImage;
window.deleteMedia = deleteMedia;
window.updateAltText = updateAltText;