# System Flow

Mô tả chi tiết flow của từng chức năng trong hệ thống.

---

## Table of Contents
1. [Authentication Flow](#1-authentication-flow)
2. [News Feed Flow](#2-news-feed-flow)
3. [Post Flow](#3-post-flow)
4. [Comment Flow](#4-comment-flow)
5. [Reaction Flow](#5-reaction-flow)
6. [Friend System Flow](#6-friend-system-flow)
7. [Messaging Flow](#7-messaging-flow)
8. [Notification Flow](#8-notification-flow)
9. [Media Upload Flow](#9-media-upload-flow)
10. [Search Flow](#10-search-flow)
11. [User Profile Flow](#11-user-profile-flow)
12. [Admin Flow](#12-admin-flow)

---

## 1. Authentication Flow

### 1.1 Register
```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  Client  │────▶│  API     │────▶│  Service │────▶│  DB      │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
     │                                  │
     │  1. POST /api/v1/auth/register   │
     │     {email, password, name}      │
     │─────────────────────────────────▶│
     │                                  │
     │                                  ├─▶ Validate input
     │                                  ├─▶ Check email uniqueness
     │                                  ├─▶ Hash password (BCrypt)
     │                                  ├─▶ Create user entity
     │                                  ├─▶ Generate verification token
     │                                  ├─▶ Save to DB
     │                                  ├─▶ Send verification email
     │                                  │
     │  2. Response: 201 Created        │
     │     {message, userId}            │
     │◀─────────────────────────────────│
```

### 1.2 Login
```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  Client   │────▶│  API     │────▶│  Service │────▶│  Redis   │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
     │                                  │
     │  1. POST /api/v1/auth/login      │
     │     {email, password}            │
     │─────────────────────────────────▶│
     │                                  │
     │                                  ├─▶ Find user by email
     │                                  ├─▶ Verify password (BCrypt)
     │                                  ├─▶ Check if email verified
     │                                  ├─▶ Check if account locked
     │                                  ├─▶ Generate JWT access token (15min)
     │                                  │   (Nimbus JOSE+JWT via Spring Security)
     │                                  ├─▶ Generate JWT refresh token (7days)
     │                                  ├─▶ Store session in Redis
     │                                  │
     │  2. Response: 200 OK             │
     │     {accessToken, refreshToken,  │
     │      user}                       │
     │◀─────────────────────────────────│
```

### 1.3 Refresh Token
```
Client                           API                            Redis
  │                                │                               │
  │  1. POST /api/v1/auth/refresh  │                               │
  │     {refreshToken}             │                               │
  │───────────────────────────────▶│                               │
  │                                │                               │
  │                                ├─▶ Verify refresh token JWT    │
  │                                ├─▶ Check token in Redis        │
  │                                ├─▶ Generate new access token   │
  │                                │                               │
  │  2. Response: 200 OK           │                               │
  │     {accessToken}              │                               │
  │◀───────────────────────────────│                               │
```

### 1.4 Logout
```
Client                           API                            Redis
  │                                │                               │
  │  1. POST /api/v1/auth/logout   │                               │
  │     Authorization: Bearer JWT  │                               │
  │───────────────────────────────▶│                               │
  │                                │                               │
  │                                ├─▶ Verify JWT                  │
  │                                ├─▶ Delete session from Redis   │
  │                                │                               │
  │  2. Response: 200 OK           │                               │
  │◀───────────────────────────────│                               │
```

### 1.5 Authentication Middleware
```
Every API Request:
  
  Client ──▶ Nginx ──▶ Spring Boot
                          │
                          ├─▶ SecurityFilterChain
                          │     │
                          │     ├─▶ Extract JWT from header
                          │     ├─▶ JwtDecoder (Nimbus JOSE+JWT)
                          │     │     ├─▶ Validate JWT signature
                          │     │     ├─▶ Check token expiration
                          │     │     └─▶ Extract claims
                          │     ├─▶ Check token in Redis (not revoked)
                          │     ├─▶ Extract user claims
                          │     └─▶ Set SecurityContext
                          │
                          ├─▶ If valid → Continue to controller
                          └─▶ If invalid → 401 Unauthorized
```

---

## 2. News Feed Flow

### 2.1 Fanout-on-Write (Write Path)
```
User creates post
      │
      ▼
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│ Post Service │────▶│ Event Bus    │────▶│ Feed Service │
└──────────────┘     └──────────────┘     └──────────────┘
      │                                        │
      ├─▶ Save post to DB                     │
      │                                        │
      │                                        ├─▶ Get user's friends list
      │                                        ├─▶ For each friend:
      │                                        │     └─▶ Add postId to friend's feed cache
      │                                        │
      │                                        └─▶ Store in Redis
      │                                              Key: feed:{userId}
      │                                              Value: Sorted Set (timestamp, postId)
```

### 2.2 Feed Read (Read Path)
```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  Client   │────▶│  API     │────▶│  Service │────▶│  Redis   │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
     │                                  │                │
     │  1. GET /api/v1/posts            │                │
     │     ?cursor={timestamp}          │                │
     │     &limit=20                    │                │
     │─────────────────────────────────▶│                │
     │                                  │                │
     │                                  ├─▶ Get feed from Redis
     │                                  │   (cache hit)   │
     │                                  │◀───────────────│
     │                                  │
     │                                  ├─▶ If cache miss → Query DB
     │                                  │
     │                                  ├─▶ Fetch post details
     │                                  ├─▶ Fetch user info for each post
     │                                  ├─▶ Fetch reaction counts
     │                                  ├─▶ Fetch comment counts
     │                                  │
     │  2. Response: 200 OK             │
     │     {posts, nextCursor}          │
     │◀─────────────────────────────────│
```

### 2.3 Feed Pagination (Cursor-based)
```
Client                          API
  │                               │
  │  1st request:                 │
  │  GET /api/v1/posts            │
  │  limit=20                     │
  │──────────────────────────────▶│
  │                               │
  │  Response:                    │
  │  {                            │
  │    posts: [...20 items],      │
  │    nextCursor: "1718456789"   │
  │  }                            │
  │◀──────────────────────────────│
  │                               │
  │  2nd request:                 │
  │  GET /api/v1/posts            │
  │  ?cursor=1718456789           │
  │  &limit=20                    │
  │──────────────────────────────▶│
  │                               │
  │  Response:                    │
  │  {                            │
  │    posts: [...20 items],      │
  │    nextCursor: "1718456700"   │
  │  }                            │
  │◀──────────────────────────────│
```

---

## 3. Post Flow

### 3.1 Create Post
```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  Client   │────▶│  API     │────▶│  Service │────▶│  DB +    │
│           │     │          │     │          │     │  Redis   │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
     │                                  │
     │  1. POST /api/v1/posts           │
     │     {content, images, privacy}   │
     │─────────────────────────────────▶│
     │                                  │
     │                                  ├─▶ Validate input
     │                                  ├─▶ Check user authentication
     │                                  ├─▶ Check rate limit (20 posts/hour)
     │                                  ├─▶ Upload images to MinIO
     │                                  ├─▶ Create post entity
     │                                  ├─▶ Save to DB
     │                                  ├─▶ Publish event: PostCreated
     │                                  │
     │  2. Response: 201 Created        │
     │     {post}                       │
     │◀─────────────────────────────────│
```

### 3.2 Edit Post
```
Client                          API                          Service
  │                               │                            │
  │  1. PUT /api/v1/posts/{id}    │                            │
  │     {content, privacy}        │                            │
  │──────────────────────────────▶│                            │
  │                               │───────────────────────────▶│
  │                               │                            │
  │                               │                            ├─▶ Find post by ID
  │                               │                            ├─▶ Check ownership
  │                               │                            ├─▶ Update fields
  │                               │                            ├─▶ Save to DB
  │                               │                            │
  │  2. Response: 200 OK          │                            │
  │     {post}                    │◀───────────────────────────│
  │◀──────────────────────────────│                            │
```

### 3.3 Delete Post (Soft Delete)
```
Client                          API                          Service
  │                               │                            │
  │  1. DELETE /api/v1/posts/{id} │                            │
  │──────────────────────────────▶│                            │
  │                               │───────────────────────────▶│
  │                               │                            │
  │                               │                            ├─▶ Find post by ID
  │                               │                            ├─▶ Check ownership or admin
  │                               │                            ├─▶ Set deletedAt = now()
  │                               │                            ├─▶ Remove from feed cache
  │                               │                            │
  │  2. Response: 200 OK          │                            │
  │◀──────────────────────────────│◀───────────────────────────│
```

### 3.4 Post Visibility Rules
```
Post.privacy:
  │
  ├─▶ PUBLIC
  │     └─▶ Anyone can see (searchable, appears in feed)
  │
  ├─▶ FRIENDS
  │     └─▶ Only friends can see
  │     └─▶ Check friendship status before showing
  │
  ├─▶ ONLY_ME
  │     └─▶ Only post owner can see
  │
  └─▶ CUSTOM
        └─▶ Specific users can/cannot see
        └─▶ Check custom list before showing
```

---

## 4. Comment Flow

### 4.1 Add Comment
```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  Client   │────▶│  API     │────▶│  Service │────▶│  DB      │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
     │                                  │
     │  1. POST /api/v1/posts/{id}/     │
     │     comments                     │
     │     {content, parentId}          │
     │─────────────────────────────────▶│
     │                                  │
     │                                  ├─▶ Validate input
     │                                  ├─▶ Check rate limit (30/min)
     │                                  ├─▶ Check nesting depth (max 3)
     │                                  ├─▶ Create comment entity
     │                                  ├─▶ Save to DB
     │                                  ├─▶ Increment post comment count
     │                                  ├─▶ Publish event: CommentCreated
     │                                  │
     │  2. Response: 201 Created        │
     │     {comment}                    │
     │◀─────────────────────────────────│
```

### 4.2 Comment Threading
```
Post
  │
  ├─▶ Comment 1 (root)
  │     ├─▶ Reply 1.1 (depth: 1)
  │     │     ├─▶ Reply 1.1.1 (depth: 2)
  │     │     └─▶ Reply 1.1.2 (depth: 2)
  │     └─▶ Reply 1.2 (depth: 1)
  │
  └─▶ Comment 2 (root)
        └─▶ Reply 2.1 (depth: 1)

Depth Limit: 3 levels
```

### 4.3 Comment Query
```
GET /api/v1/posts/{postId}/comments?sort=newest

Response Structure:
{
  comments: [
    {
      id: 1,
      content: "Root comment",
      author: { id, name, avatar },
      createdAt: "2025-01-15T10:30:00Z",
      likesCount: 5,
      replies: [                    // Nested replies
        {
          id: 2,
          content: "Reply to comment",
          author: { id, name, avatar },
          createdAt: "2025-01-15T11:00:00Z",
          likesCount: 2,
          replies: []
        }
      ]
    }
  ],
  total: 50,
  nextCursor: null
}
```

---

## 5. Reaction Flow

### 5.1 Add/Remove Reaction
```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  Client   │────▶│  API     │────▶│  Service │────▶│  DB +    │
│           │     │          │     │          │     │  Cache   │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
     │                                  │
     │  1. POST /api/v1/posts/{id}/     │
     │     reactions                    │
     │     {type: "LIKE"}               │
     │─────────────────────────────────▶│
     │                                  │
     │                                  ├─▶ Check if user already reacted
     │                                  │   ├─▶ If same type → Remove (toggle)
     │                                  │   └─▶ If different → Update type
     │                                  │
     │                                  ├─▶ Create/Update reaction
     │                                  ├─▶ Update reaction counts in cache
     │                                  ├─▶ Publish event: ReactionChanged
     │                                  │
     │  2. Response: 200 OK             │
     │     {reaction, counts}           │
     │◀─────────────────────────────────│
```

### 5.2 Reaction Types
```
┌─────────┬───────────┬──────────────┐
│  Type   │  Emoji    │  Description │
├─────────┼───────────┼──────────────┤
│ LIKE    │   👍      │  Like        │
│ LOVE    │   ❤️      │  Love        │
│ HAHA    │   😂      │  Laugh       │
│ WOW     │   😮      │  Surprise    │
│ SAD     │   😢      │  Sad         │
│ ANGRY   │   😠      │  Angry       │
└─────────┴───────────┴──────────────┘
```

### 5.3 Reaction Aggregation
```
Post Reaction Stats (Redis Cache):
  Key: reaction:post:{postId}
  Hash: {
    "LIKE": 150,
    "LOVE": 45,
    "HAHA": 23,
    "WOW": 12,
    "SAD": 5,
    "ANGRY": 2
  }

User Reaction Status:
  Key: user_reaction:{userId}:{postId}
  Value: "LIKE" (or null if no reaction)
```

---

## 6. Friend System Flow

### 6.1 Send Friend Request
```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  Client A │────▶│  API     │────▶│  Service │────▶│  DB +    │
│           │     │          │     │          │     │  Redis   │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
     │                                  │
     │  1. POST /api/v1/friends/request │
     │     {userId: B, message}         │
     │─────────────────────────────────▶│
     │                                  │
     │                                  ├─▶ Check: A ≠ B
     │                                  ├─▶ Check: Not blocked
     │                                  ├─▶ Check: No existing request
     │                                  ├─▶ Check: Not already friends
     │                                  ├─▶ Check: Max 5000 friends
     │                                  ├─▶ Check: Max 20 pending requests
     │                                  ├─▶ Create friend request (status: PENDING)
     │                                  ├─▶ Save to DB
     │                                  ├─▶ Publish event: FriendRequestSent
     │                                  │
     │  2. Response: 201 Created        │
     │     {request}                    │
     │◀─────────────────────────────────│
     │                                  │
     │                              ┌───┴───┐
     │                              │ Notify│
     │                              │ User B│
     │                              └───────┘
```

### 6.2 Accept/Reject Request
```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  Client B │────▶│  API     │────▶│  Service │────▶│  DB +    │
│           │     │          │     │          │     │  Redis   │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
     │                                  │
     │  1. PUT /api/v1/friends/{id}/    │
     │     accept                       │
     │─────────────────────────────────▶│
     │                                  │
     │                                  ├─▶ Find request by ID
     │                                  ├─▶ Check: B is recipient
     │                                  ├─▶ Check: status is PENDING
     │                                  │
     │                                  ├─▶ Update status: ACCEPTED
     │                                  ├─▶ Create friendship record
     │                                  ├─▶ Update friend counts
     │                                  ├─▶ Add to feed cache (both users)
     │                                  ├─▶ Publish event: FriendAccepted
     │                                  │
     │  2. Response: 200 OK             │
     │◀─────────────────────────────────│
```

### 6.3 Friend Request States
```
┌─────────┐    accept    ┌──────────┐
│ PENDING │─────────────▶│ ACCEPTED │
└─────────┘              └──────────┘
     │
     │ reject
     ▼
┌──────────┐
│ REJECTED │
└──────────┘

┌─────────┐    cancel    ┌──────────┐
│ PENDING │─────────────▶│ CANCELLED│
└─────────┘              └──────────┘
```

### 6.4 Unfriend
```
Client A                        API                          Service
  │                               │                            │
  │  1. DELETE /api/v1/friends/{B}│                            │
  │──────────────────────────────▶│                            │
  │                               │                            │
  │                               │                            ├─▶ Find friendship
  │                               │                            ├─▶ Check: A and B are friends
  │                               │                            ├─▶ Delete friendship record
  │                               │                            ├─▶ Remove from feed cache
  │                               │                            ├─▶ Publish event: Unfriended
  │                               │                            │
  │  2. Response: 200 OK          │                            │
  │◀──────────────────────────────│◀───────────────────────────│
```

### 6.5 Block User
```
Client A                        API                          Service
  │                               │                            │
  │  1. POST /api/v1/users/{B}/   │                            │
  │     block                      │                            │
  │──────────────────────────────▶│                            │
  │                               │                            │
  │                               │                            ├─▶ Create block record
  │                               │                            ├─▶ If friends → Unfriend
  │                               │                            ├─▶ Remove from feed cache
  │                               │                            ├─▶ Add to blocked list
  │                               │                            │
  │  2. Response: 200 OK          │                            │
  │◀──────────────────────────────│◀───────────────────────────│

Effects of blocking:
  - B cannot see A's posts
  - A cannot see B's posts
  - B cannot send messages to A
  - B cannot send friend requests to A
  - B cannot see A's profile details
```

---

## 7. Messaging Flow

### 7.1 WebSocket Connection
```
┌──────────┐     ┌──────────┐     ┌──────────┐
│  Client   │────▶│  Socket  │────▶│  Redis   │
│           │     │  Server  │     │  Pub/Sub │
└──────────┘     └──────────┘     └──────────┘
     │                                  │
     │  1. Connect with JWT             │
     │     socket.connect({token})      │
     │─────────────────────────────────▶│
     │                                  │
     │                                  ├─▶ Verify JWT
     │                                  ├─▶ Store connection mapping
     │                                  │   (userId → socketId)
     │                                  ├─▶ Publish: user online
     │                                  │
     │  2. Connection established       │
     │◀─────────────────────────────────│
```

### 7.2 Send Message
```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│ Client A  │────▶│  Socket  │────▶│  Message │────▶│  DB +    │
│           │     │  Server  │     │  Service │     │  Redis   │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
     │                                  │
     │  1. Emit: message:send           │
     │     {conversationId, content,    │
     │      type: "text"}               │
     │─────────────────────────────────▶│
     │                                  │
     │                                  ├─▶ Validate input
     │                                  ├─▶ Check rate limit
     │                                  ├─▶ Check membership
     │                                  ├─▶ Create message entity
     │                                  ├─▶ Save to DB
     │                                  ├─▶ Update conversation last message
     │                                  │
     │                                  ├─▶ Find recipient socket
     │                                  ├─▶ Emit to recipient:
     │                                  │   message:new {message}
     │                                  │
     │  2. Ack: message:sent            │
     │     {messageId, timestamp}       │
     │◀─────────────────────────────────│
```

### 7.3 Message Flow Diagram
```
┌─────────┐                        ┌─────────┐
│ User A  │                        │ User B  │
└────┬────┘                        └────┬────┘
     │                                  │
     │  ┌────────────────────────────┐  │
     │  │       WebSocket Server     │  │
     │  └────────────────────────────┘  │
     │                                  │
     │  1. message:send                 │
     │─────────────────────────────────▶│
     │                                  │
     │                    ┌─────────────┤
     │                    │ Save to DB  │
     │                    └─────────────┤
     │                                  │
     │                    ┌─────────────┤
     │                    │ Push to B   │
     │                    └─────────────┤
     │                                  │
     │                                  │ 2. message:new
     │                                  │◀─────────────
     │                                  │
     │  3. message:read                 │
     │─────────────────────────────────▶│
     │                                  │
     │                    ┌─────────────┤
     │                    │ Mark read   │
     │                    └─────────────┤
     │                                  │
     │  4. message:read:ack             │
     │◀─────────────────────────────────│
```

### 7.4 Typing Indicator
```
User A typing:
  1. User A types in input
  2. Debounce 300ms
  3. Emit: typing:start {conversationId}
  4. Server broadcasts to User B
  5. User B sees "A is typing..."

User A stops typing:
  1. User A stops for 2 seconds
  2. Emit: typing:stop {conversationId}
  3. Server broadcasts to User B
  4. User B hides typing indicator
```

### 7.5 Online Status
```
Redis Status:
  Key: user:status:{userId}
  Value: {
    status: "online" | "offline" | "away",
    lastSeen: "2025-01-15T10:30:00Z",
    socketId: "abc123"
  }
  TTL: 300 seconds (heartbeat renewal)

On Connect:
  1. Set status: online
  2. Publish: user:online {userId}

On Disconnect:
  1. Set status: offline, lastSeen: now()
  2. Publish: user:offline {userId}
```

---

## 8. Notification Flow

### 8.1 Notification Creation
```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│ Event Bus    │────▶│ Notification │────▶│  DB +        │
│              │     │ Service      │     │  WebSocket   │
└──────────────┘     └──────────────┘     └──────────────┘
                          │
                          ├─▶ Receive event (e.g., FriendRequestSent)
                          ├─▶ Determine recipients
                          ├─▶ Create notification entity
                          ├─▶ Save to DB
                          ├─▶ Check user preferences
                          ├─▶ If WebSocket connected → Push real-time
                          ├─▶ If email enabled → Queue email
                          │
                          └─▶ Publish: notification:new
```

### 8.2 Notification Types
```
┌────────────────────┬──────────────────────────────────────┐
│  Type              │  Trigger                             │
├────────────────────┼──────────────────────────────────────┤
│ FRIEND_REQUEST     │  Someone sends friend request        │
│ FRIEND_ACCEPT      │  Someone accepts friend request      │
│ POST_LIKE          │  Someone likes your post             │
│ POST_COMMENT       │  Someone comments on your post       │
│ COMMENT_LIKE       │  Someone likes your comment          │
│ COMMENT_REPLY      │  Someone replies to your comment     │
│ MESSAGE            │  Someone sends you a message         │
│ MENTION            │  Someone mentions you in post/comment│
│ SYSTEM             │  System announcements                │
└────────────────────┴──────────────────────────────────────┘
```

### 8.3 Notification Delivery
```
Priority Levels:
  
  P0 (Real-time via WebSocket):
    - New message
    - Friend request
    - Mention
  
  P1 (WebSocket + Badge update):
    - Post reaction
    - Comment on post
    - Friend accepted
  
  P2 (Batch, periodic):
    - Daily digest
    - Weekly summary

Delivery Guarantees:
  - At-least-once delivery
  - Deduplication on client
  - Read status tracking
```

---

## 9. Media Upload Flow

### 9.1 Image Upload
```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  Client   │────▶│  API     │────▶│  Service │────▶│  MinIO   │
│           │     │          │     │          │     │  (S3)    │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
     │                                  │
     │  1. POST /api/v1/media/upload    │
     │     multipart/form-data          │
     │─────────────────────────────────▶│
     │                                  │
     │                                  ├─▶ Validate file type
     │                                  ├─▶ Validate file size
     │                                  ├─▶ Generate unique filename
     │                                  ├─▶ Upload to MinIO
     │                                  ├─▶ Generate thumbnail (async)
     │                                  ├─▶ Save metadata to DB
     │                                  │
     │  2. Response: 201 Created        │
     │     {mediaId, url, thumbnailUrl} │
     │◀─────────────────────────────────│
```

### 9.2 Upload Processing Pipeline
```
Original Image
      │
      ▼
┌─────────────┐
│  Validate   │ ──▶ Check format, size
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Compress   │ ──▶ Optimize quality
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Resize     │ ──▶ Generate sizes:
│             │     - Original
│             │     - Large (1200px)
│             │     - Medium (600px)
│             │     - Thumbnail (150px)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Upload     │ ──▶ Store all sizes in MinIO
│  to MinIO   │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Save       │ ──▶ Store metadata in DB
│  Metadata   │     (path, sizes, mime type)
└─────────────┘
```

### 9.3 File Size Limits
```
┌──────────────────┬──────────────┬────────────────┐
│  Type            │  Max Size    │  Formats       │
├──────────────────┼──────────────┼────────────────┤
│  Avatar          │  5MB         │  JPEG,PNG,WebP │
│  Cover Photo     │  10MB        │  JPEG,PNG,WebP │
│  Post Image      │  10MB        │  JPEG,PNG,WebP,GIF │
│  Message Image   │  10MB        │  JPEG,PNG,WebP,GIF │
│  Message File    │  50MB        │  All           │
└──────────────────┴──────────────┴────────────────┘
```

---

## 10. Search Flow

### 10.1 Global Search
```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  Client   │────▶│  API     │────▶│  Service │────▶│  ES      │
│           │     │          │     │          │     │          │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
     │                                  │
     │  1. GET /api/v1/search?q=john    │
     │─────────────────────────────────▶│
     │                                  │
     │                                  ├─▶ Query Elasticsearch
     │                                  │   ├─▶ Search users (name, username)
     │                                  │   ├─▶ Search posts (content)
     │                                  │
     │                                  ├─▶ Apply privacy filters
     │                                  ├─▶ Rank by relevance
     │                                  │
     │  2. Response: 200 OK             │
     │     {                            │
     │       users: [...],              │
     │       posts: [...],              │
     │       total: 150                 │
     │     }                            │
     │◀─────────────────────────────────│
```

### 10.2 Search Autocomplete
```
Client                          API                          ES
  │                               │                            │
  │  1. GET /api/v1/search/       │                            │
  │     autocomplete?q=joh        │                            │
  │──────────────────────────────▶│                            │
  │                               │                            │
  │                               ├─▶ Prefix query to ES       │
  │                               │   (max 10 results)         │
  │                               │                            │
  │  2. Response: 200 OK          │                            │
  │     {suggestions: [           │                            │
  │       "John Doe",             │                            │
  │       "Johnny",               │                            │
  │       "Johnson"               │                            │
  │     ]}                        │                            │
  │◀──────────────────────────────│◀───────────────────────────│
```

---

## 11. User Profile Flow

### 11.1 View Profile
```
Client                          API                          Service
  │                               │                            │
  │  1. GET /api/v1/users/{id}    │                            │
  │──────────────────────────────▶│                            │
  │                               │                            │
  │                               │                            ├─▶ Find user by ID
  │                               │                            ├─▶ Check privacy settings
  │                               │                            ├─▶ Check relationship
  │                               │                            │   (friend, blocked, etc.)
  │                               │                            │
  │                               │                            ├─▶ If PUBLIC → Return full profile
  │                               │                            ├─▶ If FRIENDS → Check friendship
  │                               │                            ├─▶ If PRIVATE → Return limited info
  │                               │                            │
  │  2. Response: 200 OK          │                            │
  │     {user profile}            │                            │
  │◀──────────────────────────────│◀───────────────────────────│
```

### 11.2 Profile Data
```
User Profile Response:
{
  id: "uuid",
  username: "johndoe",
  displayName: "John Doe",
  avatar: "https://minio.../avatar.jpg",
  coverPhoto: "https://minio.../cover.jpg",
  bio: "Software developer",
  location: "Ho Chi Minh City",
  workplace: "Tech Company",
  education: "University of Technology",
  joinedAt: "2024-01-15T00:00:00Z",
  
  // Computed fields
  friendsCount: 250,
  postsCount: 150,
  mutualFriends: 5,        // If viewing other's profile
  isFriend: true,           // If viewing other's profile
  hasPendingRequest: false, // If viewing other's profile
  isBlocked: false
}
```

---

## 12. Admin Flow

### 12.1 Report Content
```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  Client   │────▶│  API     │────▶│  Service │────▶│  DB      │
│           │     │          │     │          │     │          │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
     │                                  │
     │  1. POST /api/v1/reports         │
     │     {targetType, targetId,       │
     │      reason, description}        │
     │─────────────────────────────────▶│
     │                                  │
     │                                  ├─▶ Validate input
     │                                  ├─▶ Check if already reported
     │                                  ├─▶ Create report entity
     │                                  ├─▶ Save to DB
     │                                  ├─▶ Notify moderators
     │                                  │
     │  2. Response: 201 Created        │
     │     {report}                     │
     │◀─────────────────────────────────│
```

### 12.2 Moderate Content
```
Admin Dashboard:
  
  1. View Reports
     └─▶ List all reports with filters
         (type, status, date range)
  
  2. Review Report
     └─▶ Show reported content
         Show reporter info
         Show context
  
  3. Take Action
     ├─▶ Dismiss Report (no action)
     ├─▶ Remove Content
     ├─▶ Warn User
     ├─▶ Temporarily Ban User
     └─▶ Permanently Ban User
  
  4. Audit Log
     └─▶ Log all admin actions
         (who, what, when, why)
```

---

## Cross-Cutting Concerns

### Rate Limiting
```
┌─────────────────┬────────────────┬───────────────┐
│  Action         │  Limit         │  Window       │
├─────────────────┼────────────────┼───────────────┤
│  Login          │  5 attempts    │  15 minutes   │
│  Register       │  3 requests    │  1 hour       │
│  Create Post    │  20 posts      │  1 hour       │
│  Add Comment    │  30 comments   │  1 minute     │
│  Send Message   │  30 messages   │  1 minute     │
│  Send Friend    │  10 requests   │  1 hour       │
│  Upload Media   │  50 files      │  1 hour       │
│  Search         │  30 requests   │  1 minute     │
└─────────────────┴────────────────┴───────────────┘
```

### Error Handling
```
Standard Error Response:
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "timestamp": "2025-01-15T10:30:00Z",
  "path": "/api/v1/posts",
  "errors": [
    {
      "field": "content",
      "message": "Content is required"
    }
  ]
}

Error Codes:
  400 - Bad Request (validation error)
  401 - Unauthorized (no token)
  403 - Forbidden (no permission)
  404 - Not Found
  409 - Conflict (duplicate)
  429 - Too Many Requests (rate limit)
  500 - Internal Server Error
```

### Caching Strategy
```
┌─────────────────┬───────────────┬─────────────────┐
│  Data           │  Cache Layer  │  TTL            │
├─────────────────┼───────────────┼─────────────────┤
│  User Session   │  Redis        │  7 days         │
│  User Profile   │  Redis        │  1 hour         │
│  News Feed      │  Redis        │  5 minutes      │
│  Post Detail    │  Redis        │  10 minutes     │
│  Friend List    │  Redis        │  30 minutes     │
│  Online Status  │  Redis        │  5 minutes      │
│  Notification   │  Redis        │  1 hour         │
│  Count (likes)  │  Redis        │  5 minutes      │
└─────────────────┴───────────────┴─────────────────┘
```
