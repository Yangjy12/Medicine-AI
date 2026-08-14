export const formatDuration = (seconds = 0) => {
  const minute = Math.floor(seconds / 60)
  const second = seconds % 60
  return `${minute}:${String(second).padStart(2, '0')}`
}

export const compactNumber = (value = 0) => {
  if (value >= 10000) return `${(value / 10000).toFixed(1)}w`
  return String(value)
}

export const formatDateTime = (value?: string) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hour}:${minute}`
}
