# Используем официальный Node.js образ для сборки
FROM node:23-slim AS builder

WORKDIR /app

# Копируем package.json и package-lock.json (или yarn.lock)
COPY package.json ./
COPY tsconfig.json ./
COPY vite.config.* ./
COPY . .

# Устанавливаем зависимости
RUN npm install

# Собираем приложение
RUN npm run build

# --- Production image ---
FROM nginx:alpine AS production

# Копируем собранные файлы из builder
COPY --from=builder /app/dist /usr/share/nginx/html

# Копируем кастомный nginx конфиг, если есть (опционально)
# COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

# --- Development image ---
# Для разработки используйте только node:18 и запускайте Vite dev server
# FROM node:18 AS dev
# WORKDIR /app
# COPY . .
# RUN npm install
# EXPOSE 5173
# CMD ["npm", "run", "dev"]