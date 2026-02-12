<template>
  <div class="language-switcher" @click="toggleLanguage">
    <div class="switch-track" :class="{ 'zh-active': currentLocale === 'zh' }">
      <span class="lang-label en">EN</span>
      <span class="lang-label zh">中</span>
      <div class="switch-thumb"></div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { locale } = useI18n()

const currentLocale = computed(() => locale.value)

const toggleLanguage = () => {
  const newLocale = locale.value === 'en' ? 'zh' : 'en'
  locale.value = newLocale
  localStorage.setItem('user_locale', newLocale)
}
</script>

<style scoped>
.language-switcher {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.switch-track {
  width: 64px;
  height: 32px;
  background-color: var(--bg-light);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 8px;
  transition: all 0.3s ease;
  user-select: none;
}

.switch-track:hover {
  border-color: var(--primary-color);
  background-color: white;
}

.lang-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-dim);
  z-index: 1;
  transition: color 0.3s;
}

.switch-thumb {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 24px;
  height: 24px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* Active States */
.zh-active .switch-thumb {
  transform: translateX(32px);
  background: var(--primary-gradient);
}

.switch-track:not(.zh-active) .switch-thumb {
  background: var(--primary-gradient); 
}

/* Text Colors */
.switch-track:not(.zh-active) .lang-label.en {
  color: white;
  mix-blend-mode: difference; /* Optional for contrast */
}

.zh-active .lang-label.zh {
  color: white; 
  mix-blend-mode: difference;
}
</style>
