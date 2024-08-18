import ResizeObserver from 'resize-observer-polyfill';

const observers = new WeakMap();

const mounted = (el, binding) => {
  beforeUnmount(el);
  const observer = new ResizeObserver(binding.value);
  observer.observe(el);
  observers.set(el, observer);
};

const beforeUnmount = (el) => {
  const observer = observers.get(el);
  if (observer) {
    observer.disconnect();
    observers.delete(el);
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
