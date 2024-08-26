module.exports = {
  mode: 'jit',
  darkMode: 'class',
  content: ['./index.html', './login.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
  },
  plugins: [require('@tailwindcss/forms'), require('@tailwindcss/typography')],
};

function withOpacity(variableName) {
  return ({ opacityValue }) => {
    if (opacityValue !== undefined) {
      return `rgba(var(${variableName}), ${opacityValue})`;
    }
    return `rgb(var(${variableName}))`;
  };
}
