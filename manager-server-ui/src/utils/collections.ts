export const compareBy = (mapper) => (a, b) => {
  const valA = mapper(a);
  const valB = mapper(b);
  return valA > valB ? 1 : valA < valB ? -1 : 0;
};

export const anyValueMatches = (obj, predicate) => {
  if (Array.isArray(obj)) {
    return obj.some((e) => anyValueMatches(e, predicate));
  } else if (obj !== null && typeof obj === 'object') {
    return anyValueMatches(Object.values(obj), predicate);
  }
  return predicate(obj);
};
