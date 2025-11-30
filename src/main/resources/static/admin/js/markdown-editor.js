/**
 * Markdown Editor Integration (Option A: EasyMDE)
 * Hooks into existing textarea#content and EasyMDE preview.
 * Falls back to basic client-side rendering if EasyMDE is unavailable.
 */

let previewDebounceTimer = null;
const DEBOUNCE_DELAY = 300; // ms
let markdownEditorInitialized = false;

function initMarkdownEditor() {
    if (markdownEditorInitialized) return; // Prevent duplicate init
    const textarea = document.getElementById('content');
    if (!textarea) return;
    markdownEditorInitialized = true;

    // If EasyMDE is present and already initialized, use it
    if (window.easyMDE) {
        const mde = window.easyMDE;
        
        // Debounced render hook
        mde.codemirror.on('change', () => {
            debouncedPreview(mde.value(), getPreviewElement());
        });

        // Initial render
        if (mde.value()) {
            updatePreview(mde.value(), getPreviewElement());
        }

        // Keyboard shortcuts (Cmd/Ctrl+B/I/K)
        mde.codemirror.on('keydown', (cm, e) => {
            handleKeyboardShortcut(e, textarea);
        });
    } else {
        // Fallback: simple textarea + custom preview element if present
        const preview = getPreviewElement();
        textarea.addEventListener('input', function() {
            debouncedPreview(this.value, preview);
        });
        if (textarea.value) {
            updatePreview(textarea.value, preview);
        }
        textarea.addEventListener('keydown', function(e) {
            if (e.key === 'Tab') { e.preventDefault(); insertText(textarea, '  '); }
            handleKeyboardShortcut(e, textarea);
        });
    }
}

function getPreviewElement() {
    // Try EasyMDE built-in preview container, else a #preview element, else a fake DIV
    const easyPreview = document.querySelector('.editor-preview');
    const previewDiv = document.getElementById('preview');
    return easyPreview || previewDiv || createPreviewPlaceholder();
}

function createPreviewPlaceholder() {
    const div = document.createElement('div');
    div.id = 'preview';
    div.className = 'markdown-preview';
    const container = document.querySelector('.main-content') || document.body;
    container.appendChild(div);
    return div;
}

function debouncedPreview(markdown, previewElement) {
    clearTimeout(previewDebounceTimer);
    previewDebounceTimer = setTimeout(() => {
        updatePreview(markdown, previewElement);
    }, DEBOUNCE_DELAY);
}

function updatePreview(markdown, previewElement) {
    if (!previewElement) return;
    if (!markdown || markdown.trim() === '') {
        previewElement.innerHTML = '<p class="preview-placeholder">Preview will appear here...</p>';
        return;
    }
    // If EasyMDE is active, its preview is managed internally; for consistency, still set innerHTML
    const html = renderMarkdownClient(markdown);
    previewElement.innerHTML = html;
}

// Basic client-side markdown renderer (limited)
function renderMarkdownClient(markdown) {
    let html = markdown;
    html = html.replace(/^### (.*$)/gim, '<h3>$1</h3>');
    html = html.replace(/^## (.*$)/gim, '<h2>$1</h2>');
    html = html.replace(/^# (.*$)/gim, '<h1>$1</h1>');
    html = html.replace(/\*\*(.*?)\*\*/gim, '<strong>$1</strong>');
    html = html.replace(/\*(.*?)\*/gim, '<em>$1</em>');
    html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/gim, '<a href="$2">$1</a>');
    html = html.replace(/!\[([^\]]*)\]\(([^)]+)\)/gim, '<img src="$2" alt="$1">');
    html = html.replace(/```(.*?)```/gims, '<pre><code>$1</code></pre>');
    html = html.replace(/`([^`]+)`/gim, '<code>$1</code>');
    html = html.replace(/^\* (.*$)/gim, '<li>$1</li>');
    html = html.replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>');
    html = html.replace(/^> (.*$)/gim, '<blockquote>$1</blockquote>');
    html = html.replace(/\n/gim, '<br>');
    return html;
}

function handleKeyboardShortcut(e, editorEl) {
    if ((e.ctrlKey || e.metaKey) && e.key === 'b') { e.preventDefault(); wrapText(editorEl, '**', '**'); }
    if ((e.ctrlKey || e.metaKey) && e.key === 'i') { e.preventDefault(); wrapText(editorEl, '*', '*'); }
    if ((e.ctrlKey || e.metaKey) && e.key === 'k') { e.preventDefault(); insertLink(editorEl, getSelectedText(editorEl)); }
}

function getSelectedText(textarea) { return textarea.value.substring(textarea.selectionStart, textarea.selectionEnd); }

function wrapText(textarea, prefix, suffix) {
    if (!textarea) return;
    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const selectedText = textarea.value.substring(start, end);
    const replacement = prefix + selectedText + suffix;
    textarea.setRangeText(replacement, start, end, 'end');
    textarea.dispatchEvent(new Event('input'));
    textarea.selectionStart = start + prefix.length;
    textarea.selectionEnd = end + prefix.length;
}

function insertText(textarea, text) {
    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    textarea.setRangeText(text, start, end, 'end');
    textarea.dispatchEvent(new Event('input'));
}

function insertLink(textarea, selectedText) {
    const url = prompt('Enter URL:', 'https://');
    if (url) {
        const linkText = selectedText || 'link text';
        const markdown = `[${linkText}](${url})`;
        const start = textarea.selectionStart;
        const end = textarea.selectionEnd;
        textarea.setRangeText(markdown, start, end, 'end');
        textarea.dispatchEvent(new Event('input'));
        textarea.selectionStart = start + 1;
        textarea.selectionEnd = start + 1 + linkText.length;
    }
}

function previewPost() {
    const title = document.getElementById('title')?.value || '';
    const contentEl = document.getElementById('content');
    const content = contentEl?.easyMDEInstance ? contentEl.easyMDEInstance.value() : (contentEl?.value || '');
    if (!title || !content) { alert('Please enter title and content before previewing'); return; }
    const previewWindow = window.open('', 'Preview', 'width=800,height=600');
    if (!previewWindow) { alert('Please allow popups for preview functionality'); return; }
    const html = `
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Preview: ${title}</title>
            <link rel="stylesheet" href="/admin/css/neon-theme.css">
            <link rel="stylesheet" href="/admin/css/markdown-preview.css">
            <style>
                body { max-width: 800px; margin: 0 auto; padding: 2rem; }
                .preview-header { margin-bottom: 2rem; padding-bottom: 1rem; border-bottom: 2px solid var(--neon-cyan); }
                .preview-title { font-size: 2.5rem; margin-bottom: 1rem; }
            </style>
        </head>
        <body>
            <div class="preview-header">
                <h1 class="preview-title neon-text">${title}</h1>
            </div>
            <div class="markdown-preview">${renderMarkdownClient(content)}</div>
        </body>
        </html>`;
    previewWindow.document.write(html);
    previewWindow.document.close();
}

window.initMarkdownEditor = initMarkdownEditor;
window.previewPost = previewPost;