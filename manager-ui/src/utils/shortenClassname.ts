export default (fullName, targetLength) => {
  if (!fullName || fullName.length < targetLength) {
    return fullName;
  }

  const tokens = fullName.split('.');
  let shortened = tokens.pop();
  while (tokens.length > 0) {
    const next = tokens.pop();
    if (next.length + 1 + shortened.length < targetLength) {
      shortened = next + '.' + shortened;
    } else {
      shortened = next[0] + '.' + shortened;
    }
  }
  return shortened;
};
