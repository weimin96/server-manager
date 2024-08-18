import { h, render } from 'vue';

export function removeElement(el) {
  if (typeof el.remove !== 'undefined') {
    el.remove();
  } else {
    el.parentNode?.removeChild(el);
  }
}

export function createComponent(component, props, parentContainer, slots = {}) {
  let vNode = h(component, props, slots);

  let container = parentContainer.querySelector('.sm-modal--wrapper');
  container = container || document.createElement('div');
  container.classList.add('sm-modal--wrapper');
  parentContainer.appendChild(container);
  render(vNode, container);

  const destroy = () => {
    if (container) render(null, container);
    container = null;
    vNode = null;
  };

  return {
    vNode,
    destroy,
  };
}
