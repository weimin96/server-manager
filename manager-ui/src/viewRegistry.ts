import { remove } from 'lodash-es';
import { Text, VNode, h, markRaw, reactive, shallowRef, toRaw } from 'vue';
import { Router, createRouter, createWebHistory } from 'vue-router';

import SmConfig from './config';
import { VIEW_GROUP, VIEW_GROUP_ICON } from './views/ViewGroup.js';

let router: Router;

const createI18nTextVNode = (label: string) =>
  shallowRef({
    render(): VNode {
      return h(Text, this.$t(label));
    },
  });

// eslint-disable-next-line no-unused-vars
type ViewFilterFunction = (view: SMView) => boolean;
type ViewConfig = {
  isEnabled?: (obj?) => boolean;
  [key: string]: any;
};

export default class ViewRegistry {
  private readonly _redirects: any[] = [];

  private _views: SMView[] = reactive([]);

  get views(): SMView[] {
    return this._views;
  }

  get routes() {
    const routes = this._toRoutes((view) => view.path && !view.parent);
    return [...routes, ...this._redirects];
  }

  get router(): Router {
    return router;
  }

  setGroupIcon(name, icon) {
    VIEW_GROUP_ICON[name] = icon;
  }

  createRouter() {
    const routesKnownToBackend = SmConfig.uiSettings.routes.map(
      (r) => new RegExp(`^${r.replace('/**', '(/.*)?')}$`),
    );
    const unknownRoutes = this.routes.filter(
      (vr) =>
        vr.path !== '/' && !routesKnownToBackend.some((br) => br.test(vr.path)),
    );
    if (unknownRoutes.length > 0) {
      console.warn(
        `The routes ${JSON.stringify(
          unknownRoutes.map((r) => r.path),
        )} aren't known to the backend and may be not properly routed!`,
      );
    }

    router = createRouter({
      history: createWebHistory(),
      linkActiveClass: 'is-active',
      routes: this.routes,
    });
    return router;
  }

  getViewByName(name) {
    return Array.prototype.find.call(this._views, (v) => v.name === name);
  }

  addView(...views: View[]): SMView[] {
    return views.map((view) => this._addView(view));
  }

  addRedirect(path: string, redirect: string | object) {
    if (typeof redirect === 'string') {
      this._redirects.push({ path, redirect: { name: redirect } });
    } else {
      this._redirects.push({ path, redirect });
    }
  }

  _addView(viewConfig: ViewConfig): SMView {
    const view = { ...viewConfig } as SMView;
    view.hasChildren = !!viewConfig.children;

    if (!viewConfig.name) {
      view.name = [viewConfig.parent, viewConfig.path]
        .filter((p) => !!p)
        .join('/');
    }

    if (viewConfig.label && !viewConfig.handle) {
      view.handle = createI18nTextVNode(viewConfig.label);
    }

    if (viewConfig.handle) {
      view.handle = markRaw(viewConfig.handle);
    }

    if (!viewConfig.group) {
      view.group = VIEW_GROUP.NONE;
    }
    if (viewConfig.component) {
      view.component = markRaw(viewConfig.component);
    }

    if (!viewConfig.isEnabled) {
      view.isEnabled = () => {
        const viewSettings = SmConfig.uiSettings.viewSettings.find(
          (vs) => vs.name === viewConfig.name,
        );
        return !viewSettings || viewSettings.enabled === true;
      };
    } else {
      view.isEnabled = viewConfig.isEnabled;
    }

    this._removeExistingView(view);
    this._views.push(view);

    return view;
  }

  _removeExistingView(view) {
    remove(this._views, (v) => {
      return v.name === view.name && v.group === view.group;
    });
  }

  _toRoutes(filter: ViewFilterFunction) {
    return this._views.filter(filter).map((view) => {
      const children = this._toRoutes(
        (childView) => childView.parent === view.name,
      );

      return {
        path: view.path,
        name: view.name,
        component: view.component,
        props: view.props,
        meta: { view: toRaw(view) },
        children,
      };
    });
  }
}
