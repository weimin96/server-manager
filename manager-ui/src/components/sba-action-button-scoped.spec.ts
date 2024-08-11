import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import SbaActionButtonScoped from './sba-action-button-scoped.vue';

import { render } from '@/test-utils';

describe('SbaActionButtonScoped', function () {
  const actionFn = vi.fn().mockResolvedValue([]);

  beforeEach(() => {
    render(SbaActionButtonScoped, {
      props: {
        instanceCount: 10,
        label: 'Execute',
        actionFn,
      },
    });
  });

  it('should cal actionFn when confirmed', async () => {
    await userEvent.click(
      await screen.findByRole('button', { name: 'Execute' }),
    );
    await userEvent.click(
      await screen.findByRole('button', { name: 'Confirm' }),
    );

    expect(actionFn).toHaveBeenCalledTimes(1);
  });
});
