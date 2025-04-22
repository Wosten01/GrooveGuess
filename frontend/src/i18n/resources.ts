import { FallbackLng } from 'i18next';
import ruCommon from './locales/ru/translation.json';
import enCommon from './locales/en/translation.json';

export type DefaultResources = 'ru';
export const defaultRecourse: DefaultResources = 'ru';
export const defaultFallbackLng: FallbackLng = 'en';

export enum TranslationNamespace {
  Common = 'common',
}

export const defaultNS = 'common';

export const resources = {
  ru: {
    [TranslationNamespace.Common]: ruCommon,
  },
  en: {
    [TranslationNamespace.Common]: enCommon,
  },
};
