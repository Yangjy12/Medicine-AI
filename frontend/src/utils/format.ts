export const formatDuration = (seconds = 0) => {
  const minute = Math.floor(seconds / 60)
  const second = seconds % 60
  return `${minute}:${String(second).padStart(2, '0')}`
}

export const compactNumber = (value = 0) => {
  if (value >= 10000) return `${(value / 10000).toFixed(1)}w`
  return String(value)
}
