export interface User {
  id: string
  username: string
  email: string
  displayName: string
  bio?: string
  avatar?: string
  coverPhoto?: string
  gender?: 'MALE' | 'FEMALE' | 'OTHER'
  birthday?: string
  location?: string
  workplace?: string
  education?: string
  friendsCount?: number
  postsCount?: number
  createdAt: string
}

export interface Post {
  id: string
  content: string
  user: User
  visibility: 'PUBLIC' | 'FRIENDS' | 'ONLY_ME' | 'CUSTOM'
  likesCount: number
  commentsCount: number
  sharesCount: number
  media?: PostMedia[]
  createdAt: string
}

export interface PostMedia {
  id: string
  url: string
  thumbnailUrl?: string
  type: string
}

export interface Comment {
  id: string
  content: string
  user: User
  postId: string
  parentId?: string
  likesCount: number
  repliesCount: number
  depth: number
  replies?: Comment[]
  createdAt: string
}

export interface Reaction {
  id: string
  userId: string
  postId?: string
  commentId?: string
  type: 'LIKE' | 'LOVE' | 'HAHA' | 'WOW' | 'SAD' | 'ANGRY'
}

export interface FriendRequest {
  id: string
  requester: User
  addressee: User
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED'
  message?: string
  createdAt: string
}

export interface Conversation {
  id: string
  name?: string
  type: 'DIRECT' | 'GROUP'
  participants: User[]
  lastMessage?: Message
  lastMessageAt?: string
}

export interface Message {
  id: string
  conversationId: string
  sender: User
  content: string
  type: 'TEXT' | 'IMAGE' | 'FILE' | 'LINK'
  read: boolean
  createdAt: string
}

export interface Notification {
  id: string
  type: string
  message: string
  actor?: User
  referenceId: string
  referenceType: string
  read: boolean
  createdAt: string
}

export interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}
