from node:carbon-alpine

# Create app directory
WORKDIR /usr/src/app

# Install app dependencies
COPY package*.json ./
RUN echo 'TEST123 in dockerfile'
RUN npm install --only=production

# Bundle app source
COPY . .

EXPOSE 3000
CMD [ "npm", "start" ]
