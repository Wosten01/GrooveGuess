import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

import { defaultNS, resources, defaultRecourse, defaultFallbackLng } from './resources';

i18n.use(initReactI18next).init({
  lng: defaultRecourse,
  defaultNS,
  resources,
});

i18n
  .use(initReactI18next)
  .init({
    defaultNS,
    lng: defaultRecourse,
    fallbackLng: defaultFallbackLng,
    resources,
    interpolation: {
      escapeValue: false,
    },
  });