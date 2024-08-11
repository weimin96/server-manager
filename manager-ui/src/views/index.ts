const isStorybook = Object.prototype.hasOwnProperty.call(window, 'STORIES');

const views = [];

if (!isStorybook) {
  const context: Record<string, any> = import.meta.glob(
    './**/index.(js|vue|ts)',
    { eager: true },
  );
  Object.keys(context)
    .filter((key) => {
      const contextElement = context[key];
      return 'default' in contextElement;
    })
    .forEach(function (key) {
      const defaultExport = context[key].default;
      if (defaultExport && defaultExport.install) {
        views.push(defaultExport);
      }
    });
}

export default views;
