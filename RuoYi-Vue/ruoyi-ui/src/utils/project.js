export const PROJECT_STATUS_OPTIONS = [
  { value: '0', label: '草稿' },
  { value: '1', label: '审批中' },
  { value: '2', label: '已立项' },
  { value: '3', label: '已驳回' },
  { value: '4', label: '进行中' },
  { value: '5', label: '已完工' }
]

export function projectStatusLabel(status) {
  const item = PROJECT_STATUS_OPTIONS.find(i => i.value === status)
  return item ? item.label : status
}

export function projectStatusTagType(status) {
  return ({ '0': 'info', '1': 'warning', '2': 'success', '3': 'danger', '4': '', '5': 'success' })[status] || 'info'
}

export function formatMoney(value) {
  const amount = Number(value || 0)
  return amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

export function canEditProject(status) {
  return status === '0' || status === '3'
}

export const WORK_HOUR_STATUS_OPTIONS = [
  { value: '0', label: '草稿' },
  { value: '1', label: '审批中' },
  { value: '2', label: '已通过' },
  { value: '3', label: '已驳回' },
  { value: '4', label: '已入账' }
]

export function workHourStatusLabel(status) {
  const item = WORK_HOUR_STATUS_OPTIONS.find(i => i.value === status)
  return item ? item.label : status
}

export function workHourStatusTagType(status) {
  return ({ '0': 'info', '1': 'warning', '2': 'success', '3': 'danger', '4': 'success' })[status] || 'info'
}

export function canEditWorkHour(status) {
  return status === '0' || status === '3'
}

export function formatHours(value) {
  const amount = Number(value || 0)
  return amount.toLocaleString('zh-CN', { minimumFractionDigits: 1, maximumFractionDigits: 1 })
}

export const REIMBURSEMENT_STATUS_OPTIONS = [...WORK_HOUR_STATUS_OPTIONS]

export function reimbursementStatusLabel(status) {
  return workHourStatusLabel(status)
}

export function reimbursementStatusTagType(status) {
  return workHourStatusTagType(status)
}

export function canEditReimbursement(status) {
  return status === '0' || status === '3'
}

export const ALERT_LEVEL_OPTIONS = [
  { value: '1', label: '黄色预警' },
  { value: '2', label: '橙色预警' },
  { value: '3', label: '红色预警' }
]

export const ALERT_STATUS_OPTIONS = [
  { value: '0', label: '待处理' },
  { value: '1', label: '已确认' },
  { value: '2', label: '已忽略' },
  { value: '3', label: '跟进中' },
  { value: '4', label: '已关闭' }
]

export const ALERT_RULE_TYPE_OPTIONS = [
  { value: 'EXEC_RATE', label: '预算执行率' },
  { value: 'SINGLE_AMOUNT', label: '单笔金额' },
  { value: 'BALANCE_RATE', label: '预算余额率' },
  { value: 'OVERDUE', label: '项目逾期' },
  { value: 'INACTIVE', label: '长期无入账' }
]

export function alertLevelLabel(level) {
  const item = ALERT_LEVEL_OPTIONS.find(i => i.value === level)
  return item ? item.label : level
}

export function alertLevelTagType(level) {
  return ({ '1': 'warning', '2': '', '3': 'danger' })[level] || 'info'
}

export function alertStatusLabel(status) {
  const item = ALERT_STATUS_OPTIONS.find(i => i.value === status)
  return item ? item.label : status
}

export function alertStatusTagType(status) {
  return ({ '0': 'danger', '1': 'success', '2': 'info', '3': 'warning', '4': 'info' })[status] || 'info'
}

export function alertRuleTypeLabel(type) {
  const item = ALERT_RULE_TYPE_OPTIONS.find(i => i.value === type)
  return item ? item.label : type
}
