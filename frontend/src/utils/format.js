/**
 * 通用格式化工具
 */

/** 性别码 → 显示文案：1 男、2 女，其它（null/0/undefined）→ 未知 */
export function genderText(gender) {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '未知'
}