import _Autolinker from 'autolinker';

export const defaults = {
  urls: {
    schemeMatches: true,
    wwwMatches: false,
    tldMatches: false,
  },
  email: false,
  phone: false,
  mention: false,
  hashtag: false,

  stripPrefix: false,
  stripTrailingSlash: false,
  newWindow: true,

  truncate: {
    length: 0,
    location: 'smart',
  },

  className: '',
};
const autolinker = new _Autolinker(defaults);

export default (s) => autolinker.link(s);
export function Autolink(cfg) {
  this.autolinker = new _Autolinker({ ...defaults, ...cfg });
  return (s) => this.autolinker.link(s);
}
