/**
 * Media Reorder Handler
 * Drag and drop reordering of media items
 */

let draggedItem = null;
let mediaReorderInitialized = false;

function initMediaReorder() {
    const mediaGrid = document.getElementById('mediaGrid');
    if (!mediaGrid || mediaReorderInitialized) return;
    updateDraggableItems();
    mediaReorderInitialized = true;
}

function updateDraggableItems() {
    const mediaItems = document.querySelectorAll('.media-item');
    mediaItems.forEach((item, index) => {
        item.setAttribute('draggable', 'true');
        item.dataset.index = index;
        item.removeEventListener('dragstart', handleDragStart);
        item.removeEventListener('dragover', handleDragOver);
        item.removeEventListener('drop', handleDrop);
        item.removeEventListener('dragend', handleDragEnd);
        item.addEventListener('dragstart', handleDragStart);
        item.addEventListener('dragover', handleDragOver);
        item.addEventListener('drop', handleDrop);
        item.addEventListener('dragend', handleDragEnd);
    });
}

function handleDragStart(e) {
    draggedItem = this;
    this.classList.add('dragging');
    e.dataTransfer.effectAllowed = 'move';
    e.dataTransfer.setData('text/html', this.innerHTML);
}

function handleDragOver(e) {
    if (e.preventDefault) e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
    const targetItem = e.currentTarget;
    if (targetItem !== draggedItem) targetItem.classList.add('drag-over');
    return false;
}

function handleDrop(e) {
    if (e.stopPropagation) e.stopPropagation();
    const targetItem = e.currentTarget;
    targetItem.classList.remove('drag-over');
    if (draggedItem !== targetItem) {
        const mediaGrid = document.getElementById('mediaGrid');
        const items = Array.from(mediaGrid.querySelectorAll('.media-item'));
        const draggedIdx = items.indexOf(draggedItem);
        const targetIdx = items.indexOf(targetItem);
        if (draggedIdx < targetIdx) {
            targetItem.parentNode.insertBefore(draggedItem, targetItem.nextSibling);
        } else {
            targetItem.parentNode.insertBefore(draggedItem, targetItem);
        }
        updateDraggableItems();
        debouncedSaveMediaOrder();
    }
    return false;
}

function handleDragEnd() {
    this.classList.remove('dragging');
    document.querySelectorAll('.media-item').forEach(item => item.classList.remove('drag-over'));
    draggedItem = null;
}

let saveTimeout = null;
function debouncedSaveMediaOrder() {
    clearTimeout(saveTimeout);
    saveTimeout = setTimeout(() => { saveMediaOrder(); }, 400);
}

async function saveMediaOrder() {
    const postId = window.currentPostId || null;
    if (!postId) { console.warn('Missing postId for reorder'); return; }
    const mediaItems = document.querySelectorAll('.media-item');
    const mediaIds = Array.from(mediaItems).map(item => parseInt(item.dataset.mediaId));
    try {
        const response = await fetch(`/manage/api/posts/${postId}/media/reorder`, {
            method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ postId, mediaIds })
        });
        const data = await response.json();
        if (!response.ok || !data.success) { throw new Error(data.error || 'Failed to save order'); }
        showReorderSuccess();
    } catch (error) {
        console.error('Reorder error:', error);
        if (window.showError) { window.showError('Failed to save media order: ' + error.message); }
    }
}

function showReorderSuccess() {
    const mediaGrid = document.getElementById('mediaGrid');
    if (!mediaGrid) return;
    mediaGrid.classList.add('reorder-success');
    setTimeout(() => { mediaGrid.classList.remove('reorder-success'); }, 500);
}

window.initMediaReorder = initMediaReorder;
window.updateDraggableItems = updateDraggableItems;