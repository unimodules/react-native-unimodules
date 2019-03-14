#!/usr/bin/env node

const packageJson = require('../package.json');

const RESET = '\x1b[0m';
const RED   = '\x1b[31m';
const GREEN = '\x1b[32m';
const BLUE  = '\x1b[34m';

console.info(`
${GREEN}Successfully installed ${RED}react-native-unimodules${GREEN}. This package contains core unimodules that are commonly depended on by other unimodules. You will need to configure your project before using other unimodules like ${RED}expo-camera${GREEN}, ${RED}expo-media-library${GREEN}.
See configuration guide:
  ${BLUE}https://www.npmjs.com/package/react-native-unimodules/v/${packageJson.version}${RESET}
`);
