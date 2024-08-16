import { merge } from 'lodash-es';
import { createI18n } from 'vue-i18n';

const context = import.meta.glob('../**/(*.)?i18n.*.json', { eager: true });
const messages = Object.keys(context)
  .map((key) => {
    const localeFromFile = /\.*i18n\.?([^/]*)\.json$/.exec(key);
    const messages = (context[key] as { default: never }).default;
    if (localeFromFile[1]) {
      return {
        [localeFromFile[1]]: messages,
      };
    } else {
      return messages;
    }
  })
  .reduce((prev, cur) => merge(prev, cur), {});

export function getAvailableLocales() {
  return Object.keys(messages);
}

let browserLanguage = navigator.language;
if (!browserLanguage.includes('zh')) {
  browserLanguage = browserLanguage.split('-')[0];
}

const i18n = createI18n({
  locale: getAvailableLocales().includes(browserLanguage)
    ? browserLanguage
    : 'en',
  fallbackLocale: 'en',
  legacy: false,
  silentFallbackWarn: process.env.NODE_ENV === 'production',
  silentTranslationWarn: process.env.NODE_ENV === 'production',
  messages,
});

export default i18n;
