/** 应用级行为常量，页面和 API 统一从这里读取。 */
export const appConfig = {
  pagination: {
    defaultPageSize: 10,
    commentPageSize: 20,
  },
  hot: {
    defaultLimit: 10,
    sidebarLimit: 5,
  },
} as const
