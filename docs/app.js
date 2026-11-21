/**
 * Aura Reader - Minimalist scripts
 */

(function () {
  'use strict';

  const REPO = 'Kouqqu/aura-reader';
  const DEFAULT_VERSION = 'v2.3.0-beta.32';
  const FALLBACK_URL = `https://github.com/${REPO}/releases/download/${DEFAULT_VERSION}/AuraReader-${DEFAULT_VERSION}.apk`;

  async function resolveLatestRelease() {
    const downloadBtns = document.querySelectorAll('.apk-download-link');
    const versionLabels = document.querySelectorAll('.release-version-label');
    const sizeLabels = document.querySelectorAll('.release-size-label');

    // Default values
    downloadBtns.forEach(btn => btn.href = FALLBACK_URL);

    try {
      const res = await fetch(`https://api.github.com/repos/${REPO}/releases?per_page=5`);
      if (!res.ok) return;

      const releases = await res.json();
      if (!Array.isArray(releases)) return;

      for (const rel of releases) {
        if (!rel.assets || !rel.assets.length) continue;

        const apk = rel.assets.find(a => a.name.endsWith('.apk') && a.name.includes('-v'))
                 || rel.assets.find(a => a.name.endsWith('.apk'));

        if (apk) {
          const version = rel.tag_name || DEFAULT_VERSION;
          const sizeMb = (apk.size / (1024 * 1024)).toFixed(1) + ' МБ';

          downloadBtns.forEach(btn => {
            btn.href = apk.browser_download_url;
            if (btn.dataset.withVersion) {
              btn.textContent = `Скачать APK (${version})`;
            }
          });

          versionLabels.forEach(el => el.textContent = version);
          sizeLabels.forEach(el => el.textContent = sizeMb);
          break;
        }
      }
    } catch (e) {
      console.warn('Fallback to bundled release info:', e);
    }
  }

  function initOpdsCopy() {
    const copyBtn = document.getElementById('copy-opds-btn');
    const urlInput = document.getElementById('opds-url-input');
    if (!copyBtn || !urlInput) return;

    copyBtn.addEventListener('click', async () => {
      const url = urlInput.textContent.trim();
      try {
        await navigator.clipboard.writeText(url);
        const originalText = copyBtn.textContent;
        copyBtn.textContent = 'Скопировано';
        setTimeout(() => {
          copyBtn.textContent = originalText;
        }, 2000);
      } catch (err) {
        prompt('Ссылка на каталог:', url);
      }
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    resolveLatestRelease();
    initOpdsCopy();
  });
})();
