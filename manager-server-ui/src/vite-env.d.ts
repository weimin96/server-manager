declare module '*.vue' {
  import { DefineComponent } from 'vue';
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const component: DefineComponent<object, object, any>;
  export default component;
}

interface ImportMeta {
  // Vite 特有的 import.meta.glob 方法
  glob(
    pattern: string,
    options?: {
      eager?: boolean;
      import?: (module: any) => any;
      as?: string;
    },
  ): Record<string, () => Promise<any>>;
}
