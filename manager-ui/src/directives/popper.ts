import Popper from 'popper.js';

const poppers = new WeakMap();

const mounted = (el, binding) => {
  const reference =
    typeof binding.value === 'string'
      ? document.getElementById(binding.value)
      : binding.value;
  if (reference) {
    const popper = new Popper(reference, el);
    poppers.set(el, popper);
  }
};

const beforeUnmount = (el) => {
  const popper = poppers.get(el);
  if (popper) {
    popper.destroy(el);
  }
};

export default {
  mounted,
  update(el, binding) {
    if (binding.value === binding.oldValue) {
      return;
    }
    mounted(el, binding);
  },
  beforeUnmount,
};
