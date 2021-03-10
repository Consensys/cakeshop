import moment from 'moment'

export function convertTimestampToMillis (timestamp) {
  if (timestamp > 1000000000000000000) {
    // probably nanoseconds
    timestamp = timestamp / 1000000
  } else if (timestamp > 1000000000000000) {
    // probably microseconds
    timestamp = timestamp / 1000
  } else if (timestamp < 1000000000000) {
    // probably seconds
    timestamp = timestamp * 1000
  }
  // now that we're in millis, get user friendly time
  return Math.round(timestamp)
}

export function dateFromTimestamp (timestamp) {
  return moment(convertTimestampToMillis(timestamp)).format('YYYY-MM-DD hh:mm A')
}

export function relativeDateFromTimestamp (timestamp) {
  return moment(convertTimestampToMillis(timestamp)).fromNow()
}
