module.exports = {
  mode: 'jit',
  content: ['./index.html', './login.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  daisyui: {
    themes: ['emerald'],
  },
  theme: {},
  plugins: [require('@tailwindcss/forms'), require('@tailwindcss/typography')],
};
