const mounted = (el, binding) => {
  if (!binding.value) {
    return;
  }

  const targetElement = document.querySelector(binding.value);
  if (targetElement) {
    const clientRect = targetElement.getBoundingClientRect();
    const top = clientRect.height + clientRect.top;

    el.style.top = `${top}px`;
    el.style.position = 'sticky';
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
};
