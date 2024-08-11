//This is a Thymleaf template whill will be rendered by the backend
// eslint-disable-next-line @typescript-eslint/no-unused-vars
var SBA = {
  uiSettings: /*[[${uiSettings}]]*/ {},
  user: /*[[${user}]]*/ null,
  extensions: {
    js: /*[[${jsExtensions}]]*/ [],
    css: /*[[${cssExtensions}]]*/ [],
  },
  csrf: {
    parameterName: /*[[${_csrf} ? ${_csrf.parameterName} : 'null']]*/ null,
    headerName: /*[[${_csrf} ? ${_csrf.headerName} : 'null']]*/ null,
  },
};
