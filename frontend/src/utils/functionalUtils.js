export const range = (start, end, increment=1) => {
  const length = (end - start) / increment
  return Array(length).fill().map((_, i) => start + (i * increment))
}
