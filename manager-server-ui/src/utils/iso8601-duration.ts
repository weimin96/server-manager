export { parse } from 'iso8601-duration';

/**
 * Convert ISO8601 duration object to milliseconds
 *
 * Hint: Years and months are ignored.
 * Calculating based on JavaScript date is too imprecise.
 *
 * @param {Object} duration - The duration object
 * @return {Number}
 */
export const toMilliseconds = (duration) => {
  let result = duration.seconds;
  result += duration.minutes * 60;
  result += duration.hours * 60 * 60;
  result += duration.days * 60 * 60 * 24;
  result += duration.weeks * 60 * 60 * 24 * 7;

  return result * 1000;
};
