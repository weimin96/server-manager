import { Component, Raw, RenderFunction } from 'vue';

import ViewRegistry from '@/viewRegistry';

export {};

declare global {
  type ApplicationStream = {
    data: any;
  } & MessageEvent;

  interface Window {
    SM: SMSettings;
    csrf: string;
    uiSettings: {
      icon: string;
      title: string;
    };
    param: any;
  }

  type PollTimer = {
    cache: number;
    datasource: number;
    gc: number;
    process: number;
    memory: number;
    threads: number;
    logfile: number;
  };

  type ViewSettings = {
    name: string;
    enabled: boolean;
  };

  type ExternalView = {
    label: string;
    url: string;
    order: number;
    iframe: boolean;
    children: ExternalView[];
  };

  type UISettings = {
    title: string;
    brand: string;
    icon: string;
    favicon: string;
    faviconDanger: string;
    pollTimer: PollTimer;
    routes: string[];
    externalViews: ExternalView[];
    viewSettings: ViewSettings[];
  };

  type SMSettings = {
    uiSettings: UISettings;
    csrf: {
      headerName: string;
      parameterName: string;
    };
    [key: string]: any;
  };

  type ViewInstallFunctionParams = {
    viewRegistry: ViewRegistry;
  };

  type SMView = {
    id: string;
    name?: string;
    parent: string;
    handle: string | Component | RenderFunction;
    path?: string;
    href?: string;
    order: number;
    isEnabled: () => boolean;
    component: Raw<any>;
    group: string;
    hasChildren: boolean;
    props: any;
  };

  type View = ComponentView | LinkView;

  interface ComponentView {
    name: string;
    path: string;
    label?: string;
    handle?: Component | RenderFunction;
    order?: number;
    group?: string;
    component: Component;
    isEnabled?: () => boolean;
  }

  interface LinkView {
    href?: string;
    label: string;
    order?: number;
  }

  type HealthStatus =
    | 'DOWN'
    | 'UP'
    | 'RESTRICTED'
    | 'UNKNOWN'
    | 'OUT_OF_SERVICE'
    | 'OFFLINE';
}
