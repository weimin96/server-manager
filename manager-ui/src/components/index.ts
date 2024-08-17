import FontAwesomeIcon from './font-awesome-icon.js';
import SmActionButtonScoped from './sm-action-button-scoped.vue';
import SmAlert from './sm-alert.vue';
import SmButtonGroup from './sm-button-group.vue';
import SmButton from './sm-button.vue';
import SmCheckbox from './sm-checkbox.vue';
import SmConfirmButton from './sm-confirm-button.vue';
import SmFormattedObj from './sm-formatted-obj.vue';
import SmIconButton from './sm-icon-button.vue';
import SmInput from './sm-input.vue';
import SmKeyValueTable from './sm-key-value-table.vue';
import SmLoadingSpinner from './sm-loading-spinner.vue';
import SmModal from './sm-modal.vue';
import SmPaginationNav from './sm-pagination-nav.vue';
import SmPanel from './sm-panel.vue';
import SmSelect from './sm-select.vue';
import SmStatusBadge from './sm-status-badge.vue';
import SmStatus from './sm-status.vue';
import SmStickySubnav from './sm-sticky-subnav.vue';
import SmTag from './sm-tag.vue';
import SmTags from './sm-tags.vue';
import SmTimeAgo from './sm-time-ago.vue';
import SmToggleScopeButton from './sm-toggle-scope-button.vue';
import SmWave from './sm-wave.vue';

export const components = {
  'sm-action-button-scoped': SmActionButtonScoped,
  'sm-alert': SmAlert,
  'sm-button-group': SmButtonGroup,
  'sm-button': SmButton,
  'sm-checkbox': SmCheckbox,
  'sm-confirm-button': SmConfirmButton,
  'sm-formatted-obj': SmFormattedObj,
  'sm-icon-button': SmIconButton,
  'sm-input': SmInput,
  'sm-select': SmSelect,
  'sm-key-value-table': SmKeyValueTable,
  'sm-loading-spinner': SmLoadingSpinner,
  'sm-modal': SmModal,
  'sm-pagination-nav': SmPaginationNav,
  'sm-panel': SmPanel,
  'sm-status-badge': SmStatusBadge,
  'sm-status': SmStatus,
  'sm-sticky-subnav': SmStickySubnav,
  'sm-tag': SmTag,
  'sm-tags': SmTags,
  'sm-time-ago': SmTimeAgo,
  'sm-toggle-scope-button': SmToggleScopeButton,
  'font-awesome-icon': FontAwesomeIcon,
  'sm-wave': SmWave,
};

export default {
  install(app) {
    for (const [name, component] of Object.entries(components)) {
      app.component(name, component);
    }
  },
};
