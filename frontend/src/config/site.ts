/**
 * 站点级配置。
 * 做成其他项目的模板时，优先修改这里，不要在页面中散落修改站点名称。
 */
export const siteConfig = {
  /** 浏览器标题和正式站点名称 */
  name: 'Community',
  /** 导航栏 Logo 使用的短名称 */
  brand: 'Community',
  description: '记录技术、生活与一切有趣的内容。',
} as const

export function pageTitle(page?: string): string {
  return page ? `${page} - ${siteConfig.name}` : siteConfig.name
}
