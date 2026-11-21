/**
 * Aura Reader - Official Web App Script
 * Anti-slop, zero-dependency, ultra-fast interactions
 */

(function () {
  'use strict';

  // --- 1. Dynamic GitHub Release Resolver ---
  const DEFAULT_VERSION = 'v2.3.0-beta.32';
  const REPO = 'Kouqqu/aura-reader';
  const FALLBACK_DOWNLOAD_URL = `https://github.com/${REPO}/releases/download/${DEFAULT_VERSION}/AuraReader-${DEFAULT_VERSION}.apk`;
  const RELEASES_PAGE_URL = `https://github.com/${REPO}/releases`;

  async function updateDownloadLinks() {
    const downloadBtns = document.querySelectorAll('.apk-download-btn');
    const versionBadges = document.querySelectorAll('.release-version-tag');

    // Set initial fallback
    downloadBtns.forEach(btn => {
      btn.href = FALLBACK_DOWNLOAD_URL;
    });

    try {
      const response = await fetch(`https://api.github.com/repos/${REPO}/releases?per_page=5`);
      if (!response.ok) return;

      const releases = await response.json();
      if (!Array.isArray(releases) || releases.length === 0) return;

      // Find the first release that has an APK asset
      for (const rel of releases) {
        if (!rel.assets || rel.assets.length === 0) continue;
        
        // Find versioned APK asset first, or any .apk
        const apkAsset = rel.assets.find(a => a.name.endsWith('.apk') && a.name.includes('-v')) 
                      || rel.assets.find(a => a.name.endsWith('.apk'));

        if (apkAsset) {
          const downloadUrl = apkAsset.browser_download_url;
          const tagName = rel.tag_name || DEFAULT_VERSION;
          const sizeMb = (apkAsset.size / (1024 * 1024)).toFixed(1);

          downloadBtns.forEach(btn => {
            btn.href = downloadUrl;
          });

          versionBadges.forEach(badge => {
            badge.textContent = `Текущий релиз: ${tagName} • ${sizeMb} МБ`;
          });

          const heroVersionPill = document.querySelector('.hero-pill-text');
          if (heroVersionPill) {
            heroVersionPill.textContent = `Доступна версия ${tagName}`;
          }
          break;
        }
      }
    } catch (e) {
      console.warn('Using offline fallback for release assets:', e);
    }
  }

  // --- 2. Interactive 3D Book Cover Stage ---
  function init3DStage() {
    const stage = document.querySelector('.interactive-book-stage');
    const glare = document.querySelector('.interactive-glare');
    if (!stage) return;

    let isHovered = false;

    stage.addEventListener('mouseenter', () => {
      isHovered = true;
      stage.style.transition = 'transform 0.1s ease-out';
    });

    stage.addEventListener('mousemove', (e) => {
      if (!isHovered) return;
      const rect = stage.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;

      const centerX = rect.width / 2;
      const centerY = rect.height / 2;

      // Calculate tilt angles (limit between -14 and 14 deg)
      const rotateX = ((y - centerY) / centerY) * -12;
      const rotateY = ((x - centerX) / centerX) * 12;

      stage.style.transform = `perspective(1000px) rotateX(${rotateX.toFixed(2)}deg) rotateY(${rotateY.toFixed(2)}deg) scale3d(1.02, 1.02, 1.02)`;

      if (glare) {
        const percentX = Math.round((x / rect.width) * 100);
        const percentY = Math.round((y / rect.height) * 100);
        glare.style.background = `radial-gradient(circle at ${percentX}% ${percentY}%, rgba(255, 255, 255, 0.3) 0%, transparent 65%)`;
      }
    });

    stage.addEventListener('mouseleave', () => {
      isHovered = false;
      stage.style.transition = 'transform 0.4s cubic-bezier(0.16, 1, 0.3, 1)';
      stage.style.transform = 'perspective(1000px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)';
    });
  }

  // --- 3. Style Switcher Interactive Demo ---
  function initStyleSwitcher() {
    const tabs = document.querySelectorAll('.style-tab-btn');
    const previewFrame = document.querySelector('.style-preview-frame');
    const dockText = document.querySelector('.dock-pill-text');

    if (!tabs.length || !previewFrame) return;

    tabs.forEach(tab => {
      tab.addEventListener('click', () => {
        tabs.forEach(t => t.classList.remove('active'));
        tab.classList.add('active');

        const style = tab.dataset.style;
        if (style === 'liquid') {
          previewFrame.classList.remove('preview-material');
          previewFrame.classList.add('preview-liquid');
          if (dockText) dockText.textContent = 'Liquid Glass • Невесомый матовый интерфейс';
        } else {
          previewFrame.classList.remove('preview-liquid');
          previewFrame.classList.add('preview-material');
          if (dockText) dockText.textContent = 'Material 3 • Классический лаконичный контраст';
        }
      });
    });
  }

  // --- 4. Interactive TTS Simulator ---
  function initTtsSimulator() {
    const playBtn = document.querySelector('.tts-btn-play');
    const words = document.querySelectorAll('.tts-word');
    if (!playBtn || !words.length) return;

    let currentIndex = 0;
    let timer = null;
    let isPlaying = false;

    function playNextWord() {
      words.forEach(w => w.classList.remove('tts-highlight'));
      if (words[currentIndex]) {
        words[currentIndex].classList.add('tts-highlight');
      }
      currentIndex = (currentIndex + 1) % words.length;
    }

    playBtn.addEventListener('click', () => {
      isPlaying = !isPlaying;
      if (isPlaying) {
        playBtn.innerHTML = `
          <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
            <rect x="6" y="4" width="4" height="16" rx="1"/>
            <rect x="14" y="4" width="4" height="16" rx="1"/>
          </svg>
        `;
        playBtn.title = "Пауза";
        playNextWord();
        timer = setInterval(playNextWord, 320);
      } else {
        playBtn.innerHTML = `
          <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
            <polygon points="5 3 19 12 5 21 5 3"/>
          </svg>
        `;
        playBtn.title = "Слушать";
        clearInterval(timer);
        timer = null;
      }
    });
  }

  // --- 5. OPDS Copy Feed URL ---
  function initOpdsCopy() {
    const copyBtn = document.querySelector('.opds-copy-btn');
    if (!copyBtn) return;

    copyBtn.addEventListener('click', async (e) => {
      e.preventDefault();
      const opdsUrl = window.location.origin + window.location.pathname.replace(/\/$/, '') + '/catalogs.json';
      try {
        await navigator.clipboard.writeText(opdsUrl);
        const originalText = copyBtn.innerHTML;
        copyBtn.innerHTML = `
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
          Ссылка скопирована
        `;
        setTimeout(() => {
          copyBtn.innerHTML = originalText;
        }, 2200);
      } catch (err) {
        prompt('Ссылка на OPDS каталог:', opdsUrl);
      }
    });
  }

  // --- DOM Ready Bootstrapper ---
  document.addEventListener('DOMContentLoaded', () => {
    updateDownloadLinks();
    init3DStage();
    initStyleSwitcher();
    initTtsSimulator();
    initOpdsCopy();
  });
})();
