# Step 1: Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
# Accept API URL at build time (default = /api for Docker nginx proxy)
ARG VITE_API_URL=http://localhost:8081/api
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

# Step 2: Serve with nginx
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
