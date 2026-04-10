import { createI18n } from 'vue-i18n'
import en from './locales/en'
import zh from './locales/zh'

const messages = {
    en,
    zh
}

// Get saved language or default to 'en'
const savedLocale = localStorage.getItem('user_locale') || 'en'

const i18n = createI18n({
    legacy: false, // Use Composition API mode
    locale: savedLocale,
    fallbackLocale: 'en',
    messages
})

export default i18n
