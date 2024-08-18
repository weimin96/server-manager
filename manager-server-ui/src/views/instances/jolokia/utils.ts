export function truncateJavaType(javaType) {
  return javaType.replace(/java\.[^A-Z]*/g, '');
}

/**
 * Truncates the given package name to the given length.
 *
 * It works similar to {@link https://logback.qos.ch/manual/layouts.html#conversionWord}
 *
 * @param javaType
 * @param length
 * @returns {string|*}
 */
export function truncatePackageName(javaType, length) {
  const split = javaType.split('.');
  if (length > 0) {
    const clazzName = split.pop();

    let shortened;
    for (let i = 0; i <= split.length; i++) {
      shortened = [
        ...[...split].splice(0, i).map((p) => p.charAt(0)),
        ...[...split].splice(i),
        clazzName,
      ].join('.');

      if (shortened.length <= length) {
        return shortened;
      }
    }

    return shortened;
  } else {
    return split?.pop();
  }
}
