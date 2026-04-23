/*
 * Nettools Live — UI enhancements
 * Injetado pelo template default.xhtml. Zero dependência além de jQuery
 * (que o template já carrega).
 *
 * Features:
 *   1. Copy button flutuante no terminal #id-panel-result (todas as tool pages)
 *   2. Enter submete o form em campos .form-control
 *   3. Auto-scroll do terminal conforme chegam novas linhas
 */
(function () {
	'use strict';

	function injectCopyButton() {
		var footer = document.getElementById('id-panel-footer');
		var result = document.getElementById('id-panel-result');
		if (!footer || !result) return;
		if (document.getElementById('ns-copy-btn')) return;

		// Garante position: relative pro footer, necessário pro absolute do botão
		var cs = window.getComputedStyle(footer);
		if (cs.position === 'static') footer.style.position = 'relative';

		var btn = document.createElement('button');
		btn.type = 'button';
		btn.id = 'ns-copy-btn';
		btn.className = 'ns-copy-btn';
		btn.setAttribute('aria-label', 'Copy terminal output');
		btn.setAttribute('title', 'Copy output');
		btn.innerHTML = '⎘ Copy';
		btn.addEventListener('click', function (ev) {
			ev.preventDefault();
			ev.stopPropagation();
			var text = (result.textContent || result.innerText || '').trim();
			if (!text) return;
			var done = function () {
				btn.classList.add('ns-copied');
				btn.innerHTML = '✓ Copied';
				if (btn.blur) btn.blur();
				setTimeout(function () {
					btn.classList.remove('ns-copied');
					btn.innerHTML = '⎘ Copy';
				}, 1600);
			};
			if (navigator.clipboard && navigator.clipboard.writeText) {
				navigator.clipboard.writeText(text).then(done).catch(function () { fallbackCopy(text, done); });
			} else {
				fallbackCopy(text, done);
			}
			return false;
		});
		footer.appendChild(btn);
	}

	function fallbackCopy(text, done) {
		var active = document.activeElement;
		var ta = document.createElement('textarea');
		ta.value = text;
		ta.setAttribute('readonly', '');
		ta.style.position = 'fixed';
		ta.style.top = '-9999px';
		ta.style.opacity = '0';
		document.body.appendChild(ta);
		ta.select();
		try { document.execCommand('copy'); done(); } catch (e) {}
		document.body.removeChild(ta);
		if (active && active.focus) active.focus();
	}

	function enableEnterSubmit() {
		// Já existe esse comportamento em páginas específicas (peoplesearch),
		// mas não em todas. Liga pra qualquer input dentro de um <h:form>.
		document.addEventListener('keypress', function (ev) {
			if (ev.keyCode !== 13 && ev.which !== 13) return;
			var t = ev.target;
			if (!t || !t.tagName) return;
			if (t.tagName !== 'INPUT') return;
			if (t.type === 'submit' || t.type === 'button') return;
			// Acha o primeiro submit do form e clica
			var form = t.form;
			if (!form) return;
			var submit = form.querySelector('input[type="submit"], button[type="submit"], button.btn-success, input.btn-success');
			if (submit) {
				ev.preventDefault();
				submit.click();
			}
		});
	}

	function autoScrollTerminal() {
		var result = document.getElementById('id-panel-result');
		if (!result) return;
		var observer = new MutationObserver(function () {
			result.scrollTop = result.scrollHeight;
		});
		observer.observe(result, { childList: true, subtree: true, characterData: true });
	}

	function init() {
		injectCopyButton();
		enableEnterSubmit();
		autoScrollTerminal();
	}

	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', init);
	} else {
		init();
	}
})();
