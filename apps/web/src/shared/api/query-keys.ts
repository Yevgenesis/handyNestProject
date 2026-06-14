export const queryKeys = {
  marketConfig: ["market-config"] as const,
  userProfile: ["user-profile"] as const,
  consentRequirements: ["consent-requirements"] as const,
  userConsents: ["user-consents"] as const,
  categories: (locale: string) => ["categories", locale] as const,
  cities: (locale: string) => ["cities", locale] as const,
  districts: (cityId: string, locale: string) =>
    ["districts", cityId, locale] as const,
  myTasks: (status?: string, page = 0) =>
    ["my-tasks", status ?? "all", page] as const,
  task: (taskId: string) => ["task", taskId] as const,
  taskOffers: (taskId: string) => ["task-offers", taskId] as const,
  taskAttachments: (taskId: string) => ["task-attachments", taskId] as const,
  myOffers: ["my-offers"] as const,
  myDeals: ["my-deals"] as const,
  deal: (dealId: string) => ["deal", dealId] as const,
  taskFeedbacks: (taskId: string) => ["task-feedbacks", taskId] as const,
  performerFeedbacks: (performerId: string) =>
    ["performer-feedbacks", performerId] as const,
  notifications: (unreadOnly: boolean, page: number) =>
    ["notifications", unreadOnly, page] as const,
  unreadNotifications: ["notifications", "unread-count"] as const,
  chats: ["chats"] as const,
  chat: (chatId: string) => ["chat", chatId] as const,
  chatTimeline: (chatId: string) => ["chat-timeline", chatId] as const,
  chatAttachments: (chatId: string) => ["chat-attachments", chatId] as const,
  performerMe: ["performer-me"] as const,
  verificationDocuments: ["verification-documents"] as const,
  verificationRequests: ["verification-requests"] as const,
};
