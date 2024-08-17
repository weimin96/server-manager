import Application from '@/services/application';
import Instance from '@/services/instance';

export interface ActionHandler {
  restart(item: any): Promise<void>;

  unregister(item: any): Promise<void>;

  shutdown(item: any): Promise<void>;
}

export class InstanceActionHandler implements ActionHandler {
  constructor(
    private $smModal: any,
    private t: any,
  ) {}

  async unregister(item: Instance) {
    const isConfirmed = await this.$smModal.confirm(
      this.t('applications.actions.unregister'),
      this.t('instances.unregister', { name: item.id }),
    );
    if (!isConfirmed) {
      return;
    }
    await item.unregister();
  }

  async shutdown(item: Instance) {
    const isConfirmed = await this.$smModal.confirm(
      this.t('applications.actions.shutdown'),
      this.t('instances.shutdown', { name: item.id }),
    );
    if (!isConfirmed) {
      return;
    }
    await item.shutdown();
  }

  async restart(item: Instance) {
    const isConfirmed = await this.$smModal.confirm(
      this.t('applications.actions.restart'),
      this.t('instances.restart', { name: item.id }),
    );
    if (!isConfirmed) {
      return;
    }
    await item.restart();
  }
}

export class ApplicationActionHandler implements ActionHandler {
  constructor(
    private $smModal: any,
    private t: any,
  ) {}

  async restart(application: Application) {
    const isConfirmed = await this.$smModal.confirm(
      this.t('applications.actions.restart'),
      this.t('applications.restart', { name: application.name }),
    );
    if (!isConfirmed) {
      return;
    }
    await application.restart();
  }

  async shutdown(application: Application) {
    const isConfirmed = await this.$smModal.confirm(
      this.t('applications.actions.shutdown'),
      this.t('applications.shutdown', { name: application.name }),
    );
    if (!isConfirmed) {
      return;
    }
    await application.shutdown();
  }

  async unregister(application: Application) {
    const isConfirmed = await this.$smModal.confirm(
      this.t('applications.actions.unregister'),
      this.t('applications.unregister', { name: application.name }),
    );
    if (!isConfirmed) {
      return;
    }
    await application.unregister();
  }
}
