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

	function injectTerminalActions() {
		var footer = document.getElementById('id-panel-footer');
		var result = document.getElementById('id-panel-result');
		if (!footer || !result) return;
		if (document.getElementById('ns-terminal-actions')) return;

		// Garante position: relative pro footer, necessário pro absolute do grupo
		var cs = window.getComputedStyle(footer);
		if (cs.position === 'static') footer.style.position = 'relative';

		var group = document.createElement('div');
		group.id = 'ns-terminal-actions';
		group.className = 'ns-terminal-actions';

		// --- Clear button ---
		var clearBtn = document.createElement('button');
		clearBtn.type = 'button';
		clearBtn.id = 'ns-clear-btn';
		clearBtn.className = 'ns-copy-btn';
		clearBtn.setAttribute('aria-label', 'Clear terminal output');
		clearBtn.setAttribute('title', 'Clear output');
		clearBtn.innerHTML = '⌫ Clear';
		clearBtn.addEventListener('click', function (ev) {
			ev.preventDefault();
			ev.stopPropagation();
			result.innerHTML = '';
			footer.style.display = 'none';
			clearBtn.classList.add('ns-copied');
			clearBtn.innerHTML = '✓ Cleared';
			if (clearBtn.blur) clearBtn.blur();
			setTimeout(function () {
				clearBtn.classList.remove('ns-copied');
				clearBtn.innerHTML = '⌫ Clear';
			}, 1200);
			return false;
		});

		// --- Copy button ---
		var copyBtn = document.createElement('button');
		copyBtn.type = 'button';
		copyBtn.id = 'ns-copy-btn';
		copyBtn.className = 'ns-copy-btn';
		copyBtn.setAttribute('aria-label', 'Copy terminal output');
		copyBtn.setAttribute('title', 'Copy output');
		copyBtn.innerHTML = '⎘ Copy';
		copyBtn.addEventListener('click', function (ev) {
			ev.preventDefault();
			ev.stopPropagation();
			var text = (result.textContent || result.innerText || '').trim();
			if (!text) return;
			var done = function () {
				copyBtn.classList.add('ns-copied');
				copyBtn.innerHTML = '✓ Copied';
				if (copyBtn.blur) copyBtn.blur();
				setTimeout(function () {
					copyBtn.classList.remove('ns-copied');
					copyBtn.innerHTML = '⎘ Copy';
				}, 1600);
			};
			if (navigator.clipboard && navigator.clipboard.writeText) {
				navigator.clipboard.writeText(text).then(done).catch(function () { fallbackCopy(text, done); });
			} else {
				fallbackCopy(text, done);
			}
			return false;
		});

		group.appendChild(clearBtn);
		group.appendChild(copyBtn);
		footer.appendChild(group);
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

	function resetTerminalIfEmpty() {
		var result = document.getElementById('id-panel-result');
		var footer = document.getElementById('id-panel-footer');
		if (!result || !footer) return;
		var txt = (result.textContent || result.innerText || '').trim();
		if (txt === '' || txt === 'Net Tools Live - Terminal...') {
			footer.style.display = 'none';
		}
	}

	function registerJsfAjaxListener() {
		// O botão Clear dispara um JSF AJAX que re-renderiza o form. O
		// conteúdo injetado via JS (Copy/Clear no canto do terminal) some
		// junto. Re-injetamos e resetamos o terminal após cada AJAX success.
		if (typeof jsf === 'undefined' || !jsf.ajax || typeof jsf.ajax.addOnEvent !== 'function') {
			return;
		}
		jsf.ajax.addOnEvent(function (data) {
			if (data && data.status === 'success') {
				injectTerminalActions();
				autoScrollTerminal();
				resetTerminalIfEmpty();
			}
		});
	}

	function highlightCurrentSidebarItem() {
		var menu = document.getElementById('side-menu');
		if (!menu) return;
		var path = window.location.pathname.replace(/\/+$/, '') || '/';
		var bestLink = null;
		var bestLen = 0;
		var links = menu.querySelectorAll('li.nav-item > a[href]');
		for (var i = 0; i < links.length; i++) {
			var a = links[i];
			var href;
			try {
				href = new URL(a.getAttribute('href'), window.location.href).pathname.replace(/\/+$/, '') || '/';
			} catch (e) { continue; }
			if (href === '/' || href.indexOf('://') !== -1) continue;
			if (path === href || (href !== '/' && path.indexOf(href) === 0 && path.charAt(href.length) === '/')) {
				if (href.length > bestLen) {
					bestLen = href.length;
					bestLink = a;
				}
			}
		}
		if (bestLink) {
			bestLink.parentElement.classList.add('ns-current');
		}
	}

	function init() {
		injectTerminalActions();
		enableEnterSubmit();
		autoScrollTerminal();
		resetTerminalIfEmpty();
		registerJsfAjaxListener();
		highlightCurrentSidebarItem();
	}

	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', init);
	} else {
		init();
	}
})();
