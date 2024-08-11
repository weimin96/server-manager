import { EMPTY, Observable, catchError, concatMap, of, timer } from './rxjs';

export default (getFn, interval, initialSize = 300 * 1024) => {
  let range = `bytes=-${initialSize}`;
  let size = 0;

  return timer(0, interval).pipe(
    concatMap(() => {
      return new Observable((observer) => {
        getFn({ headers: { range, Accept: 'text/plain' } })
          .then((response) => {
            observer.next(response);
            observer.complete();
          })
          .catch((error) => observer.error(error));
      }).pipe(
        catchError((error) => of({ data: '', status: error.response.status })),
      );
    }),
    concatMap((response) => {
      let initial = size === 0;
      const contentLength = response.data.length;

      if (response.status === 200) {
        if (!initial) {
          throw 'Expected 206 - Partial Content on subsequent requests.';
        }
        size = contentLength;
        range = `bytes=${size - 1}-`;
      } else if (response.status === 206) {
        size = parseInt(response.headers['content-range'].split('/')[1]);
        range = `bytes=${size - 1}-`;
      } else if (response.status === 416) {
        size = 0;
        range = `bytes=-${initialSize}`;
        initial = true;
      } else {
        throw 'Unexpected response status: ' + response.status;
      }

      let addendum = null;
      let skipped = 0;

      if (initial) {
        if (contentLength >= size) {
          addendum = response.data;
        } else {
          // In case of a partial response find the first line break.
          addendum = response.data.substring(response.data.indexOf('\n') + 1);
          skipped = size - addendum.length;
        }
      } else if (response.data.length > 1) {
        // Remove the first byte which has been part of the previos response.
        addendum = response.data.substring(1);
      }

      return addendum
        ? of({
            totalBytes: size,
            skipped,
            addendum,
          })
        : EMPTY;
    }),
  );
};
