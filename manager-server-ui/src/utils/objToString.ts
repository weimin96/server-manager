import { isEmpty, isObject } from 'lodash-es';

const nonEmptyComplexValue = (value) =>
  (Array.isArray(value) || isObject(value)) && !isEmpty(value);

const objToString = (obj, indent = '') => {
  if (Array.isArray(obj)) {
    if (isEmpty(obj)) {
      return indent + '[]';
    }

    return obj
      .map((value) => {
        if (nonEmptyComplexValue(value)) {
          return `${indent}-\n${objToString(value, indent + '  ')}`;
        }
        return `${indent}- ${objToString(value, '')}`;
      })
      .join('\n');
  }

  if (isObject(obj)) {
    if (isEmpty(obj)) {
      return indent + '{}';
    }

    return Object.entries(obj)
      .map(([key, value]) => {
        if (nonEmptyComplexValue(value)) {
          return `${indent}${key}:\n${objToString(value, indent + '  ')}`;
        }
        return `${indent}${key}:${objToString(value, ' ')}`;
      })
      .join('\n');
  }

  if (obj === null) {
    return indent + 'null';
  }

  if (typeof obj === 'undefined' || obj === '') {
    return '';
  }

  return indent + obj;
};

export default objToString;
